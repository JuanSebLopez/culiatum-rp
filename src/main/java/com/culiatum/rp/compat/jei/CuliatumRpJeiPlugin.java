package com.culiatum.rp.compat.jei;

import com.culiatum.rp.CuliatumRpMod;
import com.culiatum.rp.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.List;

@JeiPlugin
public final class CuliatumRpJeiPlugin implements IModPlugin {
	private static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(CuliatumRpMod.MOD_ID, "jei_plugin");
	private static final Identifier RECALL_BREWING_UID = Identifier.fromNamespaceAndPath(CuliatumRpMod.MOD_ID, "recall_potion_brewing");

	@Override
	public Identifier getPluginUid() {
		return PLUGIN_UID;
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		IJeiBrewingRecipe recipe = registration.getVanillaRecipeFactory().createBrewingRecipe(
			List.of(new ItemStack(Items.NETHER_STAR)),
			createAwkwardPotion(),
			new ItemStack(ModItems.RECALL_POTION),
			RECALL_BREWING_UID
		);
		registration.addRecipes(RecipeTypes.BREWING, List.of(recipe));
	}

	private static ItemStack createAwkwardPotion() {
		return PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
	}
}
