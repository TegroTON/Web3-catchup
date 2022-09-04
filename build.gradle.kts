import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "money.tegro"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    shadow("com.github.andreypfau.ton-kotlin:ton-kotlin:545e3f3a3b")
    shadow("io.github.microutils:kotlin-logging:2.1.23")
    implementation("org.apache.kafka:connect-api:3.2.1")
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.shadow.get())
    }
}
