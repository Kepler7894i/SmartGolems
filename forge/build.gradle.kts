plugins {
    id("net.minecraftforge.gradle")
}

val forgeVersion = project.property("forgeVersion") as String
val minecraftVersion = project.property("minecraftVersion") as String

sourceSets.main {
    java.srcDir("../common/src/main/java")
    resources.srcDir("../common/src/main/resources")
}

minecraft {
    runs {
        configureEach {
            workingDir = layout.projectDirectory.dir("run")
            // Forge has no [[mixins]] entry in mods.toml: Mixin needs the config on the command line in a dev run...
            args("--mixin.config=smartgolems.mixins.json")
        }
        register("client")
        register("server") {
            args("--nogui")
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:$forgeVersion"))
}

// ...and in the jar manifest in a release.
tasks.named<Jar>("jar") {
    manifest {
        attributes["MixinConfigs"] = "smartgolems.mixins.json"
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // Plain values captured here (not script properties) so the configuration cache can serialize the task.
    val expansions = mapOf(
        "version" to project.version.toString(),
        "minecraft_version" to minecraftVersion,
        "forge_version" to forgeVersion.substringAfter('-').substringBefore('.'),
    )
    inputs.properties(expansions)
    filesMatching("META-INF/mods.toml") { expand(expansions) }
}
