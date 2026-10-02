plugins {
    id("com.android.application")
}
android {
    namespace = "com.example.ailauncher"
    compileSdk = 37
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    defaultConfig {
        applicationId = "com.example.ailauncher"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
        ndk { abiFilters += "arm64-v8a" }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("org.mozilla.geckoview:geckoview-omni:157.0.20260924084938")
}
