package dev.smartgolems.client;

import net.minecraft.client.resources.model.sprite.SpriteId;
import org.jspecify.annotations.Nullable;

/** Added to the chest render state: the texture an Overflow Chest is drawn with (null for every other chest). */
public interface OverflowSpriteHolder {
	@Nullable SpriteId smartgolems$getOverflowSprite();

	void smartgolems$setOverflowSprite(@Nullable SpriteId sprite);
}
