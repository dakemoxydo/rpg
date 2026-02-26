package com.example.rpg.config;

import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.PlayerStatsData;
import net.minecraft.client.MinecraftClient;

public class RpgLocale {

    // ==================== GENERAL ====================

    public static String get(String key) {
        boolean ru = isPlayerRussian();
        return switch (key) {
            // Menu
            case "menu.title" -> ru ? "Меню персонажа" : "Character Menu";
            case "menu.level" -> ru ? "Ур." : "Lv.";
            case "menu.sp" -> "SP:";
            case "menu.close" -> ru ? "✕ Закрыть" : "✕ Close";
            case "menu.reset" -> ru ? "⟲ Сброс" : "⟲ Reset";
            case "menu.reset.confirm" -> ru ? "Точно сбросить?" : "Confirm reset?";

            // Tabs
            case "tab.stats" -> ru ? "Статы" : "Stats";
            case "tab.build" -> ru ? "Билд" : "Build";
            case "tab.skills" -> ru ? "Навыки" : "Skills";
            case "tab.magic" -> ru ? "Магия" : "Magic";

            // Settings
            case "settings.title" -> ru ? "⚙ Настройки" : "⚙ Settings";
            case "settings.category.main" -> ru ? "§6Основные" : "§6Main";
            case "settings.category.skills" -> ru ? "§6Скилы" : "§6Skills";
            case "settings.category.element" -> ru ? "§6Стихия: " : "§6Element: ";
            case "settings.menu_key" -> ru ? "Меню:" : "Menu:";
            case "settings.dash_key" -> ru ? "Рывок:" : "Dash:";
            case "settings.language" -> ru ? "Язык:" : "Language:";
            case "settings.back" -> ru ? "← Назад" : "← Back";
            case "settings.cancel_hint" -> ru ? "ESC для отмены" : "ESC to cancel";

            // Element selection
            case "element.title" -> ru ? "✦ Выбор стихии ✦" : "✦ Choose Element ✦";
            case "element.subtitle" -> ru ? "Определяет путь магии" : "Defines your magic path";
            case "element.info" -> ru ? "Выберите для разблокировки магии" : "Select to unlock Magic";
            case "element.click_hint" -> ru ? "▶ Нажми чтобы выбрать" : "▶ Click to select";

            // Elements
            case "element.fire" -> ru ? "Огонь" : "Fire";
            case "element.water" -> ru ? "Вода" : "Water";
            case "element.wind" -> ru ? "Ветер" : "Wind";
            case "element.lightning" -> ru ? "Молния" : "Lightning";
            case "element.earth" -> ru ? "Земля" : "Earth";
            case "element.none" -> ru ? "Нет" : "None";

            // Element descriptions
            case "element.fire.desc" -> ru ? "Мастер пламени и разрушения" : "Master of flames and destruction";
            case "element.water.desc" -> ru ? "Мастер воды и исцеления" : "Master of tides and healing";
            case "element.wind.desc" -> ru ? "Мастер воздуха и скорости" : "Master of air and speed";
            case "element.lightning.desc" -> ru ? "Мастер молний и силы" : "Master of storms and power";
            case "element.earth.desc" -> ru ? "Мастер камня и защиты" : "Master of stone and defense";

            // Stats
            case "stat.health" -> ru ? "Здоровье" : "Health";
            case "stat.strength" -> ru ? "Сила" : "Strength";
            case "stat.dexterity" -> ru ? "Ловкость" : "Dexterity";
            case "stat.vitality" -> ru ? "Живучесть" : "Vitality";
            case "stat.intelligence" -> ru ? "Интеллект" : "Intelligence";
            case "stat.magic_power" -> ru ? "Маг. Сила" : "Magic Power";
            case "stat.speed" -> ru ? "Скорость" : "Speed";
            case "stat.jump" -> ru ? "Прыжок" : "Jump";
            case "stat.mana" -> ru ? "Мана" : "Mana";
            case "stat.stamina" -> ru ? "Стамина" : "Stamina";
            case "stat.fortune" -> ru ? "Удача" : "Fortune";
            case "stat.looting" -> ru ? "Добыча" : "Looting";
            case "stat.max" -> "MAX";

            // Final Stats
            case "stat.final.title" -> ru ? "Итоговые Статы" : "Final Stats";
            case "stat.final.health" -> ru ? "Макс. Здоровье: " : "Max Health: ";
            case "stat.final.defense" -> ru ? "Защита: " : "Defense: ";
            case "stat.final.melee" -> ru ? "Урон (Ближний): " : "Melee Damage: ";
            case "stat.final.magic" -> ru ? "Маг. Сила: " : "Magic Power: ";
            case "stat.final.mana" -> ru ? "Макс. Мана: " : "Max Mana: ";
            case "stat.final.mana_regen" -> ru ? "Реген Маны: " : "Mana Regen: ";
            case "stat.final.stamina" -> ru ? "Макс. Стамина: " : "Max Stamina: ";
            case "stat.final.stamina_regen" -> ru ? "Реген Стамины: " : "Stamina Regen: ";
            case "stat.final.regen_sec" -> ru ? "/с" : "/s";

            // Abilities (Physical/Support)
            case "ability.dash" -> ru ? "Рывок" : "Dash";
            case "ability.dash.desc" -> ru ? "Быстрый рывок вперёд" : "Quick dash forward";
            case "ability.heal" -> ru ? "Лечение" : "Heal";
            case "ability.heal.desc" -> ru ? "Восстанавливает здоровье" : "Restores health";

            // Level up
            case "levelup.title" -> ru ? "★ НОВЫЙ УРОВЕНЬ! ★" : "★ LEVEL UP! ★";
            case "levelup.level" -> ru ? "Уровень" : "Level";
            case "levelup.skillpoint" -> ru ? "+1 очко навыков!" : "+1 Skill Point Earned!";

            // XP popup
            case "xp.popup" -> "XP";
            case "xp.command" -> ru ? "Команда" : "Command";

            // Magic skills - Fire
            case "skill.fire_mastery" -> ru ? "Власть огня" : "Fire Mastery";
            case "skill.fire_mastery.desc" ->
                ru ? "Базовое понимание магии огня. Открывает доступ к огненным заклинаниям."
                        : "Basic understanding of fire magic. Unlocks fire spells.";
            case "skill.fire_fireball" -> ru ? "Огненный шар" : "Fireball";
            case "skill.fire_fireball.desc" -> ru ? "Выпускает сгусток пламени, поджигающий врагов."
                    : "Shoot a ball of pure flame that ignites enemies.";

            // Magic skills - Water
            case "skill.water_mastery" -> ru ? "Власть воды" : "Water Mastery";
            case "skill.water_mastery.desc" ->
                ru ? "Базовое понимание магии воды. Открывает доступ к водяным заклинаниям."
                        : "Basic understanding of water magic. Unlocks water spells.";
            case "skill.water_bolt" -> ru ? "Водяной заряд" : "Water Bolt";
            case "skill.water_bolt.desc" -> ru ? "Выпускает снаряд под давлением, отбрасывающий врагов."
                    : "Shoot a pressurized water projectile that knocks enemies back.";

            // Magic skills - Wind
            case "skill.wind_mastery" -> ru ? "Власть ветра" : "Wind Mastery";
            case "skill.wind_mastery.desc" ->
                ru ? "Базовое понимание магии ветра. Открывает доступ к воздушным заклинаниям."
                        : "Basic understanding of wind magic. Unlocks wind spells.";
            case "skill.wind_gust" -> ru ? "Порыв ветра" : "Gust";
            case "skill.wind_gust.desc" -> ru ? "Создаёт сильный порыв ветра, запускающий вас или врагов."
                    : "Create a strong gust of wind that launches you or enemies.";

            // Magic skills - Lightning
            case "skill.lightning_mastery" -> ru ? "Власть молний" : "Lightning Mastery";
            case "skill.lightning_mastery.desc" ->
                ru ? "Базовое понимание магии молний. Открывает доступ к электрическим заклинаниям."
                        : "Basic understanding of lightning magic. Unlocks electric spells.";
            case "skill.lightning_shock" -> ru ? "Разряд" : "Shock";
            case "skill.lightning_shock.desc" -> ru ? "Бьёт врага электрическим током на короткой дистанции."
                    : "Shock an enemy with electricity at close range.";

            // Magic skills - Earth
            case "skill.earth_mastery" -> ru ? "Власть земли" : "Earth Mastery";
            case "skill.earth_mastery.desc" ->
                ru ? "Базовое понимание магии земли. Открывает доступ к земляным заклинаниям."
                        : "Basic understanding of earth magic. Unlocks earth spells.";
            case "skill.earth_rock_throw" -> ru ? "Бросок камня" : "Rock Throw";
            case "skill.earth_rock_throw.desc" ->
                ru ? "Вырывает кусок земли и бросает его во врага." : "Rip a piece of earth and throw it at an enemy.";

            // UI Elements
            case "skill.upgrade_btn" -> ru ? "Прокачать" : "Upgrade";
            case "skill.upgrade_info" -> ru ? "Апгрейд: " : "Upgrade: ";
            case "skill.locked" -> ru ? "Заблокировано" : "Locked";
            case "skill.maxed_btn" -> ru ? "Максимум" : "Max Level";
            case "skill.cost" -> ru ? "Стоимость: " : "Cost: ";
            case "skill.sp" -> "SP";
            case "skill.mana" -> ru ? "Мана: " : "Mana: ";
            case "skill.stamina" -> ru ? "Стамина: " : "Stamina: ";
            case "skill.cooldown" -> ru ? "Перезарядка: " : "Cooldown: ";
            case "skill.sec" -> ru ? "с" : "s";
            case "skill.power.damage" -> ru ? "Урон: " : "Damage: ";
            case "skill.power.heal" -> ru ? "Лечение: " : "Heal: ";
            case "skill.power.force" -> ru ? "Сила: " : "Force: ";

            // Upgrades
            case "upgrade.unlock_ability" -> ru ? "Разблокирует способность " : "Unlocks ability: ";
            case "upgrade.unlock_general" -> ru ? "Открывает способность" : "Unlocks ability";
            case "upgrade.max_level" -> ru ? "Максимальный уровень" : "Max level";
            case "upgrade.default" ->
                ru ? "Увеличивает силу и снижает перезарядку" : "Increases power and reduces cooldown";
            case "upgrade.heal" -> ru ? "Увеличивает объем исцеления: %.1f -> %.1f ХП, КД: %dс -> %dс"
                    : "Increases healing amount: %.1f -> %.1f HP, CD: %ds -> %ds";
            case "upgrade.dash" -> ru ? "Увеличивает дальность рывка. Сила: %.1f -> %.1f, КД: %dс -> %dс"
                    : "Increases dash range. Power: %.1f -> %.1f, CD: %ds -> %ds";
            case "upgrade.gust" -> ru ? "Усиливает отталкивание: %.1f -> %.1f. Радиус и Урон возрастают. КД: %dс -> %dс"
                    : "Increases knockback: %.1f -> %.1f. Radius and Damage increase. CD: %ds -> %ds";
            case "upgrade.water_bolt" -> ru ? "Увеличивает урон: %.0f -> %.0f. Усиливает замедление. КД: %dс -> %dс"
                    : "Increases damage: %.0f -> %.0f. Empowers slow. CD: %ds -> %ds";
            case "upgrade.shock_lvl2" -> ru ? "Ударная мощь возрастает: Молния теперь наносит урон. Дальность растет."
                    : "Striking power increases: Lightning now deals damage. Range grows.";
            case "upgrade.shock" ->
                ru ? "Увеличивает дальность поражения. КД: %dс -> %dс" : "Increases strike range. CD: %ds -> %ds";
            case "upgrade.fireball" -> ru ? "Увеличивает скорость и дальность полета: %.1f -> %.1f. КД: %dс -> %dс"
                    : "Increases flight speed and range: %.1f -> %.1f. CD: %ds -> %ds";
            case "upgrade.rock_throw" -> ru ? "Увеличивает максимальный урон от падения: %.0f -> %.0f. КД: %dс -> %dс"
                    : "Increases maximum fall damage: %.0f -> %.0f. CD: %ds -> %ds";

            default -> key;
        };
    }

    public static String getOrDefault(String key, String defaultStr) {
        String result = get(key);
        if (result.equals(key))
            return defaultStr;
        return result;
    }

    // ==================== XP SOURCES ====================

    public static String getXpSource(String sourceId) {
        if (sourceId == null || sourceId.isEmpty())
            return "";

        boolean ru = isPlayerRussian();

        // Specific overrides for missing or generic things
        if (sourceId.equals("rpg.xp.source.command")) {
            return ru ? "Команда" : "Command";
        }

        // Native translation fallback
        return net.minecraft.text.Text.translatable(sourceId).getString();
    }

    // ==================== HELPERS ====================

    public static String getElementName(String elementId) {
        return switch (elementId.toLowerCase()) {
            case "fire" -> get("element.fire");
            case "water" -> get("element.water");
            case "wind" -> get("element.wind");
            case "lightning" -> get("element.lightning");
            case "earth" -> get("element.earth");
            default -> get("element.none");
        };
    }

    public static String getElementDesc(String elementId) {
        return switch (elementId.toLowerCase()) {
            case "fire" -> get("element.fire.desc");
            case "water" -> get("element.water.desc");
            case "wind" -> get("element.wind.desc");
            case "lightning" -> get("element.lightning.desc");
            case "earth" -> get("element.earth.desc");
            default -> "";
        };
    }

    public static String getStatName(String statId) {
        return switch (statId.toUpperCase()) {
            case "HEALTH" -> get("stat.health");
            case "STRENGTH" -> get("stat.strength");
            case "DEXTERITY" -> get("stat.dexterity");
            case "VITALITY" -> get("stat.vitality");
            case "INTELLIGENCE" -> get("stat.intelligence");
            case "MAGIC_POWER" -> get("stat.magic_power");
            case "SPEED" -> get("stat.speed");
            case "JUMP" -> get("stat.jump");
            case "MANA" -> get("stat.mana");
            case "STAMINA" -> get("stat.stamina");
            case "FORTUNE" -> get("stat.fortune");
            case "LOOTING" -> get("stat.looting");
            default -> statId;
        };
    }

    public static String getSkillName(String skillId) {
        String key = "skill." + skillId;
        String result = get(key);
        if (result.equals(key)) {
            return formatId(skillId);
        }
        return result;
    }

    public static String getSkillDesc(String skillId) {
        String baseKey = skillId.startsWith("ability.") ? skillId.substring(8) : skillId;
        String key = "skill." + baseKey + ".desc";
        if (baseKey.equals("dash") || baseKey.equals("heal")) {
            key = "ability." + baseKey + ".desc";
        }
        String result = get(key);
        if (result.equals(key)) {
            return "";
        }
        return result;
    }

    public static String getAbilityName(String abilityId) {
        String key = "ability." + abilityId;
        String result = get(key);
        if (result.equals(key)) {
            return formatId(abilityId);
        }
        return result;
    }

    private static String formatId(String id) {
        if (id == null || id.isEmpty())
            return id;
        String[] parts = id.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    /** Проверяет язык текущего игрока: сначала профиль, затем RpgConfig */
    private static boolean isPlayerRussian() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player instanceof IPlayerStatsAccessor accessor) {
                PlayerStatsData data = accessor.rpg_getStatsData();
                if (data != null) {
                    return data.isRussian();
                }
            }
        } catch (Exception ignored) {
        }
        return RpgConfig.get().isRussian();
    }
}