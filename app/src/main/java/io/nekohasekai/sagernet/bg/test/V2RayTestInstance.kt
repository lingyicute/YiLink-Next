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

package io.nekohasekai.sagernet.bg.test

import io.nekohasekai.sagernet.bg.GuardedProcessPool
import io.nekohasekai.sagernet.bg.proto.V2RayInstance
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.fmt.buildV2RayConfig
import io.nekohasekai.sagernet.ktx.Logs
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import io.nekohasekai.sagernet.ktx.tryResume
import io.nekohasekai.sagernet.ktx.tryResumeWithException
import libcore.Libcore
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class V2RayTestInstance(profile: ProxyEntity, val link: String, val timeout: Int) : V2RayInstance(
    profile
) {

    lateinit var continuation: Continuation<Int>
    suspend fun doTest(): Int {
        return suspendCoroutine { c ->
            continuation = c
            processes = GuardedProcessPool {
                Logs.w(it)
                c.tryResumeWithException(it)
            }
            runOnDefaultDispatcher {
                try {
                    init(false)
                    launch()
                    Logs.d(config.config)
                    pluginConfigs.forEach { (_, plugin) ->
                        val (_, content) = plugin
                        Logs.d(content)
                    }
                    c.tryResume(Libcore.urlTest(v2rayPoint, "", link, timeout))
                } catch (e: Exception) {
                    c.tryResumeWithException(e)
                }
            }
        }
    }

    override fun buildConfig() {
        config = buildV2RayConfig(profile, true)
    }
}