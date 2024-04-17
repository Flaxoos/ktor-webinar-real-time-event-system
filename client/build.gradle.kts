plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(projects.common)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.circuitBreaker)
}
