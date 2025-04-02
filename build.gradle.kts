plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "de.mineking"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.dv8tion:JDA:5.3.0")
    implementation("club.minnced:discord-webhooks:0.8.4")

    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("com.charleskorn.kaml:kaml:0.73.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.5.15")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveFileName = "Bot.jar"

    manifest {
        attributes(mapOf("Main-Class" to "de.slimecloud.aprilfools.MainKt"))
    }
}