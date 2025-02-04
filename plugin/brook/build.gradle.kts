plugins {
    id("com.android.application")
}

android {
    defaultConfig {
        applicationId = "com.github.lyi.yilink.plugin.brook"
    }
    namespace = "io.nekohasekai.sagernet.plugin.brook"
}

setupPlugin("brook")