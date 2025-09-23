plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jackson)
    implementation(libs.ktorm)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.jackson)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}