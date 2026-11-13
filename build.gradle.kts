import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    kotlin("jvm") version "2.4.21"
}

group = "kr.foundcake.ytarget"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

kotlin {
    jvmToolchain(25)
}

tasks.processResources {
    // Paper downloads kotlin-stdlib at load time through plugin.yml `libraries`.
    val props = mapOf("version" to version.toString(), "kotlinVersion" to getKotlinPluginVersion())
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}