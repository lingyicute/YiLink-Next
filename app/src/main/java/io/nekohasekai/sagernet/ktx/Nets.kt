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

package io.nekohasekai.sagernet.ktx

import cn.hutool.core.lang.Validator
import io.nekohasekai.sagernet.BuildConfig
import libcore.URL
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.roundToInt
import kotlin.random.Random

fun URL.queryParameter(key: String) = queryParameterNotBlank(key).takeIf { it.isNotEmpty() }
var URL.pathSegments: List<String>
    get() = path.split("/").filter { it.isNotEmpty() }
    set(value) {
        path = value.joinToString("/")
    }

fun URL.addPathSegments(vararg segments: String) {
    pathSegments = pathSegments.toMutableList().apply {
        addAll(segments)
    }
}

fun String.isIpAddress(): Boolean {
    return Validator.isIpv4(this) || Validator.isIpv6(this)
}

fun String.isIpv4Address(): Boolean {
    return Validator.isIpv4(this)
}

fun String.isIpv6Address(): Boolean {
    return Validator.isIpv6(this)
}

fun joinHostPort(host: String, port: Int): String {
    if (Validator.isIpv6(host)) {
        return "[$host]:$port"
    }
    return "$host:$port"
}

fun String.unwrapHost(): String {
    if (startsWith("[") && endsWith("]")) {
        return substring(1, length - 1).unwrapHost()
    }
    return this
}

fun mkPort(): Int {
    val socket = Socket()
    socket.reuseAddress = true
    socket.bind(InetSocketAddress(0))
    val port = socket.localPort
    socket.close()
    return port
}

fun String.listByLine(): List<String> {
    return this.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
}

fun String.listByLineOrComma(): List<String> {
    return this.split(",", "\n").map { it.trim() }.filter { it.isNotEmpty() }
}

fun String.isValidHysteriaPort(): Boolean {
    if (this.toIntOrNull() != null) {
        return this.toInt() in 1..65535
    }
    val portRanges = this.split(",")
    if (portRanges.isEmpty()) {
        val parts = this.split("-")
        if (parts.size != 2) {
            return false
        }
        val from = parts[0].toIntOrNull()
        val to = parts[1].toIntOrNull()
        return from != null && from in 1..65535 && to != null && to in 1..65535 && from <= to
    }
    for (portRange in portRanges) {
        if (portRange.toIntOrNull() != null) {
            if (portRange.toInt() <= 0 || portRange.toInt() >= 65536) {
                return false
            }
        } else if (portRange.contains("-")) {
            val parts = portRange.split("-")
            if (parts.size != 2) {
                return false
            }
            val from = parts[0].toIntOrNull()
            val to = parts[1].toIntOrNull()
            if (from == null || to == null || from <= 0 || from >= 65536 || to <= 0 || to >= 65536 || from > to) {
                return false
            }
        } else {
            return false
        }
    }
    return true
}

fun String.isValidHysteriaMultiPort(): Boolean {
    return this.toIntOrNull() == null && this.isValidHysteriaPort()
}

fun String.toHysteriaPort(): Int {
    if (this.toIntOrNull() != null) {
        if (this.toInt() in 1..65535) {
            return this.toInt()
        }
        error("invalid port range")
    }
    val portRanges = this.split(",")
    if (portRanges.isEmpty()) {
        val parts = this.split("-")
        if (parts.size == 2) {
            val from = parts[0].toIntOrNull()
            val to = parts[1].toIntOrNull()
            if (from != null && from in 1..65535 && to != null && to in 1..65535 && from <= to) {
                return Random.nextInt(from, to + 1)
            }
        }
        error("invalid port range")
    }
    val fromList: MutableList<Int> = mutableListOf()
    val toList: MutableList<Int> = mutableListOf()
    var len = 0
    for (portRange in portRanges) {
        if (portRange.toIntOrNull() != null) {
            if (portRange.toInt() <= 0 || portRange.toInt() >= 65536) {
                error("invalid port range")
            }
            fromList.add(portRange.toInt())
            toList.add(portRange.toInt())
            len++
        } else if (portRange.contains("-")) {
            val parts = portRange.split("-")
            if (parts.size != 2) {
                error("invalid port range")
            }
            val from = parts[0].toIntOrNull()
            val to = parts[1].toIntOrNull()
            if (from == null || to == null || from <= 0 || from >= 65536 || to <= 0 || to >= 65536 || from > to) {
                error("invalid port range")
            }
            fromList.add(from)
            toList.add(to)
            len += to - from + 1
        } else {
            error("invalid port range")
        }
    }
    val portIndex = Random.nextInt(0, len)
    var oldLen = 0
    var newLen: Int
    for (i in fromList.indices) {
        newLen = oldLen + toList[i] - fromList[i] + 1
        if (portIndex < newLen) {
            return portIndex - oldLen + fromList[i]
        }
        oldLen = newLen
    }
    error("invalid port range")
}

fun String.toMegaBitsPerSecond(): Int? {
    // mihomo
    if (this.isEmpty()) {
        return 0
    }
    this.toIntOrNull()?.let {
        return it.takeIf { it >= 0 }
    }
    var splitAt = 0
    for ((i, ch) in this.withIndex()) {
        splitAt = i
        if (!ch.isDigit()) {
            break
        }
    }
    this.substring(0, splitAt).toIntOrNull()?.takeIf { it >= 0 }?.let {
        val ret = when (this.substring(splitAt).trim()) {
            "bps" -> (it / 1000000f).roundToInt()
            "Kbps" -> (it / 1000f).roundToInt()
            "Mbps" -> it
            "Gbps" -> it * 1000
            "Tbps" -> it * 1000000
            "Bps" -> (it / 125000f).roundToInt()
            "KBps" -> (it / 125f).roundToInt()
            "MBps" -> it * 8
            "GBps" -> it * 8000
            "TBps" -> it * 8000000
            else -> return null
        }
        return if (it > 0 && ret == 0) 1 else ret
    } ?: return null
}

fun String.toMegaBits(): Int? {
    // sing-box
    if (this.isEmpty()) {
        return null
    }
    this.replace(",", "").trim().toFloatOrNull()?.let {
        if (it < 0f) {
            return null
        }
        val ret = (it / 1000f / 1000f).roundToInt()
        return if (it > 0f && ret == 0) 1 else ret
    }
    var splitAt = 0
    for ((i, ch) in this.withIndex()) {
        splitAt = i
        if (!(ch.isDigit() || ch == '.' || ch == ',')) {
            break
        }
    }
    this.substring(0, splitAt).replace(",", "").toFloatOrNull()?.takeIf { it >= 0f }?.let {
        val ret = when (this.substring(splitAt).trim().lowercase()) {
            "b" -> (it / 1000f / 1000f).roundToInt()
            "k", "kb" -> (it / 1000f).roundToInt()
            "m", "mb" -> it.roundToInt()
            "g", "gb" -> (it * 1000f).roundToInt()
            "t", "tb" -> (it * 1000f * 1000f).roundToInt()
            "p", "pb" -> (it * 1000f * 1000f * 1000f).roundToInt()
            "e", "eb" -> (it * 1000f * 1000f * 1000f * 1000f).roundToInt()
            "ki", "kib" -> (it * 1.024f / 1000f).roundToInt()
            "mi", "mib" -> (it * 1.024f * 1.024f).roundToInt()
            "gi", "gib" -> (it * 1.024f * 1.024f * 1.024f * 1000f).roundToInt()
            "ti", "tib" -> (it * 1.024f * 1.024f * 1.024f * 1.024f * 1000f * 1000f).roundToInt()
            "pi", "pib" -> (it * 1.024f * 1.024f * 1.024f * 1.024f * 1.024f * 1000f * 1000f * 1000f).roundToInt()
            "ei", "eib" -> (it * 1.024f * 1.024f * 1.024f * 1.024f * 1.024f * 1.024f * 1000f * 1000f * 1000f * 1000f).roundToInt()
            else -> return null
        }
        return if (it > 0f && ret == 0) 1 else ret
    } ?: return null
}

const val USER_AGENT = "YiLink/${BuildConfig.VERSION_NAME}"
