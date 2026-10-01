plugins {
    id("com.android.application")
}
android {
    namespace = "com.example.ailauncher"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.example.ailauncher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndk { abiFilters += "arm64-v8a" }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("org.mozilla.geckoview:geckoview-omni:+")
}
