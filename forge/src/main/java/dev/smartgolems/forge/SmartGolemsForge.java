package dev.smartgolems.forge;

import dev.smartgolems.Registrar;
import dev.smartgolems.SmartGolemsMod;
import dev.smartgolems.content.OverflowChests;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;

/** Forge entrypoint (client and server): loads the config, registers the Overflow Chests (blocks first, then their items) and adds them to the creative tab. */
@Mod(SmartGolemsMod.MOD_ID)
public final class SmartGolemsForge {
	public SmartGolemsForge(final FMLJavaModLoadingContext context) {
		SmartGolemsMod.init(FMLPaths.CONFIGDIR.get());

		RegisterEvent.getBus(context.getModBusGroup()).addListener(SmartGolemsForge::register);
		BuildCreativeModeTabContentsEvent.BUS.addListener(SmartGolemsForge::addToCreativeTab);
	}

	private static void register(final RegisterEvent event) {
		final Registrar registrar = new Registrar() {
			@Override
			public <T> void register(final Registry<T> registry, final Identifier id, final T value) {
				event.register(registry.key(), id, () -> value);
			}
		};
		if (event.getRegistryKey().equals(Registries.BLOCK)) {
			OverflowChests.registerBlocks(registrar);
		} else if (event.getRegistryKey().equals(Registries.ITEM)) {
			OverflowChests.registerItems(registrar);
		}
	}

	private static void addToCreativeTab(final BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
			OverflowChests.WEATHERING_ITEMS.forEach(event::accept);
			OverflowChests.WAXED_ITEMS.forEach(event::accept);
		}
	}
}
