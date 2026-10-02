package dev.smartgolems.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;

/**
 * Copper golem search radius, read from {@code config/smartgolems.json} when the game starts.
 * The defaults are vanilla's values (32 blocks sideways, 8 up/down from the golem).
 */
public final class GolemConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final int DEFAULT_HORIZONTAL_RADIUS = 32;
	public static final int DEFAULT_VERTICAL_RADIUS = 8;
	private static final int MAX_HORIZONTAL_RADIUS = 128;
	private static final int MAX_VERTICAL_RADIUS = 64;

	private static volatile int horizontalRadius = DEFAULT_HORIZONTAL_RADIUS;
	private static volatile int verticalRadius = DEFAULT_VERTICAL_RADIUS;

	private GolemConfig() {
	}

	/** How far sideways (blocks) a copper golem looks for and walks to chests. */
	public static int horizontalRadius() {
		return horizontalRadius;
	}

	/** How far up and down (blocks) a copper golem looks for and walks to chests. */
	public static int verticalRadius() {
		return verticalRadius;
	}

	public static void load(final Path file) {
		int horizontal = DEFAULT_HORIZONTAL_RADIUS;
		int vertical = DEFAULT_VERTICAL_RADIUS;
		boolean rewrite = true;
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				final JsonObject json = GSON.fromJson(reader, JsonObject.class);
				horizontal = read(json, "horizontalRadius", DEFAULT_HORIZONTAL_RADIUS, MAX_HORIZONTAL_RADIUS);
				vertical = read(json, "verticalRadius", DEFAULT_VERTICAL_RADIUS, MAX_VERTICAL_RADIUS);
				rewrite = !json.has("horizontalRadius") || !json.has("verticalRadius");
			} catch (final IOException | RuntimeException e) {
				LOGGER.warn("Could not read {}, using the defaults", file, e);
			}
		}
		horizontalRadius = horizontal;
		verticalRadius = vertical;
		if (rewrite) {
			write(file);
		}
		LOGGER.info("Copper golems search {} blocks sideways and {} blocks up/down", horizontal, vertical);
	}

	private static int read(final JsonObject json, final String key, final int fallback, final int max) {
		if (json == null || !json.has(key)) {
			return fallback;
		}
		return Math.clamp(json.get(key).getAsInt(), 1, max);
	}

	private static void write(final Path file) {
		final JsonObject json = new JsonObject();
		json.addProperty("_comment", "How far (in blocks) a copper golem looks for chests and walks to them. Vanilla is 32 sideways and 8 up/down. Restart the game/server after changing.");
		json.addProperty("horizontalRadius", horizontalRadius);
		json.addProperty("verticalRadius", verticalRadius);
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(json, writer);
			}
		} catch (final IOException e) {
			LOGGER.warn("Could not write {}", file, e);
		}
	}
}
