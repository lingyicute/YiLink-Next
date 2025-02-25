/******************************************************************************
 *                                                                            *
 * Copyright (C) 2025 by lingyicute <li@92li.us.kg>             *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.nekohasekai.sagernet.group

import io.nekohasekai.sagernet.IPv6Mode
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.SubscriptionType
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.GroupManager
import io.nekohasekai.sagernet.database.ProxyGroup
import io.nekohasekai.sagernet.database.SubscriptionBean
import io.nekohasekai.sagernet.fmt.AbstractBean
import io.nekohasekai.sagernet.fmt.brook.BrookBean
import io.nekohasekai.sagernet.fmt.hysteria.HysteriaBean
import io.nekohasekai.sagernet.fmt.hysteria2.Hysteria2Bean
import io.nekohasekai.sagernet.fmt.juicity.JuicityBean
import io.nekohasekai.sagernet.fmt.naive.NaiveBean
import io.nekohasekai.sagernet.fmt.trojan_go.TrojanGoBean
import io.nekohasekai.sagernet.fmt.tuic.TuicBean
import io.nekohasekai.sagernet.fmt.tuic5.Tuic5Bean
import io.nekohasekai.sagernet.fmt.v2ray.StandardV2RayBean
import io.nekohasekai.sagernet.ktx.*
import kotlinx.coroutines.*
import libcore.Libcore
import java.net.Inet4Address
import java.net.InetAddress
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class GroupUpdater {

    abstract suspend fun doUpdate(
        proxyGroup: ProxyGroup,
        subscription: SubscriptionBean,
        userInterface: GroupManager.Interface?,
        byUser: Boolean
    )

    data class Progress(
        var max: Int
    ) {
        var progress by AtomicInteger()
    }

    protected suspend fun forceResolve(
        profiles: List<AbstractBean>, groupId: Long?
    ) {
        val connected = DataStore.startedProfile > 0

        var dohUrl: String? = null
        if (connected) {
            val remoteDns = DataStore.remoteDns
            when {
                remoteDns.startsWith("https+local://") -> dohUrl = remoteDns.replace(
                    "https+local://", "https://"
                )
                remoteDns.startsWith("https://") -> dohUrl = remoteDns
            }
        } else {
            val directDns = DataStore.directDns
            when {
                directDns.startsWith("https+local://") -> dohUrl = directDns.replace(
                    "https+local://", "https://"
                )
                directDns.startsWith("https://") -> dohUrl = directDns
            }
        }

        val dohHttpUrl = dohUrl ?: if (connected) {
            "https://dns.google/dns-query" // TODO: do not hardcode this
        } else {
            "https://doh.pub/dns-query" // TODO: do not hardcode this
        }

        val client = Libcore.newHttpClient().apply {
            modernTLS()
            keepAlive()
            if (SagerNet.started && DataStore.startedProfile > 0) {
                useSocks5(DataStore.socksPort)
            }
        }

        Logs.d("Using doh url $dohHttpUrl")

        val ipv6Mode = DataStore.ipv6Mode
        val lookupPool = newFixedThreadPoolContext(5, "DNS Lookup")
        val lookupJobs = mutableListOf<Job>()
        val progress = Progress(profiles.size)
        if (groupId != null) {
            GroupUpdater.progress[groupId] = progress
            GroupManager.postReload(groupId)
        }
        val ipv6First = ipv6Mode >= IPv6Mode.PREFER

        for (profile in profiles) {
            if (profile.serverAddress.isIpAddress()) continue

            lookupJobs.add(GlobalScope.launch(lookupPool) {
                try {
                    val message = Libcore.encodeDomainNameSystemQuery(
                        1, profile.serverAddress, ipv6Mode
                    )
                    val response = client.newRequest().apply {
                        setMethod("POST")
                        setURL(dohHttpUrl)
                        setContent(message)
                        setHeader("Accept", "application/dns-message")
                        setHeader("Content-Type", "application/dns-message")
                    }.execute()

                    val results = Libcore.decodeContentDomainNameSystemResponse(response.content)
                        .trimStart()
                        .split(" ")
                        .map { InetAddress.getByName(it) }

                    if (results.isEmpty()) error("empty response")
                    rewriteAddress(profile, results, ipv6First)
                } catch (e: Exception) {
                    Logs.d("Lookup ${profile.serverAddress} failed: ${e.readableMessage}",e)
                }
                if (groupId != null) {
                    progress.progress++
                    GroupManager.postReload(groupId)
                }
            })
        }

        client.close()
        lookupJobs.joinAll()
        lookupPool.close()
    }

    protected fun rewriteAddress(
        bean: AbstractBean, addresses: List<InetAddress>, ipv6First: Boolean
    ) {
        val address = addresses.sortedBy { (it is Inet4Address) xor ipv6First }[0].hostAddress

        with(bean) {
            when (this) {
                is StandardV2RayBean -> {
                    when (security) {
                        "tls" -> if (sni.isEmpty()) sni = bean.serverAddress
                    }
                }
                is TrojanGoBean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
                is HysteriaBean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
                is Hysteria2Bean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
                is NaiveBean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
                is BrookBean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
                is JuicityBean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
                is TuicBean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
                is Tuic5Bean -> {
                    if (sni.isEmpty()) sni = bean.serverAddress
                }
            }

            bean.serverAddress = address
        }
    }

    companion object {

        val updating = Collections.synchronizedSet<Long>(mutableSetOf())
        val progress = Collections.synchronizedMap<Long, Progress>(mutableMapOf())

        fun startUpdate(proxyGroup: ProxyGroup, byUser: Boolean) {
            runOnDefaultDispatcher {
                executeUpdate(proxyGroup, byUser)
            }
        }

        suspend fun executeUpdate(proxyGroup: ProxyGroup, byUser: Boolean): Boolean {
            return coroutineScope {
                if (!updating.add(proxyGroup.id)) cancel()
                GroupManager.postReload(proxyGroup.id)

                val subscription = proxyGroup.subscription!!
                val connected = DataStore.startedProfile > 0
                val userInterface = GroupManager.userInterface

                if (userInterface != null) {
                    if ((subscription.link?.startsWith("http://") == true || subscription.updateWhenConnectedOnly) && !connected) {
                        if (!userInterface.confirm(app.getString(R.string.update_subscription_warning))) {
                            finishUpdate(proxyGroup)
                            cancel()
                        }
                    }
                }

                try {
                    when (subscription.type) {
                        SubscriptionType.RAW -> RawUpdater
                        SubscriptionType.OOCv1 -> OpenOnlineConfigUpdater
                        SubscriptionType.SIP008 -> SIP008Updater
                        else -> error("wtf")
                    }.doUpdate(proxyGroup, subscription, userInterface, byUser)
                    true
                } catch (e: Throwable) {
                    Logs.w(e)
                    userInterface?.onUpdateFailure(proxyGroup, e.readableMessage)
                    finishUpdate(proxyGroup)
                    false
                }
            }
        }


        suspend fun finishUpdate(proxyGroup: ProxyGroup) {
            updating.remove(proxyGroup.id)
            progress.remove(proxyGroup.id)
            GroupManager.postUpdate(proxyGroup)
        }

    }

}