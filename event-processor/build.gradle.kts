plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(projects.common)

    implementation(libs.ktor.server.kafka)
    implementation(libs.ktor.server.rateLimiting)
}

application {
    mainClass.set("io.github.flaxoos.ApplicaitonKt")
}
