package dev.smartgolems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
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

	/** Overflow Chests join into double chests with each other, whatever their oxidation or wax. */
	@Override
	public boolean chestCanConnectTo(final BlockState blockState) {
		return blockState.getBlock() instanceof OverflowChestBlock && blockState.hasProperty(ChestBlock.TYPE);
	}

	@Override
	public BlockState getStateForPlacement(final BlockPlaceContext context) {
		final BlockState state = super.getStateForPlacement(context);
		return state == null ? null : leastOxidizedOfConnected(state, context.getLevel(), context.getClickedPos());
	}

	@Override
	protected BlockState updateShape(
		final BlockState state, final LevelReader level, final ScheduledTickAccess ticks, final BlockPos pos, final Direction directionToNeighbour,
		final BlockPos neighbourPos, final BlockState neighbourState, final RandomSource random
	) {
		final BlockState updated = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
		// Both halves of a double chest always stay the same block (oxidation, wax), so when one changes the other follows it.
		if (this.chestCanConnectTo(neighbourState)
			&& updated.getValue(ChestBlock.TYPE) != ChestType.SINGLE
			&& getConnectedDirection(updated) == directionToNeighbour) {
			return neighbourState.getBlock().withPropertiesOf(updated);
		}
		return updated;
	}

	/** A new half joining an existing chest takes the less oxidized of the two; if only one is waxed, both become unwaxed. */
	private static BlockState leastOxidizedOfConnected(final BlockState state, final Level level, final BlockPos pos) {
		if (state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
			return state;
		}
		final BlockState other = level.getBlockState(pos.relative(getConnectedDirection(state)));
		if (!(state.getBlock() instanceof OverflowChestBlock mine) || !(other.getBlock() instanceof OverflowChestBlock theirs)) {
			return state;
		}
		BlockState mineState = state;
		BlockState theirState = other;
		if (mine.isWaxed() != theirs.isWaxed()) {
			mineState = mine.isWaxed() ? OverflowChests.WEATHERING.pick(mine.weatherState).withPropertiesOf(state) : state;
			theirState = theirs.isWaxed() ? OverflowChests.WEATHERING.pick(theirs.weatherState).withPropertiesOf(other) : other;
		}
		final Block least = mine.weatherState.ordinal() <= theirs.weatherState.ordinal() ? mineState.getBlock() : theirState.getBlock();
		return least.withPropertiesOf(mineState);
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
				level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
			} else if (this.weatherState != WeatherState.UNAFFECTED) {
				change(level, pos, state, OverflowChests.WEATHERING.pick(this.weatherState.previous()), player, 3005);
				level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
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
