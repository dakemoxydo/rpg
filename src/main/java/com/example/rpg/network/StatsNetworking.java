package com.example.rpg.network;

import com.example.rpg.ability.*;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.effect.RpgHudRenderer;
import com.example.rpg.stats.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StatsNetworking {

    public static final Identifier UPGRADE_STAT_PACKET = new Identifier("rpg", "upgrade_stat");
    public static final Identifier SYNC_STATS_PACKET = new Identifier("rpg", "sync_stats");
    public static final Identifier SYNC_RESOURCES_PACKET = new Identifier("rpg", "sync_resources");
    public static final Identifier LEVEL_UP_PACKET = new Identifier("rpg", "level_up");
    public static final Identifier XP_POPUP_PACKET = new Identifier("rpg", "xp_popup");
    public static final Identifier RESET_STATS_PACKET = new Identifier("rpg", "reset_stats");
    public static final Identifier USE_ABILITY_PACKET = new Identifier("rpg", "use_ability");
    public static final Identifier UPGRADE_ABILITY_PACKET = new Identifier("rpg", "upgrade_ability");
    public static final Identifier SYNC_COOLDOWN_PACKET = new Identifier("rpg", "sync_cooldown");
    public static final Identifier SET_ELEMENT_PACKET = new Identifier("rpg", "set_element");
    public static final Identifier RESET_ELEMENT_PACKET = new Identifier("rpg", "reset_element");
    public static final Identifier UPGRADE_MAGIC_SKILL_PACKET = new Identifier("rpg", "upgrade_magic_skill");
    public static final Identifier USE_MAGIC_SKILL_PACKET = new Identifier("rpg", "use_magic_skill");
    public static final Identifier RESET_CLIENT_CONFIG_PACKET = new Identifier("rpg", "reset_client_config");

    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_STAT_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    final int statIndex = buf.readInt();
                    server.execute(() -> {
                        if (statIndex < 0 || statIndex >= StatType.values().length)
                            return;
                        StatType stat = StatType.values()[statIndex];
                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                        if (data.upgradeStat(stat)) {
                            worldData.markDirty();
                            StatsManager.applyStats(player, data);
                            syncToClient(player);
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(RESET_STATS_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                        int refundedPoints = data.getSpentPoints();
                        data.resetStats();
                        data.setSkillPoints(data.getSkillPoints() + refundedPoints);
                        worldData.markDirty();
                        StatsManager.applyStats(player, data);
                        syncToClient(player);
                        player.sendMessage(Text.literal("§a✓ Stats reset! §e" + refundedPoints + " §apoints refunded."),
                                false);
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(USE_ABILITY_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    final String abilityId = buf.readString();
                    server.execute(() -> {
                        if (abilityId == null || abilityId.isEmpty())
                            return;
                        Ability ability = AbilityRegistry.get(abilityId);
                        if (ability == null)
                            return;

                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                        int level = data.getAbilityLevel(abilityId);
                        if (level <= 0) {
                            player.sendMessage(Text.literal("§cYou don't have this ability!"), true);
                            return;
                        }

                        if (AbilityCooldownManager.isOnCooldown(player.getUuid(), abilityId)) {
                            int remaining = AbilityCooldownManager.getCooldown(player.getUuid(), abilityId) / 20;
                            player.sendMessage(Text.literal("§cCooldown: " + remaining + "s"), true);
                            return;
                        }

                        boolean resourceOk = ability.usesStamina()
                                ? data.useStamina(ability.getResourceCost())
                                : data.useMana(ability.getResourceCost());

                        if (!resourceOk) {
                            player.sendMessage(
                                    Text.literal(
                                            ability.usesStamina() ? "§6Not enough stamina!" : "§9Not enough mana!"),
                                    true);
                            return;
                        }

                        ability.execute(player, level);
                        AbilityCooldownManager.setCooldown(player.getUuid(), abilityId, ability.getCooldownTicks());
                        worldData.markDirty();
                        syncToClient(player);
                        syncCooldown(player, abilityId, ability.getCooldownTicks());
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_ABILITY_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    final String abilityId = buf.readString();
                    server.execute(() -> {
                        if (abilityId == null || abilityId.isEmpty())
                            return;
                        Ability ability = AbilityRegistry.get(abilityId);
                        if (ability == null)
                            return;

                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                        if (data.upgradeAbility(abilityId, ability.getMaxLevel(), ability.getCostPerLevel())) {
                            worldData.markDirty();
                            syncToClient(player);
                            player.sendMessage(Text.literal("§a✓ Ability upgraded!"), false);
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(SET_ELEMENT_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    final int elementIndex = buf.readInt();
                    server.execute(() -> {
                        if (elementIndex < 0 || elementIndex >= MagicElement.values().length)
                            return;
                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                        MagicElement element = MagicElement.values()[elementIndex];
                        data.setElement(element);
                        worldData.markDirty();
                        syncToClient(player);
                        player.sendMessage(Text.literal("§a✓ You chose " + element.getDisplayName() + "!"), false);
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(RESET_ELEMENT_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                        // 1. Считаем очки, потраченные на магию
                        int refundedPoints = data.getSpentMagicPoints();

                        // 2. Возвращаем очки игроку
                        data.setSkillPoints(data.getSkillPoints() + refundedPoints);

                        // 3. Сбрасываем стихию и все магические скиллы
                        data.resetElement();

                        worldData.markDirty();
                        syncToClient(player);

                        player.sendMessage(
                                Text.literal("§c✗ Element reset. §e" + refundedPoints + " §apoints refunded."), false);
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_MAGIC_SKILL_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    final String skillId = buf.readString();
                    server.execute(() -> {
                        if (skillId == null || skillId.isEmpty())
                            return;
                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                        MagicAbility ability = MagicSkillRegistry.getAbility(skillId);
                        if (ability == null)
                            return;

                        if (!data.meetsRequirements(ability.getRequiredSkills())) {
                            player.sendMessage(Text.literal("§cRequirements not met!"), true);
                            return;
                        }

                        if (data.upgradeMagicSkill(skillId, ability.getMaxLevel(), ability.getCostPerLevel())) {
                            worldData.markDirty();
                            syncToClient(player);
                            player.sendMessage(Text.literal("§a✓ Magic skill upgraded!"), false);
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(USE_MAGIC_SKILL_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    final String skillId = buf.readString();
                    server.execute(() -> {
                        if (skillId == null || skillId.isEmpty())
                            return;

                        MagicAbility ability = MagicSkillRegistry.getAbility(skillId);
                        if (ability == null)
                            return;

                        RpgWorldData worldData = RpgWorldData.get(server);
                        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                        if (data.getElement() != ability.getElement()) {
                            player.sendMessage(Text.literal("§cWrong element!"), true);
                            return;
                        }

                        int level = data.getMagicSkillLevel(skillId);
                        if (level <= 0) {
                            player.sendMessage(Text.literal("§cYou don't have this skill!"), true);
                            return;
                        }

                        if (ability.getManaCost() <= 0) {
                            player.sendMessage(Text.literal("§7Passive skill."), true);
                            return;
                        }

                        if (AbilityCooldownManager.isOnCooldown(player.getUuid(), skillId)) {
                            int remaining = AbilityCooldownManager.getCooldown(player.getUuid(), skillId) / 20;
                            player.sendMessage(Text.literal("§cCooldown: " + remaining + "s"), true);
                            return;
                        }

                        int manaCost = ability.getManaCost(level);
                        if (!data.useMana(manaCost)) {
                            player.sendMessage(Text.literal("§9Not enough mana! Need " + manaCost), true);
                            return;
                        }

                        ability.execute(player, level);
                        AbilityCooldownManager.setCooldown(player.getUuid(), skillId, ability.getCooldownTicks());

                        worldData.markDirty();
                        syncToClient(player);
                        syncCooldown(player, skillId, ability.getCooldownTicks());
                    });
                });
    }

    public static void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_STATS_PACKET,
                (client, handler, buf, responseSender) -> {
                    NbtCompound nbt = buf.readNbt();
                    if (nbt == null)
                        return;
                    final PlayerStatsData data = PlayerStatsData.fromNbt(nbt);
                    client.execute(() -> {
                        if (client.player != null && data != null) {
                            ((IPlayerStatsAccessor) client.player).rpg_setStatsData(data);
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_RESOURCES_PACKET,
                (client, handler, buf, responseSender) -> {
                    final float mana = buf.readFloat();
                    final float stamina = buf.readFloat();
                    client.execute(() -> {
                        if (client.player != null) {
                            PlayerStatsData data = ((IPlayerStatsAccessor) client.player).rpg_getStatsData();
                            if (data != null) {
                                data.setCurrentMana(mana);
                                data.setCurrentStamina(stamina);
                            }
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(LEVEL_UP_PACKET,
                (client, handler, buf, responseSender) -> {
                    final int newLevel = buf.readInt();
                    client.execute(() -> RpgHudRenderer.triggerLevelUp(newLevel));
                });

        ClientPlayNetworking.registerGlobalReceiver(XP_POPUP_PACKET,
                (client, handler, buf, responseSender) -> {
                    final int xpAmount = buf.readInt();
                    final String source = buf.readString();
                    client.execute(() -> RpgHudRenderer.addXpPopup(xpAmount, source));
                });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_COOLDOWN_PACKET,
                (client, handler, buf, responseSender) -> {
                    final String abilityId = buf.readString();
                    final int cooldownTicks = buf.readInt();
                    client.execute(() -> {
                        if (client.player != null) {
                            AbilityCooldownManager.setCooldown(client.player.getUuid(), abilityId, cooldownTicks);
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(RESET_CLIENT_CONFIG_PACKET,
                (client, handler, buf, responseSender) -> {
                    client.execute(() -> {
                        com.example.rpg.config.RpgConfig.get().clearAllKeybinds();
                    });
                });
    }

    // CLIENT -> SERVER
    public static void sendUpgradeRequest(StatType stat) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(stat.ordinal());
        ClientPlayNetworking.send(UPGRADE_STAT_PACKET, buf);
    }

    public static void sendResetRequest() {
        ClientPlayNetworking.send(RESET_STATS_PACKET, PacketByteBufs.create());
    }

    public static void sendUseAbility(String abilityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(abilityId);
        ClientPlayNetworking.send(USE_ABILITY_PACKET, buf);
    }

    public static void sendUpgradeAbility(String abilityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(abilityId);
        ClientPlayNetworking.send(UPGRADE_ABILITY_PACKET, buf);
    }

    public static void sendSetElement(MagicElement element) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(element.ordinal());
        ClientPlayNetworking.send(SET_ELEMENT_PACKET, buf);
    }

    public static void sendResetElement() {
        ClientPlayNetworking.send(RESET_ELEMENT_PACKET, PacketByteBufs.create());
    }

    public static void sendUpgradeMagicSkill(String skillId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(skillId);
        ClientPlayNetworking.send(UPGRADE_MAGIC_SKILL_PACKET, buf);
    }

    public static void sendUseMagicSkill(String skillId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(skillId);
        ClientPlayNetworking.send(USE_MAGIC_SKILL_PACKET, buf);
    }

    // SERVER -> CLIENT
    public static void syncToClient(ServerPlayerEntity player) {
        RpgWorldData worldData = RpgWorldData.get(player.getServer());
        PlayerStatsData data = worldData.getPlayerData(player.getUuid());
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(data.toNbt());
        ServerPlayNetworking.send(player, SYNC_STATS_PACKET, buf);
    }

    public static void syncResourcesToClient(ServerPlayerEntity player) {
        RpgWorldData worldData = RpgWorldData.get(player.getServer());
        PlayerStatsData data = worldData.getPlayerData(player.getUuid());
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(data.getCurrentMana());
        buf.writeFloat(data.getCurrentStamina());
        ServerPlayNetworking.send(player, SYNC_RESOURCES_PACKET, buf);
    }

    public static void sendLevelUpEffect(ServerPlayerEntity player, int newLevel) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(newLevel);
        ServerPlayNetworking.send(player, LEVEL_UP_PACKET, buf);
    }

    public static void sendXpPopup(ServerPlayerEntity player, int xpAmount, String source) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(xpAmount);
        buf.writeString(source);
        ServerPlayNetworking.send(player, XP_POPUP_PACKET, buf);
    }

    public static void syncCooldown(ServerPlayerEntity player, String abilityId, int cooldownTicks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(abilityId);
        buf.writeInt(cooldownTicks);
        ServerPlayNetworking.send(player, SYNC_COOLDOWN_PACKET, buf);
    }

    public static void sendResetConfigToClient(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, RESET_CLIENT_CONFIG_PACKET, PacketByteBufs.create());
    }
}