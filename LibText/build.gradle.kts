plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "$rootDir/build_aar.gradle")

android {
    namespace = "com.bihe0832.android.lib.text"
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    buildToolsVersion = rootProject.extra["buildToolsVersion"] as String

    defaultConfig {
        minSdk = rootProject.extra["libMinSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    lint {
        abortOnError = false
    }
}
