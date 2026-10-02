package dev.smartgolems.fabric;

import dev.smartgolems.Registrar;
import dev.smartgolems.SmartGolemsMod;
import dev.smartgolems.content.OverflowChests;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;

/** Fabric entrypoint (client and server): registers the Overflow Chests and loads the config. */
public class SmartGolemsFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		SmartGolemsMod.init(FabricLoader.getInstance().getConfigDir());

		final Registrar registrar = new Registrar() {
			@Override
			public <T> void register(final Registry<T> registry, final Identifier id, final T value) {
				Registry.register(registry, id, value);
			}
		};
		OverflowChests.registerBlocks(registrar);
		OverflowChests.registerItems(registrar);

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
			OverflowChests.WEATHERING_ITEMS.forEach(output::accept);
			OverflowChests.WAXED_ITEMS.forEach(output::accept);
		});
	}
}
