package dev.smartgolems.mixin.client;

import dev.smartgolems.client.OverflowSpriteHolder;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChestRenderState.class)
public abstract class ChestRenderStateMixin implements OverflowSpriteHolder {
	@Unique
	private @Nullable SpriteId smartgolems$overflowSprite;

	@Override
	public @Nullable SpriteId smartgolems$getOverflowSprite() {
		return this.smartgolems$overflowSprite;
	}

	@Override
	public void smartgolems$setOverflowSprite(final @Nullable SpriteId sprite) {
		this.smartgolems$overflowSprite = sprite;
	}
}
