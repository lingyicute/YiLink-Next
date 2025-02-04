plugins {
    id("com.android.application")
}

android {
    defaultConfig {
        applicationId = "com.github.lyi.yilink.plugin.mieru"
    }
    namespace = "io.nekohasekai.sagernet.plugin.mieru"
}

setupPlugin("mieru")