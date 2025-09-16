plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gson)
    implementation(platform(libs.mongo.bom))
    implementation(libs.mongo.kotlin.coroutine)
    implementation(libs.mongo.bson.kotlinx)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}