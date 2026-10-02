plugins {
    java
    id("net.fabricmc.fabric-loom") version "1.18.2" apply false
    id("net.neoforged.moddev") version "2.0.148" apply false
}

// Fabric and NeoForge are built from the same loader-independent sources in common/ (see fabric/ and neoforge/).
configure(subprojects.filter { it.name != "common" }) {
    // Minecraft 26.x needs Java 25.
    apply(plugin = "java")

    val minecraftVersion = project.property("minecraftVersion") as String
    val modLoader = project.name

    group = rootProject.property("group") as String
    version = rootProject.property("version") as String

    base {
        // smartgolems-<loader>-<minecraft version>-<mod version>.jar
        archivesName.set("smartgolems-$modLoader-$minecraftVersion")
    }

    repositories {
        mavenCentral()
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.neoforged.net/releases/")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        withSourcesJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    tasks.named<Jar>("jar") {
        // The LGPL is an addition to the GPL, so both texts ship in the jar.
        from(rootProject.files("LICENSE", "LICENSE.GPL")) {
            rename { "${it}_smartgolems" }
        }
    }
}
