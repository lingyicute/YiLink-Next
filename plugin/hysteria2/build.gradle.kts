plugins {
    id("com.android.application")
}

android {
    defaultConfig {
        applicationId = "com.github.lyi.yilink.plugin.hysteria2"
    }
    namespace = "io.nekohasekai.sagernet.plugin.hysteria2"
}

setupPlugin("hysteria2")