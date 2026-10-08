// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: CC0-1.0

pluginManagement {
    repositories {
		mavenLocal()
		maven("https://maven.axle.coffee/releases")
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/")
		maven("https://maven.architectury.dev")
		maven("https://maven.minecraftforge.net")
		maven("https://maven.kikugie.dev/snapshots")
		maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("net.fabricmc.fabric-loom") version "1.16.1" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.16.1" apply false
    id("dev.architectury.loom") version "1.14.473" apply false
    kotlin("jvm") version "2.3.10" apply false
    kotlin("plugin.serialization") version "2.3.10" apply false
    id("coffee.axle.blahaj") version "3.3.1"
    id("dev.kikugie.stonecutter") version "0.9.8"
}

rootProject.name = settings.extra["mod.name"] as String

buildscript {
    configurations.classpath {
        resolutionStrategy {
            force(
                "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1",
                "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.8.1",
                "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1",
                "org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1"
            )
        }
    }
}

blahaj {
    init(rootProject) {
        mc("1.21.4", "fabric")
        mc("1.21.5", "fabric")
        mc("1.21.8", "fabric")
        mc("1.21.10", "fabric")
        mc("1.21.11", "fabric")
        mc("26.1.2", "fabric")
        mc("26.2", "fabric")
        mc("26.3", "fabric")
    }
}
