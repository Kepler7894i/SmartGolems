package dev.smartgolems.content;

import dev.smartgolems.Registrar;
import dev.smartgolems.SmartGolemsMod;
import dev.smartgolems.mixin.BlockEntityTypeAccessor;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** The Overflow Chest: eight blocks (four oxidation stages, each also waxed) and their items. */
public final class OverflowChests {
	public static final String NAME = "overflow_chest";
	/** Chest textures are overflow.png, overflow_exposed.png, ... (see assets/smartgolems/textures/entity/chest). */
	public static final String TEXTURE_PREFIX = "overflow";

	public static WeatheringCopperCollection.ByState<Block> WEATHERING;
	public static WeatheringCopperCollection.ByState<Block> WAXED;
	public static WeatheringCopperCollection.ByState<Item> WEATHERING_ITEMS;
	public static WeatheringCopperCollection.ByState<Item> WAXED_ITEMS;

	private OverflowChests() {
	}

	public static void registerBlocks(final Registrar registrar) {
		WEATHERING = createBlocks(registrar, "", WeatheringOverflowChestBlock::new);
		WAXED = createBlocks(registrar, "waxed_", OverflowChestBlock::new);

		// The overflow chests use the vanilla chest block entity, which only accepts the blocks it lists as valid.
		final BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) BlockEntityTypes.CHEST;
		final Set<Block> valid = new HashSet<>(accessor.smartgolems$getValidBlocks());
		WEATHERING.forEach(valid::add);
		WAXED.forEach(valid::add);
		accessor.smartgolems$setValidBlocks(Set.copyOf(valid));
	}

	public static void registerItems(final Registrar registrar) {
		WEATHERING_ITEMS = WEATHERING.map(block -> createItem(registrar, block));
		WAXED_ITEMS = WAXED.map(block -> createItem(registrar, block));
	}

	private static WeatheringCopperCollection.ByState<Block> createBlocks(
		final Registrar registrar, final String prefix, final BiFunction<WeatherState, BlockBehaviour.Properties, Block> factory
	) {
		return WeatheringCopperCollection.STATES.map(state -> {
			final String stage = state == WeatherState.UNAFFECTED ? "" : state.getSerializedName() + "_";
			final Identifier id = SmartGolemsMod.id(prefix + stage + NAME);
			final ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
			final BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
				.mapColor(Blocks.COPPER_BLOCK.weathering().pick(state).defaultMapColor())
				.strength(3.0F, 6.0F)
				.sound(SoundType.COPPER)
				.requiresCorrectToolForDrops()
				.setId(key);
			final Block block = factory.apply(state, properties);
			registrar.register(BuiltInRegistries.BLOCK, id, block);
			return block;
		});
	}

	private static Item createItem(final Registrar registrar, final Block block) {
		final Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		final ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		final BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
		item.registerBlocks(Item.BY_BLOCK, item);
		registrar.register(BuiltInRegistries.ITEM, id, item);
		return item;
	}
}
