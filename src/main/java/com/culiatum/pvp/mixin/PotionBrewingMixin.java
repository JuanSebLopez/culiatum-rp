package com.culiatum.pvp.mixin;

import com.culiatum.pvp.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
	@Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
	private static void culiatum$pvpHasMix(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
		if (isRecallRecipe(input, ingredient) || isRecallRecipe(ingredient, input)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mix", at = @At("HEAD"), cancellable = true)
	private static void culiatum$pvpMix(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
		if (isRecallRecipe(input, ingredient) || isRecallRecipe(ingredient, input)) {
			cir.setReturnValue(new ItemStack(ModItems.RECALL_POTION));
		}
	}

	private static boolean isRecallRecipe(ItemStack input, ItemStack ingredient) {
		if (!ingredient.is(Items.NETHER_STAR)) {
			return false;
		}

		if (!input.is(Items.POTION)) {
			return false;
		}

		PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
		return contents != null && contents.is(Potions.AWKWARD);
	}
}
