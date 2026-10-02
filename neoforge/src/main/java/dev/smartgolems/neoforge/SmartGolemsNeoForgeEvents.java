package dev.smartgolems.neoforge;

import dev.smartgolems.Registrar;
import dev.smartgolems.SmartGolemsMod;
import dev.smartgolems.content.OverflowChests;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Registers the Overflow Chests (blocks first, then their items, as the registries are filled in that order) and adds them to the creative tab. */
@EventBusSubscriber(modid = SmartGolemsMod.MOD_ID)
public final class SmartGolemsNeoForgeEvents {
	private SmartGolemsNeoForgeEvents() {
	}

	@SubscribeEvent
	public static void register(final RegisterEvent event) {
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

	@SubscribeEvent
	public static void addToCreativeTab(final BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
			OverflowChests.WEATHERING_ITEMS.forEach(event::accept);
			OverflowChests.WAXED_ITEMS.forEach(event::accept);
		}
	}
}
