import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.hierynomus:sshj:0.40.0")
    }
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
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.jackson)
    implementation(libs.ktor.server.partial.content)
    implementation(libs.ktor.server.http.redirect)
    implementation(libs.ktor.server.hsts)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.logback.classic)
    implementation(libs.firebase.admin)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.koin.ktor)
    implementation(libs.jackson)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.ktorm)
    implementation(libs.ktorm.postgresql)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.converter.jackson)
    implementation(libs.postgresql)
    implementation(libs.bouncyCastle)
    implementation(libs.bouncyCastle.bcpkix)
    implementation(libs.jmimemagic)
    implementation(libs.commonsIo)
    implementation(platform(libs.mongo.bom))
    implementation(libs.mongo.kotlin.coroutine)
    implementation(libs.mongo.kotlin.extenstions)
    implementation(libs.mongo.bson.kotlinx)
    implementation(project(":core")) { isTransitive = false }
    implementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)
    testImplementation(platform(libs.junit5.bom))
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.platform)
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

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("compileTestKotlin") {
}

tasks.register("deploy") {
    group = "build"
    dependsOn(tasks.jar)
    val file = tasks.jar.get().outputs.files.singleFile

    doLast {
        fun SSHClient.exec(cmd: String) {
            val session = startSession()
            session.exec(cmd).join()
            session.close()
        }

        val client = SSHClient()
        client.useCompression()
        client.addHostKeyVerifier(PromiscuousVerifier())
        client.connect("api-textguardian.ddns.net")
        val key = client.loadKeys("${System.getProperty("user.home")}/.ssh/id_rsa_cloud")
        client.authPublickey("root", key)
        val tempFile = File.createTempFile("service", "tmp").apply {
            deleteOnExit()
        }
        tempFile.writeText(
            """
            [Unit]
            Description=KMessenger Service
            After=network.target

            [Service]
            Type=exec
            RemainAfterExit=false
            ExecStart=sudo -i -u root /usr/bin/java -jar /root/server.jar
            ExecStop=touch /root/stop
            Restart=on-failure
            RestartSec=10

            [Install]
            WantedBy=multi-user.target
            """.trimIndent()
        )

        println("Stopping service")
        client.exec("mkdir -p /root")
        client.exec("systemctl stop kmessenger.service")
        client.exec("rm -f /root/server.jar")
        client.exec("rm -f /usr/lib/systemd/system/kmessenger.service")

        val transfer = client.newSCPFileTransfer()
        println("Uploading kmessenger.service...")
        transfer.upload(FileSystemFile(tempFile), "/usr/lib/systemd/system/kmessenger.service")
        println("Uploading server.jar...")
        transfer.upload(FileSystemFile(file), "/root/server.jar")

        println("Enabling service")
        client.exec("systemctl daemon-reload")
        client.exec("systemctl enable kmessenger.service")
        client.exec("systemctl start kmessenger.service")
        client.close()
    }
}