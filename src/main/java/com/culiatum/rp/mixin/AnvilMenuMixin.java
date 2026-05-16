package com.culiatum.rp.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
	@Shadow
	@Final
	private DataSlot cost;

	public AnvilMenuMixin(net.minecraft.world.inventory.MenuType<?> menuType, int containerId, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.inventory.ContainerLevelAccess access, net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition slotDefinition) {
		super(menuType, containerId, inventory, access, slotDefinition);
	}

	@Inject(method = "createResult", at = @At("TAIL"))
	private void culiatum$allowCreativeAnvilLevels(CallbackInfo ci) {
		ItemStack baseStack = this.inputSlots.getItem(0);
		ItemStack additionStack = this.inputSlots.getItem(1);

		if (baseStack.isEmpty() || additionStack.isEmpty()) {
			return;
		}

		ItemStack resultStack = this.resultSlots.getItem(0);
		ItemStack workingResult = resultStack.isEmpty() ? baseStack.copy() : resultStack.copy();
		boolean changed = mergeCreativeLevels(baseStack, additionStack, workingResult, DataComponents.ENCHANTMENTS);
		changed |= mergeCreativeLevels(baseStack, additionStack, workingResult, DataComponents.STORED_ENCHANTMENTS);

		if (!changed) {
			return;
		}

		this.resultSlots.setItem(0, workingResult);
		if (this.cost.get() <= 0) {
			this.cost.set(1);
		}
	}

	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void culiatum$removeAnvilPickupCap(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
		if (this.cost.get() <= 0) {
			cir.setReturnValue(false);
			return;
		}

		cir.setReturnValue(player.hasInfiniteMaterials() || player.experienceLevel >= this.cost.get());
	}

	private static boolean mergeCreativeLevels(ItemStack baseStack, ItemStack additionStack, ItemStack resultStack, DataComponentType<ItemEnchantments> componentType) {
		ItemEnchantments baseEnchantments = baseStack.getOrDefault(componentType, ItemEnchantments.EMPTY);
		ItemEnchantments additionEnchantments = additionStack.getOrDefault(componentType, ItemEnchantments.EMPTY);
		ItemEnchantments resultEnchantments = resultStack.getOrDefault(componentType, ItemEnchantments.EMPTY);

		if (additionEnchantments.isEmpty()) {
			return false;
		}

		ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(resultEnchantments);
		boolean changed = false;

		for (Holder<Enchantment> enchantmentHolder : additionEnchantments.keySet()) {
			int additionLevel = additionEnchantments.getLevel(enchantmentHolder);
			int baseLevel = baseEnchantments.getLevel(enchantmentHolder);
			int resultLevel = resultEnchantments.getLevel(enchantmentHolder);
			int maxVanillaLevel = enchantmentHolder.value().getMaxLevel();

			if (additionLevel <= maxVanillaLevel || additionLevel <= baseLevel || additionLevel <= resultLevel) {
				continue;
			}

			mutableEnchantments.set(enchantmentHolder, additionLevel);
			changed = true;
		}

		if (changed) {
			resultStack.set(componentType, mutableEnchantments.toImmutable());
		}

		return changed;
	}
}
