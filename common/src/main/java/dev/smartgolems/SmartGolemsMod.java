package dev.smartgolems;

import dev.smartgolems.config.GolemConfig;
import java.nio.file.Path;
import net.minecraft.resources.Identifier;

public final class SmartGolemsMod {
	public static final String MOD_ID = "smartgolems";

	private SmartGolemsMod() {
	}

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	/** Called by each loader's entrypoint with the loader's config directory. */
	public static void init(final Path configDir) {
		GolemConfig.load(configDir.resolve(MOD_ID + ".json"));
	}
}
