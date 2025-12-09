plugins {
    alias(libs.plugins.kotlin.jvm)
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
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.converter.jackson)
    implementation(libs.bouncyCastle)
    implementation(libs.bouncyCastle.bcpkix)
    implementation(platform(libs.koin.bom))
    implementation(libs.jna)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}