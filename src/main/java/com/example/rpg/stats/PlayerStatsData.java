package com.example.rpg.stats;

import com.example.rpg.ability.MagicSkillRegistry;
import com.example.rpg.ability.magic.MagicAbility;
import net.minecraft.nbt.NbtCompound;
import java.util.HashMap;
import java.util.Map;

public class PlayerStatsData {

    private final Map<StatType, Integer> statLevels = new HashMap<>();
    private final Map<String, Integer> abilityLevels = new HashMap<>();
    private final Map<String, Integer> magicSkillLevels = new HashMap<>();
    private int totalXp = 0;
    private int currentLevel = 0;
    private int skillPoints = 0;

    private float currentMana = 100;
    private float currentStamina = 100;

    private MagicElement element = MagicElement.NONE;

    public PlayerStatsData() {
        for (StatType stat : StatType.values()) {
            statLevels.put(stat, 0);
        }
    }

    // --- Стихия ---

    public MagicElement getElement() { return element; }

    public void setElement(MagicElement element) {
        this.element = element;
    }

    public boolean hasElement() {
        return element != MagicElement.NONE;
    }

    public void resetElement() {
        this.element = MagicElement.NONE;
        this.magicSkillLevels.clear();
    }

    // --- Магические скилы ---

    public int getMagicSkillLevel(String skillId) {
        return magicSkillLevels.getOrDefault(skillId, 0);
    }

    public void setMagicSkillLevel(String skillId, int level) {
        magicSkillLevels.put(skillId, level);
    }

    public boolean canUpgradeMagicSkill(String skillId, int maxLevel, int cost) {
        int currentLvl = getMagicSkillLevel(skillId);
        return currentLvl < maxLevel && skillPoints >= cost;
    }

    public boolean upgradeMagicSkill(String skillId, int maxLevel, int cost) {
        if (!canUpgradeMagicSkill(skillId, maxLevel, cost)) return false;
        skillPoints -= cost;
        magicSkillLevels.put(skillId, getMagicSkillLevel(skillId) + 1);
        return true;
    }

    public boolean hasMagicSkill(String skillId) {
        return getMagicSkillLevel(skillId) > 0;
    }

    public boolean meetsRequirements(String[] requiredSkills) {
        if (requiredSkills == null || requiredSkills.length == 0) return true;
        for (String reqId : requiredSkills) {
            if (reqId == null || getMagicSkillLevel(reqId) <= 0) return false;
        }
        return true;
    }

    public int getSpentMagicPoints() {
        int spent = 0;
        for (Map.Entry<String, Integer> entry : magicSkillLevels.entrySet()) {
            String skillId = entry.getKey();
            int level = entry.getValue();

            MagicAbility ability = MagicSkillRegistry.getAbility(skillId);
            if (ability != null) {
                spent += level * ability.getCostPerLevel();
            } else {
                spent += level * 3;
            }
        }
        return spent;
    }

    // --- Статы ---

    public int getStatLevel(StatType stat) {
        return statLevels.getOrDefault(stat, 0);
    }

    public boolean canUpgrade(StatType stat) {
        int currentLvl = getStatLevel(stat);
        return currentLvl < stat.getMaxLevel() && skillPoints >= stat.getCostPerPoint();
    }

    public boolean upgradeStat(StatType stat) {
        if (!canUpgrade(stat)) return false;
        skillPoints -= stat.getCostPerPoint();
        statLevels.put(stat, getStatLevel(stat) + 1);
        return true;
    }

    public void setStatLevel(StatType stat, int level) {
        statLevels.put(stat, Math.min(level, stat.getMaxLevel()));
        if (stat == StatType.MANA) {
            currentMana = Math.min(currentMana, getMaxMana());
        } else if (stat == StatType.STAMINA) {
            currentStamina = Math.min(currentStamina, getMaxStamina());
        }
    }

    // --- Мана ---

    public int getMaxMana() {
        int level = getStatLevel(StatType.MANA);
        return 100 + (level * 20);
    }

    public float getCurrentMana() {
        return Math.min(currentMana, getMaxMana());
    }

    public void setCurrentMana(float mana) {
        this.currentMana = Math.max(0, Math.min(mana, getMaxMana()));
    }

    public float getManaRegenPerSecond() {
        int level = getStatLevel(StatType.MANA);
        return 1.0f + (level * 1.0f);
    }

    public boolean useMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    public void regenMana(float deltaSeconds) {
        currentMana = Math.min(currentMana + getManaRegenPerSecond() * deltaSeconds, getMaxMana());
    }

    public float getManaProgress() {
        int max = getMaxMana();
        if (max <= 0) return 0;
        return getCurrentMana() / max;
    }

    // --- Стамина ---

    public int getMaxStamina() {
        int level = getStatLevel(StatType.STAMINA);
        return 100 + (level * 15);
    }

    public float getCurrentStamina() {
        return Math.min(currentStamina, getMaxStamina());
    }

    public void setCurrentStamina(float stamina) {
        this.currentStamina = Math.max(0, Math.min(stamina, getMaxStamina()));
    }

    public float getStaminaRegenPerSecond() {
        int level = getStatLevel(StatType.STAMINA);
        return 2.0f + (level * 0.5f);
    }

    public boolean useStamina(int amount) {
        if (currentStamina >= amount) {
            currentStamina -= amount;
            return true;
        }
        return false;
    }

    public void regenStamina(float deltaSeconds) {
        currentStamina = Math.min(currentStamina + getStaminaRegenPerSecond() * deltaSeconds, getMaxStamina());
    }

    public float getStaminaProgress() {
        int max = getMaxStamina();
        if (max <= 0) return 0;
        return getCurrentStamina() / max;
    }

    // --- Способности ---

    public int getAbilityLevel(String abilityId) {
        return abilityLevels.getOrDefault(abilityId, 0);
    }

    public void setAbilityLevel(String abilityId, int level) {
        abilityLevels.put(abilityId, level);
    }

    public boolean canUpgradeAbility(String abilityId, int maxLevel, int cost) {
        int currentLvl = getAbilityLevel(abilityId);
        return currentLvl < maxLevel && skillPoints >= cost;
    }

    public boolean upgradeAbility(String abilityId, int maxLevel, int cost) {
        if (!canUpgradeAbility(abilityId, maxLevel, cost)) return false;
        skillPoints -= cost;
        abilityLevels.put(abilityId, getAbilityLevel(abilityId) + 1);
        return true;
    }

    public boolean hasAbility(String abilityId) {
        return getAbilityLevel(abilityId) > 0;
    }

    // --- XP и уровни ---

    public int getTotalXp() { return totalXp; }
    public int getCurrentLevel() { return currentLevel; }
    public int getSkillPoints() { return skillPoints; }

    public void setSkillPoints(int points) { this.skillPoints = points; }
    public void setCurrentLevel(int level) { this.currentLevel = level; }
    public void setTotalXp(int xp) { this.totalXp = xp; }

    public int getXpForNextLevel() {
        return 50 + (currentLevel * 25);
    }

    public int getCurrentLevelXp() {
        int xpUsed = 0;
        for (int i = 0; i < currentLevel; i++) {
            xpUsed += 50 + (i * 25);
        }
        return totalXp - xpUsed;
    }

    public float getXpProgress() {
        int needed = getXpForNextLevel();
        if (needed <= 0) return 1.0f;
        return (float) getCurrentLevelXp() / (float) needed;
    }

    public boolean addXp(int amount) {
        totalXp += amount;
        boolean leveledUp = false;
        while (getCurrentLevelXp() >= getXpForNextLevel()) {
            currentLevel++;
            skillPoints++;
            currentMana = getMaxMana();
            currentStamina = getMaxStamina();
            leveledUp = true;
        }
        return leveledUp;
    }

    public void reset() {
        totalXp = 0;
        currentLevel = 0;
        skillPoints = 0;
        currentMana = 100;
        currentStamina = 100;
        for (StatType stat : StatType.values()) {
            statLevels.put(stat, 0);
        }
        abilityLevels.clear();
    }

    public void resetStats() {
        for (StatType stat : StatType.values()) {
            statLevels.put(stat, 0);
        }
        abilityLevels.clear();
        currentMana = Math.min(currentMana, getMaxMana());
        currentStamina = Math.min(currentStamina, getMaxStamina());
    }

    public int getSpentPoints() {
        int spent = 0;
        for (StatType stat : StatType.values()) {
            spent += getStatLevel(stat) * stat.getCostPerPoint();
        }
        for (int level : abilityLevels.values()) {
            spent += level * 2;
        }
        return spent;
    }

    // --- NBT ---

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("totalXp", totalXp);
        nbt.putInt("currentLevel", currentLevel);
        nbt.putInt("skillPoints", skillPoints);
        nbt.putFloat("currentMana", currentMana);
        nbt.putFloat("currentStamina", currentStamina);
        nbt.putString("element", element.name());

        NbtCompound statsNbt = new NbtCompound();
        for (StatType stat : StatType.values()) {
            statsNbt.putInt(stat.name(), getStatLevel(stat));
        }
        nbt.put("stats", statsNbt);

        NbtCompound abilitiesNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : abilityLevels.entrySet()) {
            abilitiesNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("abilities", abilitiesNbt);

        NbtCompound magicNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : magicSkillLevels.entrySet()) {
            magicNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("magicSkills", magicNbt);

        return nbt;
    }

    public static PlayerStatsData fromNbt(NbtCompound nbt) {
        if (nbt == null) {
            return new PlayerStatsData();
        }

        PlayerStatsData data = new PlayerStatsData();
        data.totalXp = nbt.getInt("totalXp");
        data.currentLevel = nbt.getInt("currentLevel");
        data.skillPoints = nbt.getInt("skillPoints");
        data.currentMana = nbt.contains("currentMana") ? nbt.getFloat("currentMana") : 100;
        data.currentStamina = nbt.contains("currentStamina") ? nbt.getFloat("currentStamina") : 100;

        if (nbt.contains("element")) {
            try {
                data.element = MagicElement.valueOf(nbt.getString("element"));
            } catch (Exception e) {
                data.element = MagicElement.NONE;
            }
        }

        NbtCompound statsNbt = nbt.getCompound("stats");
        for (StatType stat : StatType.values()) {
            if (statsNbt.contains(stat.name())) {
                data.statLevels.put(stat, statsNbt.getInt(stat.name()));
            }
        }

        NbtCompound abilitiesNbt = nbt.getCompound("abilities");
        for (String key : abilitiesNbt.getKeys()) {
            data.abilityLevels.put(key, abilitiesNbt.getInt(key));
        }

        NbtCompound magicNbt = nbt.getCompound("magicSkills");
        for (String key : magicNbt.getKeys()) {
            data.magicSkillLevels.put(key, magicNbt.getInt(key));
        }

        return data;
    }

    public void copyFrom(PlayerStatsData other) {
        this.totalXp = other.totalXp;
        this.currentLevel = other.currentLevel;
        this.skillPoints = other.skillPoints;
        this.currentMana = other.currentMana;
        this.currentStamina = other.currentStamina;
        this.element = other.element;
        this.statLevels.clear();
        this.statLevels.putAll(other.statLevels);
        this.abilityLevels.clear();
        this.abilityLevels.putAll(other.abilityLevels);
        this.magicSkillLevels.clear();
        this.magicSkillLevels.putAll(other.magicSkillLevels);
    }
}