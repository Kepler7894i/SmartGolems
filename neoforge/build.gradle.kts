plugins {
    id("net.neoforged.moddev")
}

val minecraftVersion = project.property("minecraftVersion") as String
val neoforgeVersion = project.property("neoforgeVersion") as String

sourceSets.main {
    java.srcDir("../common/src/main/java")
    resources.srcDir("../common/src/main/resources")
}

neoForge {
    version = neoforgeVersion

    runs {
        create("client") {
            client()
            // ./gradlew :neoforge:runClient -PquickPlay=127.0.0.1:25565 joins a server straight away (for testing).
            if (project.hasProperty("quickPlay")) {
                programArguments.addAll("--quickPlayMultiplayer", project.property("quickPlay") as String, "--username", "Tester")
            }
        }
        create("server") { server() }
    }

    mods {
        create("smartgolems") { sourceSet(sourceSets.main.get()) }
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // Plain values captured here (not script properties) so the configuration cache can serialize the task.
    val expansions = mapOf(
        "version" to project.version.toString(),
        "minecraft_version" to minecraftVersion,
    )
    inputs.properties(expansions)
    filesMatching("META-INF/neoforge.mods.toml") { expand(expansions) }
}
