plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.20" // <-- Add this version constraint explicitly
}
android {
    namespace = "com.meshnet.llm"
    compileSdk = 34 // Change it back to this format
    
    defaultConfig {

        applicationId = "com.meshnet.llm"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = false
        dataBinding = false
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}
