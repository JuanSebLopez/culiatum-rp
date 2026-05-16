package com.culiatum.rp.item;

import com.culiatum.rp.CuliatumRpMod;
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
	public static final Item RECALL_POTION = register("recall_potion", new RecallPotionItem(createProperties("recall_potion")
		.stacksTo(16)
		.rarity(Rarity.RARE)));
	public static final Item RADAR = register("radar", new RadarItem(createProperties("radar")
		.stacksTo(1)
		.rarity(Rarity.UNCOMMON)));
	public static final Item HOME_SETTER = register("home_setter", new HomeSetterItem(createProperties("home_setter")
		.stacksTo(16)
		.rarity(Rarity.UNCOMMON)));
	public static final Item HANDCUFFS = register("handcuffs", new HandcuffsItem(createProperties("handcuffs")
		.stacksTo(1)
		.rarity(Rarity.UNCOMMON)));
	public static final Item POLICE_BATON = register("police_baton", new PoliceBatonItem(
		PoliceBatonItem.createProperties(createProperties("police_baton"))
	));

	private ModItems() {
	}

	private static Item.Properties createProperties(String path) {
		Identifier id = Identifier.fromNamespaceAndPath(CuliatumRpMod.MOD_ID, path);
		ResourceKey<Item> resourceKey = ResourceKey.create(Registries.ITEM, id);
		return new Item.Properties().setId(resourceKey);
	}

	private static Item register(String path, Item item) {
		Identifier id = Identifier.fromNamespaceAndPath(CuliatumRpMod.MOD_ID, path);
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(RADAR));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(HOME_SETTER));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(HANDCUFFS));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(POLICE_BATON));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> entries.accept(RECALL_POTION));
	}
}
