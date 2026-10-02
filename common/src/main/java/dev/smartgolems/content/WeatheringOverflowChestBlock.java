package dev.smartgolems.content;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/** An Overflow Chest that oxidizes over time exactly like a copper chest (it waits while someone has it open). */
public class WeatheringOverflowChestBlock extends OverflowChestBlock implements ChangeOverTimeBlock<WeatherState> {
	public WeatheringOverflowChestBlock(final WeatherState weatherState, final BlockBehaviour.Properties properties) {
		super(weatherState, properties);
	}

	@Override
	public boolean isWaxed() {
		return false;
	}

	@Override
	protected boolean isRandomlyTicking(final BlockState state) {
		return this.getState() != WeatherState.OXIDIZED;
	}

	@Override
	protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
		// Only the left half of a double chest ages; the right half copies it (see updateShape).
		if (state.getValue(ChestBlock.TYPE) != ChestType.RIGHT
			&& level.getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.getEntitiesWithContainerOpen().isEmpty()) {
			this.changeOverTime(state, level, pos, random);
		}
	}

	@Override
	public Optional<BlockState> getNext(final BlockState state) {
		if (this.getState() == WeatherState.OXIDIZED) {
			return Optional.empty();
		}
		return Optional.of(OverflowChests.WEATHERING.pick(this.getState().next()).withPropertiesOf(state));
	}

	@Override
	public float getChanceModifier() {
		return this.getState() == WeatherState.UNAFFECTED ? 0.75F : 1.0F;
	}

	@Override
	public WeatherState getAge() {
		return this.getState();
	}
}
