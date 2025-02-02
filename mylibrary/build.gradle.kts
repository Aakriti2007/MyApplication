import io.grpc.internal.SharedResourceHolder.release

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.example.mylibrary"
    compileSdk = 34

    defaultConfig {
        minSdk = 25

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.github.Aakriti2007"
                artifactId = "idauth"
                version = "1.0"
            }
        }

    }
}