plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("maven-publish")
}

version = "1.0.0"
group = "dev.booky"

repositories {
    maven("https://maven.parchmentmc.org/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.20.4:2024.02.25@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:0.15.9")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 17
    }
}
