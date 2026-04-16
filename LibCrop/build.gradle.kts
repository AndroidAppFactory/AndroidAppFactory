plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "$rootDir/build_aar.gradle")

android {
    namespace = "com.bihe0832.android.lib.crop.jni"
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    buildToolsVersion = rootProject.extra["buildToolsVersion"] as String
    ndkVersion = rootProject.extra["ndk_version"] as String

    defaultConfig {
        minSdk = rootProject.extra["libMinSdkVersion"] as Int

        externalNativeBuild {
            cmake {
                cppFlags("-frtti", "-fexceptions", "-std=c++11", "-fvisibility=hidden", "-Wall")
                arguments("-DANDROID_PLATFORM=android-21")
                arguments("-DANDROID_TOOLCHAIN=clang")
                arguments("-DANDROID_STL=c++_shared")
            }
        }

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += (rootProject.extra["ndk_abiFilters_project"] as String).split(",")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = rootProject.extra["cmake_version"] as String
        }
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
