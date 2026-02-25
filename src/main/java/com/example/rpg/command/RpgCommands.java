package com.example.rpg.command;

import com.example.rpg.event.RpgEventHandlers; // Изменённый импорт
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.command.argument.EntityArgumentType;
import java.util.Collection;

public class RpgCommands {

        public static void register() {
                CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

                        dispatcher.register(CommandManager.literal("rpg")

                                        .then(CommandManager.literal("help")
                                                        .executes(context -> {
                                                                ServerCommandSource source = context.getSource();
                                                                // English
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§6§l=== RPG Mod Commands ==="), false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg help §7- Show this help"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg stats §7- Show your current stats"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg addxp <amount> §7- Add XP to yourself"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg setlevel <level> §7- Set your RPG level"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg addpoints <amount> §7- Add skill points"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg setstat <stat> <level> §7- Set stat level"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg reset [<targets>] §7- Reset stats and XP (and keybinds)"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg list §7- List all stat names"),
                                                                                false);

                                                                // Russian
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§6§l=== Команды RPG Мода ==="), false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg help §7- Показать это меню помощи"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg stats §7- Показать ваши текущие характеристики"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg addxp <кол-во> §7- Выдать себе опыт"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg setlevel <уровень> §7- Установить уровень RPG"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg addpoints <кол-во> §7- Выдать очки навыков"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg setstat <стат> <уровень> §7- Установить уровень характеристики"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg reset [<цели>] §7- Сброс статистики, XP и биндов"),
                                                                                false);
                                                                source.sendFeedback(() -> Text.literal(
                                                                                "§e/rpg list §7- Список всех названий характеристик"),
                                                                                false);
                                                                return 1;
                                                        }))

                                        .then(CommandManager.literal("stats")
                                                        .executes(context -> {
                                                                ServerPlayerEntity player = context.getSource()
                                                                                .getPlayerOrThrow();
                                                                RpgWorldData worldData = RpgWorldData
                                                                                .get(player.getServer());
                                                                PlayerStatsData data = worldData
                                                                                .getPlayerData(player.getUuid());

                                                                context.getSource().sendFeedback(() -> Text
                                                                                .literal("§6§l=== Your RPG Stats ==="),
                                                                                false);
                                                                context.getSource().sendFeedback(() -> Text.literal(
                                                                                String.format("§eLevel: §f%d §7| §eXP: §f%d/%d §7| §eSkill Points: §f%d",
                                                                                                data.getCurrentLevel(),
                                                                                                data.getCurrentLevelXp(),
                                                                                                data.getXpForNextLevel(),
                                                                                                data.getSkillPoints())),
                                                                                false);

                                                                for (StatType stat : StatType.values()) {
                                                                        int lvl = data.getStatLevel(stat);
                                                                        String line = String.format(
                                                                                        "§a%s§7: §f%d/%d §7(cost: %d pts)",
                                                                                        stat.getDisplayName(), lvl,
                                                                                        stat.getMaxLevel(),
                                                                                        stat.getCostPerPoint());
                                                                        context.getSource().sendFeedback(
                                                                                        () -> Text.literal(line),
                                                                                        false);
                                                                }
                                                                return 1;
                                                        }))

                                        .then(CommandManager.literal("addxp")
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .then(CommandManager
                                                                        .argument("amount",
                                                                                        IntegerArgumentType.integer(1))
                                                                        .executes(context -> {
                                                                                ServerPlayerEntity player = context
                                                                                                .getSource()
                                                                                                .getPlayerOrThrow();
                                                                                int amount = IntegerArgumentType
                                                                                                .getInteger(context,
                                                                                                                "amount");
                                                                                RpgEventHandlers.awardXp(player,
                                                                                                amount); // Исправлено
                                                                                context.getSource().sendFeedback(
                                                                                                () -> Text.literal(
                                                                                                                "§aAdded §e" + amount
                                                                                                                                + " §aXP!"),
                                                                                                false);
                                                                                return 1;
                                                                        })))

                                        .then(CommandManager.literal("setlevel")
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .then(CommandManager
                                                                        .argument("level",
                                                                                        IntegerArgumentType.integer(0))
                                                                        .executes(context -> {
                                                                                ServerPlayerEntity player = context
                                                                                                .getSource()
                                                                                                .getPlayerOrThrow();
                                                                                int level = IntegerArgumentType
                                                                                                .getInteger(context,
                                                                                                                "level");
                                                                                RpgWorldData worldData = RpgWorldData
                                                                                                .get(player.getServer());
                                                                                PlayerStatsData data = worldData
                                                                                                .getPlayerData(player
                                                                                                                .getUuid());

                                                                                int totalXp = 0;
                                                                                for (int i = 0; i < level; i++) {
                                                                                        totalXp += 50 + (i * 25);
                                                                                }
                                                                                data.setTotalXp(totalXp);
                                                                                data.setCurrentLevel(level);
                                                                                worldData.markDirty();

                                                                                StatsManager.applyStats(player, data);
                                                                                StatsNetworking.syncToClient(player);

                                                                                context.getSource().sendFeedback(
                                                                                                () -> Text.literal(
                                                                                                                "§aLevel set to §e"
                                                                                                                                + level),
                                                                                                false);
                                                                                return 1;
                                                                        })))

                                        .then(CommandManager.literal("addpoints")
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .then(CommandManager
                                                                        .argument("amount",
                                                                                        IntegerArgumentType.integer(1))
                                                                        .executes(context -> {
                                                                                ServerPlayerEntity player = context
                                                                                                .getSource()
                                                                                                .getPlayerOrThrow();
                                                                                int amount = IntegerArgumentType
                                                                                                .getInteger(context,
                                                                                                                "amount");
                                                                                RpgWorldData worldData = RpgWorldData
                                                                                                .get(player.getServer());
                                                                                PlayerStatsData data = worldData
                                                                                                .getPlayerData(player
                                                                                                                .getUuid());

                                                                                data.setSkillPoints(data
                                                                                                .getSkillPoints()
                                                                                                + amount);
                                                                                worldData.markDirty();
                                                                                StatsNetworking.syncToClient(player);

                                                                                context.getSource().sendFeedback(
                                                                                                () -> Text.literal(
                                                                                                                "§aAdded §e" + amount
                                                                                                                                + " §askill points!"),
                                                                                                false);
                                                                                return 1;
                                                                        })))

                                        .then(CommandManager.literal("setstat")
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .then(CommandManager.argument("stat", StringArgumentType.word())
                                                                        .then(CommandManager.argument("level",
                                                                                        IntegerArgumentType.integer(0))
                                                                                        .executes(context -> {
                                                                                                ServerPlayerEntity player = context
                                                                                                                .getSource()
                                                                                                                .getPlayerOrThrow();
                                                                                                String statName = StringArgumentType
                                                                                                                .getString(context,
                                                                                                                                "stat");
                                                                                                int level = IntegerArgumentType
                                                                                                                .getInteger(context,
                                                                                                                                "level");

                                                                                                StatType stat = null;
                                                                                                for (StatType s : StatType
                                                                                                                .values()) {
                                                                                                        if (s.name().equalsIgnoreCase(
                                                                                                                        statName)) {
                                                                                                                stat = s;
                                                                                                                break;
                                                                                                        }
                                                                                                }

                                                                                                if (stat == null) {
                                                                                                        context.getSource()
                                                                                                                        .sendError(
                                                                                                                                        Text.literal("§cUnknown stat: "
                                                                                                                                                        + statName
                                                                                                                                                        +
                                                                                                                                                        ". Use /rpg list to see all stats."));
                                                                                                        return 0;
                                                                                                }

                                                                                                RpgWorldData worldData = RpgWorldData
                                                                                                                .get(player.getServer());
                                                                                                PlayerStatsData data = worldData
                                                                                                                .getPlayerData(player
                                                                                                                                .getUuid());

                                                                                                data.setStatLevel(stat,
                                                                                                                level);
                                                                                                worldData.markDirty();
                                                                                                StatsManager.applyStats(
                                                                                                                player,
                                                                                                                data);
                                                                                                StatsNetworking.syncToClient(
                                                                                                                player);

                                                                                                StatType finalStat = stat;
                                                                                                context.getSource()
                                                                                                                .sendFeedback(() -> Text
                                                                                                                                .literal("§a" + finalStat
                                                                                                                                                .getDisplayName()
                                                                                                                                                +
                                                                                                                                                " set to level §e"
                                                                                                                                                + level),
                                                                                                                                false);
                                                                                                return 1;
                                                                                        }))))

                                        .then(CommandManager.literal("reset")
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .executes(context -> {
                                                                // Default reset self
                                                                ServerPlayerEntity player = context.getSource()
                                                                                .getPlayerOrThrow();
                                                                resetPlayer(player);
                                                                context.getSource().sendFeedback(() -> Text.literal(
                                                                                "§cYour stats and XP have been reset!"),
                                                                                false);
                                                                return 1;
                                                        })
                                                        .then(CommandManager
                                                                        .argument("targets",
                                                                                        EntityArgumentType.players())
                                                                        .executes(context -> {
                                                                                Collection<ServerPlayerEntity> targets = EntityArgumentType
                                                                                                .getPlayers(context,
                                                                                                                "targets");
                                                                                for (ServerPlayerEntity target : targets) {
                                                                                        resetPlayer(target);
                                                                                }
                                                                                context.getSource().sendFeedback(
                                                                                                () -> Text.literal(
                                                                                                                "§cSuccessfully reset stats and configs for "
                                                                                                                                + targets.size()
                                                                                                                                + " player(s)!"),
                                                                                                true);
                                                                                return targets.size();
                                                                        })))

                                        .then(CommandManager.literal("list")
                                                        .executes(context -> {
                                                                context.getSource().sendFeedback(() -> Text
                                                                                .literal("§6§l=== Available Stats ==="),
                                                                                false);
                                                                for (StatType stat : StatType.values()) {
                                                                        context.getSource().sendFeedback(() -> Text
                                                                                        .literal("§e" + stat.name()
                                                                                                        + " §7- " +
                                                                                                        stat.getDisplayName()
                                                                                                        + " (max: " +
                                                                                                        stat.getMaxLevel()
                                                                                                        + ")"),
                                                                                        false);
                                                                }
                                                                return 1;
                                                        })));
                });
        }

        private static void resetPlayer(ServerPlayerEntity player) {
                RpgWorldData worldData = RpgWorldData.get(player.getServer());
                PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                data.reset();
                worldData.markDirty();
                StatsManager.applyStats(player, data);
                StatsNetworking.syncToClient(player);
                StatsNetworking.sendResetConfigToClient(player);
        }
}