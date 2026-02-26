package com.example.rpg.stats;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatRegistry {
    private static final Map<String, AttributeStat> REGISTRY = new LinkedHashMap<>();

    public static final String STRENGTH = "strength";
    public static final String DEXTERITY = "dexterity";
    public static final String VITALITY = "vitality";
    public static final String INTELLIGENCE = "intelligence";
    public static final String MAGIC_POWER = "magic_power";

    public static void init() {
        register(new AttributeStat.Builder(STRENGTH, "Strength")
                .description("Increases physical melee damage.")
                .maxLevel(20)
                .costPerPoint(1)
                .defaultColor(0xFFe85050) // Red
                .baseValue(0.0)
                .valuePerLevel(1.0) // +1 Damage per level
                .nextLevelDescription((level, stat) -> {
                    AttributeStat aStat = (AttributeStat) stat;
                    return String.format("+%.1f Damage (To %.1f)", aStat.getValuePerLevel(),
                            aStat.getValueAtLevel(level + 1));
                })
                .build());

        register(new AttributeStat.Builder(DEXTERITY, "Dexterity")
                .description("Increases max Stamina and move speed.")
                .maxLevel(20)
                .costPerPoint(1)
                .defaultColor(0xFF50e8a0) // Green/Teal
                .baseValue(100.0) // Base stamina
                .valuePerLevel(10.0) // +10 Stamina per level
                .baseRegen(5.0) // Base stamina regen
                .regenPerLevel(0.5) // +0.5 Stamina regen per level
                .dynamicColor((element,
                        defaultC) -> element != null && element != MagicElement.NONE ? element.staminaColor : defaultC)
                .nextLevelDescription((level, stat) -> {
                    AttributeStat aStat = (AttributeStat) stat;
                    return String.format("+%.0f Stamina, +%.1f Regen", aStat.getValuePerLevel(),
                            aStat.getRegenAtLevel(level + 1) - aStat.getRegenAtLevel(level));
                })
                .build());

        register(new AttributeStat.Builder(VITALITY, "Vitality")
                .description("Increases max Health and Defense.")
                .maxLevel(20)
                .costPerPoint(1)
                .defaultColor(0xFFe88050) // Orange
                .baseValue(20.0) // Base HP
                .valuePerLevel(2.0) // +2 HP (1 heart) per level
                .nextLevelDescription((level, stat) -> {
                    AttributeStat aStat = (AttributeStat) stat;
                    return String.format("+%.1f HP (To %.1f), +0.5%% Defense", aStat.getValuePerLevel(),
                            aStat.getValueAtLevel(level + 1));
                })
                .build());

        register(new AttributeStat.Builder(INTELLIGENCE, "Intelligence")
                .description("Increases max Mana and Mana regeneration.")
                .maxLevel(20)
                .costPerPoint(1)
                .defaultColor(0xFF4169E1) // Blue
                .baseValue(100.0) // Base mana
                .valuePerLevel(15.0) // +15 Mana per level
                .baseRegen(2.0) // Base mana regen
                .regenPerLevel(0.5) // +0.5 Mana regen per level
                .dynamicColor((element, defaultC) -> element != null && element != MagicElement.NONE ? element.manaColor
                        : defaultC)
                .nextLevelDescription((level, stat) -> {
                    AttributeStat aStat = (AttributeStat) stat;
                    return String.format("+%.0f Mana, +%.1f Regen", aStat.getValuePerLevel(),
                            aStat.getRegenAtLevel(level + 1) - aStat.getRegenAtLevel(level));
                })
                .build());

        register(new AttributeStat.Builder(MAGIC_POWER, "Magic Power")
                .description("Significantly increases magical damage and healing.")
                .maxLevel(20)
                .costPerPoint(1)
                .defaultColor(0xFF9932CC) // Purple
                .baseValue(0.0)
                .valuePerLevel(2.0) // +2 Magic Power per level
                .nextLevelDescription((level, stat) -> {
                    AttributeStat aStat = (AttributeStat) stat;
                    return String.format("+%.1f Magic Power (To %.1f)", aStat.getValuePerLevel(),
                            aStat.getValueAtLevel(level + 1));
                })
                .build());
    }

    private static void register(AttributeStat stat) {
        REGISTRY.put(stat.getId(), stat);
    }

    public static AttributeStat get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<AttributeStat> getAll() {
        return REGISTRY.values();
    }
}
