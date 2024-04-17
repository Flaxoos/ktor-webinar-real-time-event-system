import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version libs.versions.kotlin.get() apply false
    id("io.ktor.plugin") version libs.versions.ktor.get()
    alias(libs.plugins.kotlin.serialization)
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.pkg.github.com/flaxoos/flax-gradle-plugins")
        }
    }
}

tasks.withType<ShadowJar> {
    enabled = false
}

val ktorServerCore = libs.ktor.server.core.jvm.get()
val ktorServerCio = libs.ktor.server.cio.jvm.get()
val logback = libs.logback.classic.get()
val ktorServerContentNegotiation = libs.ktor.server.contentNegotiation.jvm.get()
val ktorSerializationKotlinxJson = libs.ktor.serialization.kotlinx.json.get()
val ktorServerTestHost = libs.ktor.server.test.host.get()
val ktorServerTests = libs.ktor.server.tests.jvm.get()
val kotlinTestJunit = libs.kotlin.test.junit.get()
val kotlinxDatetime = libs.kotlinx.datetime.get()

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.ktor.plugin")
    apply(plugin = "kotlinx-serialization")

    group = "io.github.flaxoos"
    version = "0.0.1"

    dependencies {
        implementation(ktorServerCore)
        implementation(ktorServerCio)
        implementation(logback)
        implementation(kotlinxDatetime)
        implementation(ktorServerContentNegotiation)
        implementation(ktorSerializationKotlinxJson)
        testImplementation(ktorServerTestHost)
        testImplementation(ktorServerTests)
        testImplementation(kotlinTestJunit)
    }

    sourceSets {
        main {
            resources {
                srcDirs("$rootDir/logging")
            }
        }
    }
}
