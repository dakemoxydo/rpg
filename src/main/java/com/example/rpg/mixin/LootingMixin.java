package com.example.rpg.mixin;

import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.stats.RpgWorldData;
import com.example.rpg.stats.StatType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class LootingMixin {

    // Без @Unique - это просто private static
    private static final Logger rpg$LOGGER = LoggerFactory.getLogger("rpg");

    @Inject(method = "getLooting", at = @At("RETURN"), cancellable = true)
    private static void rpg_addLootingBonus(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        try {
            RpgWorldData worldData = RpgWorldData.get(server);
            if (worldData == null) {
                return;
            }

            PlayerStatsData data = worldData.getPlayerData(player.getUuid());
            if (data == null) {
                return;
            }

            int rpgLooting = data.getStatLevel(StatType.LOOTING);
            if (rpgLooting > 0) {
                cir.setReturnValue(cir.getReturnValue() + rpgLooting);
            }
        } catch (Exception e) {
            rpg$LOGGER.error("Error applying RPG looting bonus", e);
        }
    }
}