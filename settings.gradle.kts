pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.1"
}

rootProject.name = "coffeeproxy"

stonecutter {
    kotlinController = true
    create(rootProject) {
        val intermediaryVersions = listOf("1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11")
        val mojmapVersions = listOf("26.1.2", "26.2")

        for (v in intermediaryVersions) {
            version(v, v)
        }
        for (v in mojmapVersions) {
            val project = version(v, v)
            project.buildscript = "build.mojmap.gradle.kts"
        }

        vcsVersion = "1.21.10"
    }
}
