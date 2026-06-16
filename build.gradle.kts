import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("plugin.serialization") version "1.7.22"
}

group = "money.tegro"
version = "1.1.2"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.ton:ton-kotlin:0.1.1")
    implementation("io.github.microutils:kotlin-logging:3.0.4")
    implementation("io.awspring.cloud:spring-cloud-starter-aws-messaging:2.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
