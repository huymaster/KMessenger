plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.github.huymaster"
version = "0.0.1"
val mainClassName = "com.github.huymaster.textguardian.server.ApplicationKt"

application {
    mainClass = mainClassName
}

dependencies {
    implementation(libs.ktor.server.rate.limiting)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.gson)
    implementation(libs.ktor.server.partial.content)
    implementation(libs.ktor.server.http.redirect)
    implementation(libs.ktor.server.hsts)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.ssl)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.firebase.admin)
    implementation(libs.ktorm)
    implementation(libs.gson)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(project(":core")) {
        isTransitive = false
    }
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

tasks.jar {
    manifest {
        this.attributes("Main-Class" to mainClassName)
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(sourceSets.main.get().output)

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Exec>("runServer") {
    println("Running server at ${file(".").parentFile}")
    dependsOn(tasks.jar)
    val jarPath = tasks.jar.get().outputs.files.singleFile
    commandLine("authbind", "--deep", "java", "-jar", "$jarPath")
    workingDir = file(".").parentFile
}