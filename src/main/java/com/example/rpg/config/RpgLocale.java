package com.example.rpg.config;

public class RpgLocale {

    // ==================== GENERAL ====================

    public static String get(String key) {
        boolean ru = RpgConfig.get().isRussian();
        return switch (key) {
            // Menu
            case "menu.title" -> ru ? "Меню персонажа" : "Character Menu";
            case "menu.level" -> ru ? "Ур." : "Lv.";
            case "menu.sp" -> "SP:";
            case "menu.close" -> ru ? "✕ Закрыть" : "✕ Close";
            case "menu.reset" -> ru ? "⟲ Сброс" : "⟲ Reset";

            // Tabs
            case "tab.stats" -> ru ? "Статы" : "Stats";
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
            case "stat.speed" -> ru ? "Скорость" : "Speed";
            case "stat.jump" -> ru ? "Прыжок" : "Jump";
            case "stat.mana" -> ru ? "Мана" : "Mana";
            case "stat.stamina" -> ru ? "Стамина" : "Stamina";
            case "stat.fortune" -> ru ? "Удача" : "Fortune";
            case "stat.looting" -> ru ? "Добыча" : "Looting";
            case "stat.max" -> "MAX";

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
            case "skill.fire_fireball" -> ru ? "Огненный шар" : "Fireball";

            // Magic skills - Water
            case "skill.water_mastery" -> ru ? "Власть воды" : "Water Mastery";
            case "skill.water_bolt" -> ru ? "Водяной заряд" : "Water Bolt";

            // Magic skills - Wind
            case "skill.wind_mastery" -> ru ? "Власть ветра" : "Wind Mastery";
            case "skill.wind_gust" -> ru ? "Порыв ветра" : "Gust";

            // Magic skills - Lightning
            case "skill.lightning_mastery" -> ru ? "Власть молний" : "Lightning Mastery";
            case "skill.lightning_shock" -> ru ? "Разряд" : "Shock";

            // Magic skills - Earth
            case "skill.earth_mastery" -> ru ? "Власть земли" : "Earth Mastery";
            case "skill.earth_rock_throw" -> ru ? "Бросок камня" : "Rock Throw";

            default -> key;
        };
    }

    // ==================== XP SOURCES ====================

    public static String getXpSource(String sourceId) {
        if (sourceId == null || sourceId.isEmpty())
            return "";

        boolean ru = RpgConfig.get().isRussian();
        String lowerSource = sourceId.toLowerCase();

        return switch (lowerSource) {
            // Ores
            case "coal" -> ru ? "Уголь" : "Coal";
            case "iron" -> ru ? "Железо" : "Iron";
            case "copper" -> ru ? "Медь" : "Copper";
            case "gold" -> ru ? "Золото" : "Gold";
            case "lapis" -> ru ? "Лазурит" : "Lapis";
            case "redstone" -> ru ? "Редстоун" : "Redstone";
            case "diamond" -> ru ? "Алмаз" : "Diamond";
            case "emerald" -> ru ? "Изумруд" : "Emerald";
            case "quartz" -> ru ? "Кварц" : "Quartz";
            case "nether_gold" -> ru ? "Незер. золото" : "Nether Gold";
            case "ancient_debris" -> ru ? "Древние обломки" : "Ancient Debris";

            // Bosses
            case "ender_dragon" -> ru ? "Дракон Края" : "Ender Dragon";
            case "wither" -> ru ? "Визер" : "Wither";
            case "elder_guardian" -> ru ? "Древний страж" : "Elder Guardian";
            case "warden" -> ru ? "Хранитель" : "Warden";

            // Mobs
            case "zombie" -> ru ? "Зомби" : "Zombie";
            case "skeleton" -> ru ? "Скелет" : "Skeleton";
            case "creeper" -> ru ? "Крипер" : "Creeper";
            case "spider" -> ru ? "Паук" : "Spider";
            case "cave_spider" -> ru ? "Пещерный паук" : "Cave Spider";
            case "enderman" -> ru ? "Эндермен" : "Enderman";
            case "blaze" -> ru ? "Ифрит" : "Blaze";
            case "ghast" -> ru ? "Гаст" : "Ghast";
            case "magma_cube" -> ru ? "Магмовый куб" : "Magma Cube";
            case "slime" -> ru ? "Слизень" : "Slime";
            case "witch" -> ru ? "Ведьма" : "Witch";
            case "phantom" -> ru ? "Фантом" : "Phantom";
            case "drowned" -> ru ? "Утопленник" : "Drowned";
            case "husk" -> ru ? "Кадавр" : "Husk";
            case "stray" -> ru ? "Зимогор" : "Stray";
            case "piglin" -> ru ? "Пиглин" : "Piglin";
            case "zombified_piglin" -> ru ? "Зомби-пиглин" : "Zombified Piglin";
            case "hoglin" -> ru ? "Хоглин" : "Hoglin";
            case "pillager" -> ru ? "Разбойник" : "Pillager";
            case "ravager" -> ru ? "Разоритель" : "Ravager";
            case "evoker" -> ru ? "Вызыватель" : "Evoker";
            case "vindicator" -> ru ? "Поборник" : "Vindicator";
            case "wither_skeleton" -> ru ? "Скелет-визер" : "Wither Skeleton";
            case "guardian" -> ru ? "Страж" : "Guardian";
            case "shulker" -> ru ? "Шалкер" : "Shulker";
            case "silverfish" -> ru ? "Чешуйница" : "Silverfish";
            case "endermite" -> ru ? "Эндермит" : "Endermite";
            case "vex" -> ru ? "Вредина" : "Vex";

            // Animals
            case "pig" -> ru ? "Свинья" : "Pig";
            case "cow" -> ru ? "Корова" : "Cow";
            case "sheep" -> ru ? "Овца" : "Sheep";
            case "chicken" -> ru ? "Курица" : "Chicken";
            case "rabbit" -> ru ? "Кролик" : "Rabbit";
            case "horse" -> ru ? "Лошадь" : "Horse";
            case "wolf" -> ru ? "Волк" : "Wolf";
            case "fox" -> ru ? "Лиса" : "Fox";
            case "cat" -> ru ? "Кошка" : "Cat";
            case "bee" -> ru ? "Пчела" : "Bee";
            case "polar_bear" -> ru ? "Белый медведь" : "Polar Bear";
            case "llama" -> ru ? "Лама" : "Llama";
            case "panda" -> ru ? "Панда" : "Panda";
            case "goat" -> ru ? "Коза" : "Goat";
            case "axolotl" -> ru ? "Аксолотль" : "Axolotl";
            case "iron_golem" -> ru ? "Железный голем" : "Iron Golem";
            case "snow_golem" -> ru ? "Снежный голем" : "Snow Golem";
            case "villager" -> ru ? "Житель" : "Villager";

            // System
            case "command" -> ru ? "Команда" : "Command";

            default -> sourceId;
        };
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
}