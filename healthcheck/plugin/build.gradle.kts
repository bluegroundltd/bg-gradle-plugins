plugins {
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
}

repositories {
    mavenCentral()
}

group = "com.theblueground.healthcheck"
version = "0.0.1"

gradlePlugin {
    plugins {
        create("healthCheckPlugin") {
            id = "com.theblueground.healthcheck"
            displayName = "Healthcheck plugin"
            description = "This plugin allows you to do health checks on a specified endpoint"
            implementationClass = "com.theblueground.healthcheck.HealthCheckPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.github.kittinunf.fuel:fuel:2.3.1") {
        because("Used in order to `ping` the healthcheck endpoint!")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

pluginBundle {
    website = "https://engineering.theblueground.com/blog/"
    vcsUrl = "https://github.com/bluegroundltd/bg-gradle-plugins"
    description = "This plugin allows you to do health checks on a specified endpoint"
    tags = listOf("Blueground", "kotlin", "healthcheck")
}
