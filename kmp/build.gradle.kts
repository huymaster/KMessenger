@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvmToolchain(17)
    jvm()
    wasmJs {
        browser {
//            commonWebpackConfig { outputFileName = "composeApp.js" }
        }
        binaries.executable()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jna)
            implementation(project(":core"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.github.huymaster.textguardian.jvm.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.github.huymaster.textguardian.jvm"
            packageVersion = "1.0.0"
        }
    }
}