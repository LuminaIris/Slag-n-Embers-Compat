package dev.lopyluna.slag.client.render;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.Set;
import java.util.function.Consumer;

public class CustomRenderedItems {
	private static final Set<Item> ITEMS = new ReferenceOpenHashSet<>();
	private static boolean itemsFiltered = false;

	public static void register(Item item) {
		ITEMS.add(item);
	}
	public static void forEach(Consumer<Item> consumer) {
		if (!itemsFiltered) {
            ITEMS.removeIf(item -> !BuiltInRegistries.ITEM.containsValue(item) || !(IClientItemExtensions.of(item).getCustomRenderer() instanceof CustomRenderedItemModelRenderer));
			itemsFiltered = true;
		}
		ITEMS.forEach(consumer);
	}
}
