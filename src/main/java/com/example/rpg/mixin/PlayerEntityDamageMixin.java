package com.example.rpg.mixin;

import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.stats.RpgWorldData;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDamageMixin {

    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), argsOnly = true)
    private float modifyAppliedDamage(float amount, DamageSource source) {
        if (!source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            Object thiz = this;
            if (thiz instanceof ServerPlayerEntity serverPlayer) {
                PlayerStatsData data = RpgWorldData.get(serverPlayer.getServer()).getPlayerData(serverPlayer.getUuid());
                if (data != null) {
                    float reduction = data.getDefenseReduction();
                    if (reduction > 0) {
                        return amount * (1.0f - reduction);
                    }
                }
            }
        }
        return amount;
    }
}
