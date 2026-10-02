package dev.smartgolems;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

/** Registers a value in a registry; the way that happens differs between the mod loaders. */
public interface Registrar {
	<T> void register(Registry<T> registry, Identifier id, T value);
}
