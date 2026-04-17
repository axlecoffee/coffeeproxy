plugins {
    id("net.fabricmc.fabric-loom")
}

val modVersion: String by project
val mavenGroup: String by project
val archivesBaseName: String by project
val loaderVersion: String by project
val fabricApiVersion: String by project

version = "${modVersion}+${stonecutter.current.version}"
group = mavenGroup
base { archivesName.set(archivesBaseName) }

val javaTarget = JavaVersion.VERSION_25

java {
    withSourcesJar()
    sourceCompatibility = javaTarget
    targetCompatibility = javaTarget
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

@Suppress("UnstableApiUsage")
loom {
    mixin { useLegacyMixinAp = false }
}

dependencies {
    val mcDependency = findProperty("minecraftDependency")?.toString() ?: stonecutter.current.version
    minecraft("com.mojang:minecraft:${mcDependency}")
    implementation("net.fabricmc:fabric-loader:${loaderVersion}")
    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    include(implementation("io.netty:netty-handler-proxy:4.1.118.Final")!!)
    include(implementation("io.netty:netty-codec-socks:4.1.118.Final")!!)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to inputs.properties["version"]))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaTarget.majorVersion.toInt())
}

tasks.jar {
    from("LICENSE") { rename { "${it}_${archivesBaseName}" } }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named("jar"))
    into(rootProject.layout.buildDirectory.dir("libs/${modVersion}"))
    dependsOn("build")
}
