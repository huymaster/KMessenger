@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap/") }
    }
}


dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://packages.confluent.io/maven/")
    }
}

rootProject.name = "KMessenger"

include("server")
include("android")
include("core")