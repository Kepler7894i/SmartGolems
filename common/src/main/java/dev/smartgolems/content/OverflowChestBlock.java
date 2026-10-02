package dev.smartgolems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The waxed Overflow Chest: a single chest (it never joins up with a neighbour into a double chest) where copper golems leave items
 * that have no matching chest. It is deliberately not in the {@code copper_chests} tag, so golems never take items out of it.
 * See {@link WeatheringOverflowChestBlock} for the variants that oxidize.
 */
public class OverflowChestBlock extends ChestBlock {
	private final WeatherState weatherState;

	public OverflowChestBlock(final WeatherState weatherState, final BlockBehaviour.Properties properties) {
		super(() -> BlockEntityTypes.CHEST, CopperChestBlock.getHingeSound(weatherState, true), CopperChestBlock.getHingeSound(weatherState, false), properties);
		this.weatherState = weatherState;
	}

	public WeatherState getState() {
		return this.weatherState;
	}

	public boolean isWaxed() {
		return true;
	}

	@Override
	public boolean chestCanConnectTo(final BlockState blockState) {
		return false;
	}

	@Override
	public boolean shouldChangedStateKeepBlockEntity(final BlockState oldState) {
		return oldState.getBlock() instanceof OverflowChestBlock;
	}

	@Override
	protected InteractionResult useItemOn(
		final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit
	) {
		if (stack.is(Items.HONEYCOMB) && !this.isWaxed()) {
			change(level, pos, state, OverflowChests.WAXED.pick(this.weatherState), player, 3003);
			level.playSound(player, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (stack.is(ItemTags.AXES)) {
			if (this.isWaxed()) {
				change(level, pos, state, OverflowChests.WEATHERING.pick(this.weatherState), player, 3004);
				level.playSound(player, pos, SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
			} else if (this.weatherState != WeatherState.UNAFFECTED) {
				change(level, pos, state, OverflowChests.WEATHERING.pick(this.weatherState.previous()), player, 3005);
				level.playSound(player, pos, SoundEvents.AXE_SCRAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
			} else {
				return super.useItemOn(stack, state, level, pos, player, hand, hit);
			}
			stack.hurtAndBreak(1, player, hand);
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	private static void change(final Level level, final BlockPos pos, final BlockState state, final Block to, final Player player, final int levelEvent) {
		final BlockState newState = to.withPropertiesOf(state);
		level.setBlock(pos, newState, 11);
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
		level.levelEvent(player, levelEvent, pos, 0);
	}
}
