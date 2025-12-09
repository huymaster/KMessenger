plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serializer)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.github.huymaster.textguardian.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.huymaster.textguardian.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("rd") {
            storeFile = file("/home/huymaster/kmessenger.jks")
            storePassword = "062425"
            keyAlias = "kmessenger"
            keyPassword = "062425"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("rd")
        }
        debug {
            signingConfig = signingConfigs.getByName("rd")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    packaging {
        resources {
            pickFirsts += "/META-INF/DEPENDENCIES"
            pickFirsts += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "/META-INF/io.netty.versions.properties"
            pickFirsts += "/META-INF/INDEX.LIST"
        }
    }
}

kotlin {
    compilerOptions {
    }
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.biometric)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.jackson)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.converter.jackson)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.jackson)
    implementation(libs.ktor.client.websockets)
    implementation(libs.jna)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.android.compat)
    implementation(libs.koin.compose)
    implementation(libs.ktorm)
    implementation(libs.bouncyCastle)
    implementation(libs.bouncyCastle.bcpkix)
    implementation(project(":core")) {
        isTransitive = false
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}