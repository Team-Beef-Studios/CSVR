import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.ChronoUnit

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "su.xash.engine"
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "su.xash"
        applicationIdSuffix = "engine"
        versionName = "0.21"
        versionCode = getBuildNum()
        minSdk = 21
        targetSdk = 29
        compileSdk = 34

        externalNativeBuild {
            cmake {
                abiFilters("arm64-v8a")
                arguments("-DANDROID_USE_LEGACY_TOOLCHAIN_FILE=OFF")
            }
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path = file("CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".test"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        register("asan") {
            initWith(getByName("debug"))
        }

        register("continuous") {
            initWith(getByName("release"))
            applicationIdSuffix = ".test"
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("../../3rdparty/xash3d-fwgs/3rdparty/extras/xash-extras", "../moddb")
            java.srcDir("../../3rdparty/SDL/android-project/app/src/main/java")
        }
    }

    lint {
        abortOnError = false
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    androidResources {
        noCompress += ""
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            keepDebugSymbols.add("**/*.so")
        }
    }
}

dependencies {
    implementation("ch.acra:acra-http:5.11.2")
}

fun getBuildNum(): Int {
    val now = LocalDateTime.now()
    val releaseDate = LocalDateTime.of(2015, Month.APRIL, 1, 0, 0, 0)
    val qBuildNum = releaseDate.until(now, ChronoUnit.DAYS)
    val minuteOfDay = now.hour * 60 + now.minute
    return (qBuildNum * 10000 + minuteOfDay).toInt()
}
