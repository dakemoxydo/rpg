package com.example.rpg.stats;

public interface IStat {
    String getId();

    String getDisplayName();

    String getDescription();

    int getMaxLevel();

    int getCostPerPoint();

    int getColor(MagicElement element);

    String getNextLevelDescription(int currentLevel);
}
