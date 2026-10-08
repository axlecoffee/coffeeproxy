// SPDX-FileCopyrightText: 2026 Axle Duggan (axlecoffee) <contact@axle.coffee>
//
// SPDX-License-Identifier: CC0-1.0
plugins {
    id("coffee.axle.blahaj")
}

blahaj {
    config {}
    setup {
    }
}

// god knows
tasks.named("distTar") { enabled = false }
tasks.named("distZip") { enabled = false }

dependencies {
    include(implementation("io.netty:netty-handler-proxy:4.1.118.Final")!!)
    include(implementation("io.netty:netty-codec-socks:4.1.118.Final")!!)
    include(implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")!!)
    include(implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.8.1")!!)
    include(implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")!!)
}