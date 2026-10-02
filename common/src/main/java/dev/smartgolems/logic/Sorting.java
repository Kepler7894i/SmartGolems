package dev.smartgolems.logic;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Questions a copper golem asks about a chest's contents before deciding to walk to it. */
public final class Sorting {
	private Sorting() {
	}

	/** Does the container already hold this kind of item (what vanilla golems check only once they have arrived)? */
	public static boolean contains(final Container container, final ItemStack stack) {
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			if (ItemStack.isSameItem(container.getItem(slot), stack)) {
				return true;
			}
		}
		return false;
	}

	/** Could at least part of the stack be added to the container, the way a golem adds items (empty slot or matching, not full, stack)? */
	public static boolean hasRoomFor(final Container container, final ItemStack stack) {
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			final ItemStack existing = container.getItem(slot);
			if (existing.isEmpty()) {
				return true;
			}
			if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
				return true;
			}
		}
		return false;
	}
}
