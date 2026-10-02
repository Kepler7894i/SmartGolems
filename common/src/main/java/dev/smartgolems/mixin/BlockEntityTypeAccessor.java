package dev.smartgolems.mixin;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mutable;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
	@Accessor("validBlocks")
	Set<Block> smartgolems$getValidBlocks();

	@Mutable
	@Accessor("validBlocks")
	void smartgolems$setValidBlocks(Set<Block> validBlocks);
}
