package com.culiatum.pvp.item;

import com.culiatum.pvp.CuliatumPvpMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class ModItems {
	public static final Item RECALL_POTION = register("recall_potion", new RecallPotionItem(new Item.Properties()
		.stacksTo(16)
		.rarity(Rarity.RARE)));
	public static final Item RADAR = register("radar", new RadarItem(new Item.Properties()
		.stacksTo(1)
		.rarity(Rarity.UNCOMMON)));

	private ModItems() {
	}

	private static Item register(String path, Item item) {
		Identifier id = Identifier.fromNamespaceAndPath(CuliatumPvpMod.MOD_ID, path);
		ResourceKey<Item> resourceKey = ResourceKey.create(Registries.ITEM, id);
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(RADAR));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> entries.accept(RECALL_POTION));
	}
}
