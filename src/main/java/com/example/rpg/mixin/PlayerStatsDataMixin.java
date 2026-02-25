package com.example.rpg.mixin;

import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.PlayerStatsData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerStatsDataMixin implements IPlayerStatsAccessor {

    @Unique
    private PlayerStatsData rpg_statsData = new PlayerStatsData();

    @Override
    public PlayerStatsData rpg_getStatsData() {
        return rpg_statsData;
    }

    @Override
    public void rpg_setStatsData(PlayerStatsData data) {
        this.rpg_statsData = data;
    }
}