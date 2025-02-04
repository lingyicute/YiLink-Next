package io.nekohasekai.sagernet.fmt.http3

import io.nekohasekai.sagernet.ktx.queryParameter
import io.nekohasekai.sagernet.ktx.urlSafe
import libcore.Libcore

fun parseHttp3(link: String): Http3Bean {
    val url = Libcore.parseURL(link)
    if (url.path != "/" && url.path != "") error("Not http3 proxy")

    return Http3Bean().apply {
        serverAddress = url.host
        serverPort = url.port.takeIf { it > 0 } ?: 443
        username = url.username
        password = url.password
        name = url.fragment
        url.queryParameter("sni")?.let {
            sni = it
        }
    }
}

fun Http3Bean.toUri(): String {
    val builder = Libcore.newURL("quic")
    builder.host = serverAddress
    builder.port = serverPort

    if (username.isNotEmpty()) {
        builder.username = username
    }
    if (password.isNotEmpty()) {
        builder.password = password
    }
    if (sni.isNotEmpty()) {
        // non-standard
        builder.addQueryParameter("sni", sni)
    }
    if (name.isNotEmpty()) {
        builder.setRawFragment(name.urlSafe())
    }

    return builder.string
}