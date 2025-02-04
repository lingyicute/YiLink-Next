import cn.hutool.crypto.digest.DigestUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.Project
import org.kohsuke.github.GitHubBuilder
import java.io.File
import java.util.*

fun Project.downloadAssets(update: Boolean) {
    val assetsDir = File(projectDir, "src/main/assets")
    val downloader = OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build()

    val github = GitHubBuilder().build()

    val geoipVersion = File(assetsDir, "v2ray/geoip.version.txt")
    val geoipRelease = if (update) {
        github.getRepository("v2fly/geoip").latestRelease
    } else {
        github.getRepository("v2fly/geoip").listReleases().find {
            it.tagName == geoipVersion.readText()
        }
    } ?: error("unable to list geoip release")

    val geoipFile = File(assetsDir, "v2ray/geoip.dat")

    if (update) {
        geoipVersion.deleteRecursively()
    }
    geoipFile.parentFile.mkdirs()

    val geoipDat = (geoipRelease.listAssets().toSet().find { it.name == "geoip.dat" }
        ?: error("geoip.dat not found in ${geoipRelease.assetsUrl}")).browserDownloadUrl

    val geoipDatSha256sum = (geoipRelease.listAssets().toSet().find { it.name == "geoip.dat.sha256sum" }
        ?: error("geoip.dat.sha256sum not found in ${geoipRelease.assetsUrl}")).browserDownloadUrl

    println("Downloading $geoipDatSha256sum ...")

    val geoipChecksum = downloader.newCall(
        Request.Builder().url(geoipDatSha256sum).build()
    ).execute().body.string().trim().substringBefore(" ").uppercase(Locale.ROOT)

    var count = 0

    while (true) {
        count++

        println("Downloading $geoipDat ...")

        downloader.newCall(
            Request.Builder().url(geoipDat).build()
        ).execute().body.byteStream().use {
            geoipFile.outputStream().use { out -> it.copyTo(out) }
        }

        val fileSha256 = DigestUtil.sha256Hex(geoipFile).uppercase(Locale.ROOT)
        if (fileSha256 != geoipChecksum) {
            System.err.println(
                "Error verifying ${geoipFile.name}: \nLocal: ${
                    fileSha256.uppercase(
                        Locale.ROOT
                    )
                }\nRemote: $geoipChecksum"
            )
            if (count > 3) error("Exit")
            System.err.println("Retrying...")
            continue
        }

        if (update) {
            geoipVersion.writeText(geoipRelease.tagName)
        }
        break
    }

    val geositeVersion = File(assetsDir, "v2ray/geosite.version.txt")
    val geositeRelease = if (update) {
        github.getRepository("v2fly/domain-list-community").latestRelease
    } else {
        github.getRepository("v2fly/domain-list-community").listReleases().find {
            it.tagName == geositeVersion.readText()
        }
    } ?: error("unable to list geosite release")

    val geositeFile = File(assetsDir, "v2ray/geosite.dat")

    if (update) {
        geositeVersion.deleteRecursively()
    }

    val geositeDat = (geositeRelease.listAssets().toSet().find { it.name == "dlc.dat" }
        ?: error("dlc.dat not found in ${geositeRelease.assetsUrl}")).browserDownloadUrl

    val geositeDatSha256sum = (geositeRelease.listAssets().toSet().find { it.name == "dlc.dat.sha256sum" }
        ?: error("dlc.dat.sha256sum not found in ${geositeRelease.assetsUrl}")).browserDownloadUrl

    println("Downloading $geositeDatSha256sum ...")

    val geositeChecksum = downloader.newCall(
        Request.Builder().url(geositeDatSha256sum).build()
    ).execute().body.string().trim().substringBefore(" ").uppercase(Locale.ROOT)

    count = 0

    while (true) {
        count++

        println("Downloading $geositeDat ...")

        downloader.newCall(
            Request.Builder().url(geositeDat).build()
        ).execute().body.byteStream().use {
            geositeFile.outputStream().use { out -> it.copyTo(out) }
        }

        val fileSha256 = DigestUtil.sha256Hex(geositeFile).uppercase(Locale.ROOT)
        if (fileSha256 != geositeChecksum) {
            System.err.println(
                "Error verifying ${geositeFile.name}: \nLocal: ${
                    fileSha256.uppercase(
                        Locale.ROOT
                    )
                }\nRemote: $geositeChecksum"
            )
            if (count > 3) error("Exit")
            System.err.println("Retrying...")
            continue
        }

        if (update) {
            geositeVersion.writeText(geositeRelease.tagName)
        }
        break
    }

}