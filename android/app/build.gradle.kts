plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lvonasek.csvr"
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "com.lvonasek"
        applicationIdSuffix = "csvr"
        minSdk = 26
        targetSdk = 26
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("../../3rdparty/xash3d-fwgs/3rdparty/extras/xash-extras", "../../3rdparty/cs16client-extras")
            java.srcDir("../../3rdparty/SDL/android-project/app/src/main/java")
            jniLibs.srcDirs("../../3rdparty/xash3d-fwgs/3rdparty/openxr/lib/android")
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
