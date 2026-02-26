package com.example.rpg.mixin;

import com.example.rpg.network.StatsNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamagePre(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // Оставим метод, если в будущем снова понадобится считывать ХП до удара
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamagePost(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Нас интересует только сервер и случаи когда был нанесен реальный урон
        // (cir.getReturnValue() = true)
        if (!entity.getWorld().isClient() && cir.getReturnValueZ()) {
            // Используем 'amount' - это сырой, 100% правдивый урон до сокращений броней
            // и до обрезания под остаток здоровья жертвы (overkill)
            if (amount > 0.01f) {
                ServerWorld world = (ServerWorld) entity.getWorld();

                // Засчитываем комбо атакующему игроку
                if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
                    StatsNetworking.sendComboDamage(attacker, amount);
                }

                // Рассылаем пакет всем игрокам вокруг
                for (ServerPlayerEntity player : world.getPlayers()) {
                    // Жертве (самому себе) не показываем цифры отлетающего от нее самой урона,
                    // чтобы не было спама на экране
                    if (player.equals(entity)) {
                        continue;
                    }

                    // Если игрок достаточно близко, чтобы видеть
                    if (player.squaredDistanceTo(entity) < 64 * 64) {
                        StatsNetworking.sendDamageText(
                                player,
                                entity.getX(),
                                entity.getY() + entity.getHeight() * 0.5,
                                entity.getZ(),
                                amount);
                    }
                }
            }
        }
    }
}
