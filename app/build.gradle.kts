plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.ailauncher"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.ailauncher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndk { abiFilters += "arm64-v8a" } // modern phones only; keeps APK smaller
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    // Firefox engine. If Sync fails, replace "+" with a specific version
    // listed at https://maven.mozilla.org/?prefix=maven2/org/mozilla/geckoview/
    implementation("org.mozilla.geckoview:geckoview-omni:+")
}
