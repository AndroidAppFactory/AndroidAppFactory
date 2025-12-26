plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

val applicationID = "com.bihe0832.android.test"
val applicationName = "子勰Lib"
val applicationPrefix = "Zixie"

android {
    namespace = "com.bihe0832.android.test"
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    buildToolsVersion = rootProject.extra["buildToolsVersion"] as String

    defaultConfig {
        applicationId = applicationID
        minSdk = rootProject.extra["appMinSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
        versionName = rootProject.extra["versionName"] as String
        versionCode = rootProject.extra["versionCode"] as Int
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }

    packagingOptions {
        jniLibs {
            keepDebugSymbols.addAll(listOf("**/*.so"))
            pickFirsts.addAll(listOf(
                "lib/armeabi/libc++_shared.so",
                "lib/x86/libc++_shared.so",
                "lib/x86_64/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so",
                "lib/arm64-v8a/libc++_shared.so"
            ))
        }
        resources {
            excludes.addAll(listOf(
                "META-INF/*.kotlin_module",
                "META-INF/proguard/androidx-annotations.pro",
                "META-INF/proguard/coroutines.pro"
            ))
        }
    }

    lint {
        abortOnError = false
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }

    @Suppress("UNCHECKED_CAST")
    val signingConfigMap = rootProject.extra["signingConfigs"] as Map<String, Any>
    
    signingConfigs {
        getByName("debug") {
            keyAlias = signingConfigMap["keyAlias"] as String
            keyPassword = signingConfigMap["keyPassword"] as String
            storeFile = file(signingConfigMap["storeFile"] as String)
            storePassword = signingConfigMap["storePassword"] as String
        }
        create("release") {
            keyAlias = signingConfigMap["keyAlias"] as String
            keyPassword = signingConfigMap["keyPassword"] as String
            storeFile = file(signingConfigMap["storeFile"] as String)
            storePassword = signingConfigMap["storePassword"] as String
        }
    }

    setProperty("archivesBaseName", "${applicationPrefix}_V${defaultConfig.versionName}_${defaultConfig.versionCode}")

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = rootProject.extra["minifyEnabled"] as Boolean
            proguardFiles("./../proguard-rules-common.pro")
            isShrinkResources = false
        }
    }

    configurations.all {
        resolutionStrategy {
            force("androidx.lifecycle:lifecycle-livedata-core:2.6.0")
        }
    }
}
