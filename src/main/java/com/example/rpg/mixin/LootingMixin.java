package com.example.rpg.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class LootingMixin {

    // Без @Unique - это просто private static
    // Logger removed

    @Inject(method = "getLooting", at = @At("RETURN"), cancellable = true)
    private static void rpg_addLootingBonus(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        // Obsolete: Looting stat removed.
        // Can be reimplemented via passive skill tree later.
    }
}