package com.example.rpg.network;

import com.example.rpg.ability.*;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.effect.RpgHudRenderer;
import com.example.rpg.stats.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сетевой слой RPG-мода. Обрабатывает все пакеты между клиентом и сервером.
 * После рефакторинга: USE_ABILITY_PACKET — единый для всех способностей (физ. и
 * маг.),
 * UPGRADE_ABILITY_PACKET — единый для всех прокачек.
 */
public class StatsNetworking {

    private static final Logger LOGGER = LoggerFactory.getLogger("rpg");

    // ==================== PACKET IDs ====================

    private static final Identifier UPGRADE_STAT = new Identifier("rpg", "upgrade_stat");
    private static final Identifier SYNC_STATS = new Identifier("rpg", "sync_stats");
    private static final Identifier SYNC_RESOURCES = new Identifier("rpg", "sync_resources");
    private static final Identifier LEVEL_UP = new Identifier("rpg", "level_up");
    private static final Identifier XP_POPUP = new Identifier("rpg", "xp_popup");
    private static final Identifier SPAWN_DAMAGE_TEXT = new Identifier("rpg", "spawn_damage_text");
    private static final Identifier ADD_COMBO_DAMAGE = new Identifier("rpg", "add_combo_damage");
    private static final Identifier RESET_STATS = new Identifier("rpg", "reset_stats");
    private static final Identifier USE_ABILITY = new Identifier("rpg", "use_ability");
    private static final Identifier UPGRADE_ABILITY = new Identifier("rpg", "upgrade_ability");
    private static final Identifier SYNC_COOLDOWN = new Identifier("rpg", "sync_cooldown");
    private static final Identifier SET_ELEMENT = new Identifier("rpg", "set_element");
    private static final Identifier RESET_ELEMENT = new Identifier("rpg", "reset_element");
    private static final Identifier RESET_CLIENT_CONFIG = new Identifier("rpg", "reset_client_config");
    private static final Identifier UPDATE_SETTINGS = new Identifier("rpg", "update_settings");

    // ==================== SERVER HANDLERS ====================

    public static void registerServerHandlers() {
        // -- Upgrade Stat --
        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_STAT, (server, player, handler, buf, responseSender) -> {
            String statId = buf.readString();
            server.execute(() -> {
                AttributeStat stat = StatRegistry.get(statId);
                if (stat == null) {
                    LOGGER.warn("Unknown stat: {}", statId);
                    return;
                }
                RpgWorldData worldData = RpgWorldData.get(player.getServer());
                PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                if (data.upgradeStat(statId)) {
                    worldData.markDirty();
                    StatsManager.applyStats(player, data);
                    syncToClient(player);
                }
            });
        });

        // -- Reset Stats --
        ServerPlayNetworking.registerGlobalReceiver(RESET_STATS, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                RpgWorldData worldData = RpgWorldData.get(player.getServer());
                PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                int refund = data.getSpentPoints();
                data.resetStats();
                data.setSkillPoints(data.getSkillPoints() + refund);
                worldData.markDirty();
                StatsManager.applyStats(player, data);
                syncToClient(player);
            });
        });

        // -- Единый USE_ABILITY (физические + магические) --
        ServerPlayNetworking.registerGlobalReceiver(USE_ABILITY, (server, player, handler, buf, responseSender) -> {
            String abilityId = buf.readString();
            server.execute(() -> handleUseAbility(player, abilityId));
        });

        // -- Единый UPGRADE_ABILITY (физические + магические) --
        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_ABILITY, (server, player, handler, buf, responseSender) -> {
            String abilityId = buf.readString();
            server.execute(() -> handleUpgradeAbility(player, abilityId));
        });

        // -- Set Element --
        ServerPlayNetworking.registerGlobalReceiver(SET_ELEMENT, (server, player, handler, buf, responseSender) -> {
            String elementName = buf.readString();
            server.execute(() -> {
                try {
                    MagicElement el = MagicElement.valueOf(elementName.toUpperCase());
                    RpgWorldData worldData = RpgWorldData.get(player.getServer());
                    PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                    if (!data.hasElement()) {
                        data.setElement(el);
                        // Больше не выдаём базовый навык автоматически. Игрок должен купить его сам.
                        // MagicAbility baseAbility = AbilityRegistry.getBaseAbility(el);
                        // if (baseAbility != null) {
                        // data.setSkillLevel(baseAbility.getId(), 1);
                        // }
                        worldData.markDirty();
                        syncToClient(player);
                    }
                } catch (Exception e) {
                    LOGGER.error("Invalid element name: {}", elementName, e);
                }
            });
        });

        // -- Reset Element --
        ServerPlayNetworking.registerGlobalReceiver(RESET_ELEMENT, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                RpgWorldData worldData = RpgWorldData.get(player.getServer());
                PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                int refund = data.getSpentMagicPoints();
                data.resetElement();
                data.setSkillPoints(data.getSkillPoints() + refund);
                worldData.markDirty();
                syncToClient(player);
            });
        });

        // -- Update Settings (language, keybinds) --
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_SETTINGS, (server, player, handler, buf, responseSender) -> {
            String settingType = buf.readString();
            String settingValue = buf.readString();
            server.execute(() -> {
                RpgWorldData worldData = RpgWorldData.get(player.getServer());
                PlayerStatsData data = worldData.getPlayerData(player.getUuid());
                switch (settingType) {
                    case "language" -> data.setLanguage(settingValue);
                    case "openMenuKey" -> data.setOpenMenuKey(Integer.parseInt(settingValue));
                    case "keybind" -> {
                        String[] parts = settingValue.split(":", 2);
                        if (parts.length == 2) {
                            data.setKeybind(parts[0], Integer.parseInt(parts[1]));
                        }
                    }
                    case "clearKeybinds" -> data.clearAllKeybinds();
                }
                worldData.markDirty();
                syncToClient(player);
            });
        });
    }

    // ==================== UNIFIED ABILITY HANDLERS ====================

    /**
     * Единый обработчик использования способности.
     * Работает и для физических (Dash, Heal), и для магических (Fireball, Gust,
     * etc.).
     */
    private static void handleUseAbility(ServerPlayerEntity player, String abilityId) {
        IAbility ability = AbilityRegistry.get(abilityId);
        if (ability == null) {
            LOGGER.warn("Unknown ability: {}", abilityId);
            return;
        }

        RpgWorldData worldData = RpgWorldData.get(player.getServer());
        PlayerStatsData data = worldData.getPlayerData(player.getUuid());
        int level = data.getSkillLevel(abilityId);

        if (level <= 0)
            return;

        // Для магических способностей — проверяем стихию
        if (ability instanceof MagicAbility magic) {
            if (data.getElement() != magic.getElement())
                return;
        }

        // Проверяем кулдаун
        if (AbilityCooldownManager.isOnCooldown(player.getUuid(), abilityId))
            return;

        // Проверяем и тратим ресурс
        int cost = ability.getResourceCost(level);
        if (ability.usesStamina()) {
            if (!data.useStamina(cost)) {
                player.sendMessage(Text.literal("§cNot enough stamina!"), true);
                return;
            }
        } else {
            if (!data.useMana(cost)) {
                player.sendMessage(Text.literal("§cNot enough mana!"), true);
                return;
            }
        }

        // Выполняем
        ability.execute(player, level);

        // Ставим КД и сохраняем
        AbilityCooldownManager.setCooldown(player.getUuid(), abilityId, ability.getCooldownTicks(level, data));
        worldData.markDirty();
        syncResources(player);
        syncCooldown(player, abilityId, ability.getCooldownTicks(level, data));
    }

    /**
     * Единый обработчик прокачки способности.
     */
    private static void handleUpgradeAbility(ServerPlayerEntity player, String abilityId) {
        IAbility ability = AbilityRegistry.get(abilityId);
        if (ability == null) {
            LOGGER.warn("Unknown ability for upgrade: {}", abilityId);
            return;
        }

        // Для магии — проверяем стихию и требования
        if (ability instanceof MagicAbility magic) {
            RpgWorldData worldData = RpgWorldData.get(player.getServer());
            PlayerStatsData data = worldData.getPlayerData(player.getUuid());
            if (data.getElement() != magic.getElement())
                return;
            if (!data.meetsRequirements(magic.getRequiredSkills()))
                return;
        }

        RpgWorldData worldData = RpgWorldData.get(player.getServer());
        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

        if (data.upgradeSkill(abilityId, ability.getMaxLevel(), ability.getCostPerLevel())) {
            worldData.markDirty();
            syncToClient(player);
        }
    }

    // ==================== CLIENT HANDLERS ====================

    public static void registerClientHandlers() {
        // Sync Stats
        ClientPlayNetworking.registerGlobalReceiver(SYNC_STATS, (client, handler, buf, responseSender) -> {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            client.execute(() -> {
                if (client.player instanceof IPlayerStatsAccessor accessor) {
                    PacketByteBuf readBuf = PacketByteBufs.create();
                    readBuf.writeBytes(data);
                    PlayerStatsData statsData = PlayerStatsData.fromNbt(readBuf.readNbt());
                    accessor.rpg_setStatsData(statsData);
                }
            });
        });

        // Sync Resources
        ClientPlayNetworking.registerGlobalReceiver(SYNC_RESOURCES, (client, handler, buf, responseSender) -> {
            float mana = buf.readFloat();
            float stamina = buf.readFloat();
            client.execute(() -> {
                if (client.player instanceof IPlayerStatsAccessor accessor) {
                    PlayerStatsData data = accessor.rpg_getStatsData();
                    data.setCurrentMana(mana);
                    data.setCurrentStamina(stamina);
                }
            });
        });

        // Level Up Effect
        ClientPlayNetworking.registerGlobalReceiver(LEVEL_UP, (client, handler, buf, responseSender) -> {
            int level = buf.readInt();
            client.execute(() -> RpgHudRenderer.triggerLevelUp(level));
        });

        // XP Popup
        ClientPlayNetworking.registerGlobalReceiver(XP_POPUP, (client, handler, buf, responseSender) -> {
            int amount = buf.readInt();
            String source = buf.readString();
            client.execute(() -> RpgHudRenderer.addXpPopup(amount, source));
        });

        // Damage Text
        ClientPlayNetworking.registerGlobalReceiver(SPAWN_DAMAGE_TEXT, (client, handler, buf, responseSender) -> {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float amount = buf.readFloat();
            client.execute(() -> com.example.rpg.effect.DamageTextManager.spawnDamageText(x, y, z, amount));
        });

        // Add Combo Damage
        ClientPlayNetworking.registerGlobalReceiver(ADD_COMBO_DAMAGE, (client, handler, buf, responseSender) -> {
            float amount = buf.readFloat();
            client.execute(() -> RpgHudRenderer.addComboDamage(amount));
        });

        // Sync Cooldown
        ClientPlayNetworking.registerGlobalReceiver(SYNC_COOLDOWN, (client, handler, buf, responseSender) -> {
            String abilityId = buf.readString();
            int ticks = buf.readInt();
            client.execute(() -> {
                if (client.player != null) {
                    AbilityCooldownManager.setCooldown(client.player.getUuid(), abilityId, ticks);
                }
            });
        });

        // Reset Client Config
        ClientPlayNetworking.registerGlobalReceiver(RESET_CLIENT_CONFIG, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                com.example.rpg.config.RpgConfig.get().clearAllKeybinds();
            });
        });
    }

    // ==================== CLIENT → SERVER SENDERS ====================

    /** Отправить запрос на прокачку стата */
    public static void sendUpgradeRequest(String statId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(statId);
        ClientPlayNetworking.send(UPGRADE_STAT, buf);
    }

    /** Отправить запрос на сброс статов */
    public static void sendResetRequest() {
        ClientPlayNetworking.send(RESET_STATS, PacketByteBufs.create());
    }

    /** Единый метод: использование любой способности (физ. или маг.) */
    public static void sendUseAbility(String abilityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(abilityId);
        ClientPlayNetworking.send(USE_ABILITY, buf);
    }

    /** Единый метод: прокачка любой способности (физ. или маг.) */
    public static void sendUpgradeAbility(String abilityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(abilityId);
        ClientPlayNetworking.send(UPGRADE_ABILITY, buf);
    }

    /** Выбор стихии */
    public static void sendSetElement(MagicElement element) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(element.name());
        ClientPlayNetworking.send(SET_ELEMENT, buf);
    }

    /** Сброс стихии */
    public static void sendResetElement() {
        ClientPlayNetworking.send(RESET_ELEMENT, PacketByteBufs.create());
    }

    /** Обновить настройку игрока */
    public static void sendUpdateSetting(String type, String value) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(type);
        buf.writeString(value);
        ClientPlayNetworking.send(UPDATE_SETTINGS, buf);
    }

    // ==================== SERVER → CLIENT SENDERS ====================

    /** Полная синхронизация данных игрока */
    public static void syncToClient(ServerPlayerEntity player) {
        RpgWorldData worldData = RpgWorldData.get(player.getServer());
        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(data.toNbt());
        ServerPlayNetworking.send(player, SYNC_STATS, buf);
    }

    /** Синхронизация только ресурсов (мана/стамина) */
    public static void syncResources(ServerPlayerEntity player) {
        RpgWorldData worldData = RpgWorldData.get(player.getServer());
        PlayerStatsData data = worldData.getPlayerData(player.getUuid());

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(data.getCurrentMana());
        buf.writeFloat(data.getCurrentStamina());
        ServerPlayNetworking.send(player, SYNC_RESOURCES, buf);
    }

    /** Эффект повышения уровня */
    public static void sendLevelUpEffect(ServerPlayerEntity player, int level) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(level);
        ServerPlayNetworking.send(player, LEVEL_UP, buf);
    }

    /** XP-попап */
    public static void sendXpPopup(ServerPlayerEntity player, int amount, String source) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(amount);
        buf.writeString(source);
        ServerPlayNetworking.send(player, XP_POPUP, buf);
    }

    /** Текст урона */
    public static void sendDamageText(ServerPlayerEntity player, double x, double y, double z, float amount) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(amount);
        ServerPlayNetworking.send(player, SPAWN_DAMAGE_TEXT, buf);
    }

    /** Комбо урон */
    public static void sendComboDamage(ServerPlayerEntity player, float amount) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(amount);
        ServerPlayNetworking.send(player, ADD_COMBO_DAMAGE, buf);
    }

    /** Синхронизация кулдауна */
    public static void syncCooldown(ServerPlayerEntity player, String abilityId, int ticks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(abilityId);
        buf.writeInt(ticks);
        ServerPlayNetworking.send(player, SYNC_COOLDOWN, buf);
    }

    /** Сброс клиентского конфига */
    public static void sendResetConfigToClient(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, RESET_CLIENT_CONFIG, PacketByteBufs.create());
    }

    // ==================== HELPERS ====================
    // parseStatType removed since we now use StatRegistry string IDs
}