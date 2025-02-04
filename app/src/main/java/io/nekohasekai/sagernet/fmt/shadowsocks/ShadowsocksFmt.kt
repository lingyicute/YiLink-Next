/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <contact-sagernet@sekai.icu>             *
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

package io.nekohasekai.sagernet.fmt.shadowsocks

import cn.hutool.core.codec.Base64
import cn.hutool.json.JSONObject
import com.github.shadowsocks.plugin.PluginConfiguration
import com.github.shadowsocks.plugin.PluginOptions
import io.nekohasekai.sagernet.ktx.decodeBase64UrlSafe
import io.nekohasekai.sagernet.ktx.queryParameter
import io.nekohasekai.sagernet.ktx.unUrlSafe
import io.nekohasekai.sagernet.ktx.urlSafe
import libcore.Libcore

fun PluginConfiguration.fixInvalidParams() {

    if (selected == "simple-obfs") {

        pluginsOptions["obfs-local"] = getOptions().apply { id = "obfs-local" }
        pluginsOptions.remove(selected)
        selected = "obfs-local"

    }

}

fun ShadowsocksBean.fixInvalidParams() {
    if (method == "plain") method = "none"
    plugin = PluginConfiguration(plugin).apply { fixInvalidParams() }.toString()

}

fun parseShadowsocks(url: String): ShadowsocksBean {

    if (url.contains("@")) {

        var link = Libcore.parseURL(url)

        if (link.username.isEmpty()) { // fix justmysocks's shit link
            link = Libcore.parseURL(
                ("ss://" + url.substringAfter("ss://").substringBefore("#").decodeBase64UrlSafe())
            )
            link.setRawFragment(url.substringAfter("#"))
        }

        // ss-android style

        if (link.password.isNotEmpty() || url.substringAfter("ss://").substringBefore("@").endsWith(":")) {

            return ShadowsocksBean().apply {

                serverAddress = link.host
                serverPort = link.port
                method = link.username
                password = link.password
                plugin = link.queryParameter("plugin") ?: ""
                name = link.fragment
                fixInvalidParams()

            }

        }

        val methodAndPswd = link.username.decodeBase64UrlSafe()

        return ShadowsocksBean().apply {

            serverAddress = link.host
            serverPort = link.port
            method = methodAndPswd.substringBefore(":")
            password = methodAndPswd.substringAfter(":")
            plugin = link.queryParameter("plugin") ?: ""
            name = link.fragment

            fixInvalidParams()

        }

    } else {

        // v2rayN style

        var v2Url = url

        if (v2Url.contains("#")) v2Url = v2Url.substringBefore("#")

        val link = Libcore.parseURL(
            ("ss://" + v2Url.substringAfter("ss://").decodeBase64UrlSafe())
        )

        return ShadowsocksBean().apply {

            serverAddress = link.host
            serverPort = link.port
            method = link.username
            password = link.password
            plugin = ""
            if (url.contains("#")) {
                name = url.substringAfter("#").unUrlSafe()
            }

            fixInvalidParams()

        }

    }

}

fun ShadowsocksBean.toUri(): String {

    val builder = Libcore.newURL("ss")
    builder.host = serverAddress
    builder.port = serverPort
    if (method.startsWith("2022-blake3-")) {
        builder.username = method
        builder.password = password
    } else {
        builder.username = Base64.encodeUrlSafe("$method:$password")
    }

    if (plugin.isNotEmpty() && PluginConfiguration(plugin).selected.isNotEmpty()) {
        var p = PluginConfiguration(plugin).selected
        if (PluginConfiguration(plugin).getOptions().toString().isNotEmpty()) {
            p += ";" + PluginConfiguration(plugin).getOptions().toString()
        }
        builder.rawPath = "/"
        builder.addQueryParameter("plugin", p)
    }

    if (name.isNotEmpty()) {
        builder.setRawFragment(name.urlSafe())
    }

    return builder.string

}

fun JSONObject.parseShadowsocks(): ShadowsocksBean {
    return ShadowsocksBean().apply {
        var pluginStr = ""
        val pId = getStr("plugin")
        if (!pId.isNullOrEmpty()) {
            val plugin = PluginOptions(pId, getStr("plugin_opts"))
            pluginStr = plugin.toString(false)
        }

        serverAddress = getStr("server")
        serverPort = getInt("server_port")
        password = getStr("password")
        method = getStr("method")
        plugin = pluginStr
        name = getStr("remarks", "")

        fixInvalidParams()
    }
}
