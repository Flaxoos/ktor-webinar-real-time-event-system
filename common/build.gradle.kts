import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("io.ktor.plugin") apply false
}

dependencies {
    implementation(libs.ktor.server.kafka)
}
