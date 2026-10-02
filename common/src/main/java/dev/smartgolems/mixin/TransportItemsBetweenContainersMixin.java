package dev.smartgolems.mixin;

import dev.smartgolems.config.GolemConfig;
import dev.smartgolems.content.OverflowChestBlock;
import dev.smartgolems.logic.Sorting;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes copper golems know what is in every chest in range instead of walking to chests one by one to look:
 * <ul>
 * <li>picking up: the nearest copper chest that is not empty</li>
 * <li>putting down: the nearest chest that already holds the item (and has room), else the nearest Overflow Chest with room,
 * else nothing (the golem keeps the item; it never dumps it in an unrelated chest)</li>
 * </ul>
 * It also applies the configured search radius, and lets golems deliver into Overflow Chests.
 */
@Mixin(TransportItemsBetweenContainers.class)
public abstract class TransportItemsBetweenContainersMixin {
	@Shadow
	private TransportItemsBetweenContainers.@Nullable TransportItemTarget target;

	@Shadow
	private static boolean isPickingUpItems(final PathfinderMob body) {
		throw new AssertionError();
	}

	@Shadow
	private static Set<GlobalPos> getVisitedPositions(final PathfinderMob mob) {
		throw new AssertionError();
	}

	@Shadow
	private static Set<GlobalPos> getUnreachablePositions(final PathfinderMob mob) {
		throw new AssertionError();
	}

	@Shadow
	private AABB getTargetSearchArea(final PathfinderMob mob) {
		throw new AssertionError();
	}

	@Shadow
	private int getHorizontalSearchDistance(final PathfinderMob mob) {
		throw new AssertionError();
	}

	@Shadow
	private TransportItemsBetweenContainers.@Nullable TransportItemTarget isTargetValidToPick(
		final PathfinderMob body, final Level level, final BlockEntity blockEntity, final Set<GlobalPos> visitedPositions,
		final Set<GlobalPos> unreachablePositions, final AABB targetBlockSearchArea
	) {
		throw new AssertionError();
	}

	@Inject(method = "getHorizontalSearchDistance", at = @At("RETURN"), cancellable = true)
	private void smartgolems$horizontalRadius(final PathfinderMob mob, final CallbackInfoReturnable<Integer> cir) {
		if (mob instanceof CopperGolem && !mob.isPassenger()) {
			cir.setReturnValue(GolemConfig.horizontalRadius());
		}
	}

	@Inject(method = "getVerticalSearchDistance", at = @At("RETURN"), cancellable = true)
	private void smartgolems$verticalRadius(final PathfinderMob mob, final CallbackInfoReturnable<Integer> cir) {
		if (mob instanceof CopperGolem && !mob.isPassenger()) {
			cir.setReturnValue(GolemConfig.verticalRadius());
		}
	}

	/** Overflow Chests are valid places to put items down (never to pick items up from). */
	@Inject(method = "isWantedBlock", at = @At("RETURN"), cancellable = true)
	private void smartgolems$overflowChestIsADestination(final PathfinderMob mob, final BlockState block, final CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && mob instanceof CopperGolem && !isPickingUpItems(mob) && block.getBlock() instanceof OverflowChestBlock) {
			cir.setReturnValue(true);
		}
	}

	/** On arrival, an Overflow Chest accepts anything it has room for (vanilla only accepts an empty chest or one holding the item). */
	@Redirect(
		method = "doReachedTargetInteraction",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;matchesLeavingItemsRequirement(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;)Z"
		)
	)
	private boolean smartgolems$overflowChestAcceptsAnything(final PathfinderMob body, final Container container) {
		if (this.target != null && body instanceof CopperGolem && this.target.state().getBlock() instanceof OverflowChestBlock) {
			return Sorting.hasRoomFor(container, body.getMainHandItem());
		}
		// Unlike vanilla, an empty chest is not a place to leave things: only a chest that already holds the item will do.
		return Sorting.contains(container, body.getMainHandItem());
	}

	@Inject(method = "getTransportTarget", at = @At("HEAD"), cancellable = true)
	private void smartgolems$findTarget(
		final ServerLevel level, final PathfinderMob body, final CallbackInfoReturnable<Optional<TransportItemsBetweenContainers.TransportItemTarget>> cir
	) {
		if (!(body instanceof CopperGolem)) {
			return;
		}

		final boolean pickingUp = isPickingUpItems(body);
		final ItemStack held = body.getMainHandItem();
		final AABB searchArea = this.getTargetSearchArea(body);
		final Set<GlobalPos> visited = getVisitedPositions(body);
		final Set<GlobalPos> unreachable = getUnreachablePositions(body);
		final List<ChunkPos> chunks = ChunkPos.rangeClosed(ChunkPos.containing(body.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance(body), 16) + 1).toList();

		TransportItemsBetweenContainers.TransportItemTarget sourceChest = null;
		TransportItemsBetweenContainers.TransportItemTarget matchingChest = null;
		TransportItemsBetweenContainers.TransportItemTarget overflowChest = null;
		double sourceDistance = Double.MAX_VALUE;
		double matchingDistance = Double.MAX_VALUE;
		double overflowDistance = Double.MAX_VALUE;

		for (final ChunkPos chunkPos : chunks) {
			final LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
			if (chunk == null) {
				continue;
			}
			for (final BlockEntity blockEntity : chunk.getBlockEntities().values()) {
				if (!(blockEntity instanceof ChestBlockEntity chest)) {
					continue;
				}
				final double distance = chest.getBlockPos().distToCenterSqr(body.position());
				final TransportItemsBetweenContainers.TransportItemTarget candidate = this.isTargetValidToPick(body, level, chest, visited, unreachable, searchArea);
				if (candidate == null) {
					continue;
				}

				final Container container = candidate.container();
				if (pickingUp) {
					if (distance < sourceDistance && !container.isEmpty()) {
						sourceChest = candidate;
						sourceDistance = distance;
					}
				} else if (candidate.state().getBlock() instanceof OverflowChestBlock) {
					if (distance < overflowDistance && Sorting.hasRoomFor(container, held)) {
						overflowChest = candidate;
						overflowDistance = distance;
					}
				} else if (Sorting.contains(container, held)) {
					if (distance < matchingDistance && Sorting.hasRoomFor(container, held)) {
						matchingChest = candidate;
						matchingDistance = distance;
					}
				}
			}
		}

		final TransportItemsBetweenContainers.TransportItemTarget chosen;
		if (pickingUp) {
			chosen = sourceChest;
		} else if (matchingChest != null) {
			chosen = matchingChest;
		} else {
			chosen = overflowChest;
		}
		cir.setReturnValue(Optional.ofNullable(chosen));
	}
}
