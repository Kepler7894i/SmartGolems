package dev.smartgolems.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.smartgolems.SmartGolemsMod;
import dev.smartgolems.content.OverflowChestBlock;
import dev.smartgolems.content.OverflowChests;
import dev.smartgolems.client.OverflowSpriteHolder;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws Overflow Chests with their own textures (a copper chest with a black lock) instead of the copper chest ones. */
@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin {
	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
		at = @At("TAIL")
	)
	private void smartgolems$pickSprite(
		final BlockEntity blockEntity, final ChestRenderState state, final float partialTicks, final Vec3 cameraPosition,
		final ModelFeatureRenderer.CrumblingOverlay breakProgress, final CallbackInfo ci
	) {
		SpriteId sprite = null;
		if (blockEntity.getBlockState().getBlock() instanceof OverflowChestBlock chest) {
			final WeatherState weatherState = chest.getState();
			final String stage = weatherState == WeatherState.UNAFFECTED ? "" : "_" + weatherState.getSerializedName();
			final String half = switch (state.type) {
				case LEFT -> "_left";
				case RIGHT -> "_right";
				default -> "";
			};
			sprite = Sheets.CHEST_MAPPER.apply(SmartGolemsMod.id(OverflowChests.TEXTURE_PREFIX + stage + half));
		}
		((OverflowSpriteHolder) state).smartgolems$setOverflowSprite(sprite);
	}

	@ModifyVariable(
		method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("STORE"),
		ordinal = 0
	)
	private SpriteId smartgolems$useOverflowSprite(
		final SpriteId original, final ChestRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera
	) {
		final SpriteId overflow = ((OverflowSpriteHolder) state).smartgolems$getOverflowSprite();
		return overflow != null ? overflow : original;
	}
}
