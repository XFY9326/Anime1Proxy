import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("io.ktor.plugin") version "2.3.12"
}

group = "tool.xfy9326"
version = "0.0.1"

application {
    mainClass.set("tool.xfy9326.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cio-jvm")
    implementation("io.ktor:ktor-server-resources-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")

    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-java-jvm")
    implementation("io.ktor:ktor-client-logging-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")

    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    implementation("org.jsoup:jsoup:1.18.1")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
