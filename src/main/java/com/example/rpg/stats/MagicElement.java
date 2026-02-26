package com.example.rpg.stats;

public enum MagicElement {
    NONE(
            "None", "No element selected", "○",
            0xFF1a1a1a, 0xFF2a2a2a, 0xFF3a3a3a,
            0xFF888888, 0xFF555555,
            0xFF888888, 0xFFCCCCCC, 0xFF999999,
            0xFF666666, 0xFF444444,
            0xFF4169E1, 0xFFDAA520// Default mana/stamina
    ),
    FIRE(
            "Fire", "Master of flames and destruction", "🔥",
            0xFF1a0a05, 0xFF2d1510, 0xFF3d2015,
            0xFFFF6B35, 0xFF8B3A1A,
            0xFFFF6B35, 0xFFFFE4D6, 0xFFD4A08A,
            0xFFFF4500, 0xFFCC3700,
            0xFFFF6347, 0xFFFF8C00// Fire mana/stamina
    ),
    WATER(
            "Water", "Master of tides and healing", "💧",
            0xFF050a1a, 0xFF101525, 0xFF152030,
            0xFF4169E1, 0xFF1A3A8B,
            0xFF4169E1, 0xFFD6E4FF, 0xFF8AA0D4,
            0xFF1E90FF, 0xFF1565C0,
            0xFF4169E1, 0xFF00CED1// Water mana/stamina
    ),
    WIND(
            "Wind", "Master of air and speed", "🌪",
            0xFF051a15, 0xFF102520, 0xFF153025,
            0xFF40E0D0, 0xFF1A8B7A,
            0xFF40E0D0, 0xFFD6FFF5, 0xFF8AD4C8,
            0xFF00CED1, 0xFF008B8B,
            0xFF40E0D0, 0xFF98FB98// Wind mana/stamina
    ),
    LIGHTNING(
            "Lightning", "Master of storms and power", "⚡",
            0xFF1a1a05, 0xFF252515, 0xFF302520,
            0xFFFFD700, 0xFF8B7500,
            0xFFFFD700, 0xFFFFFED6, 0xFFD4C88A,
            0xFFFFD700, 0xFFB8860B,
            0xFFFFD700, 0xFFDAA520// Lightning mana/stamina
    ),
    EARTH(
            "Earth", "Master of stone and defense", "🪨",
            0xFF0a0f05, 0xFF151a10, 0xFF202515,
            0xFF8B4513, 0xFF5D3A1A,
            0xFF8B4513, 0xFFE8DCC8, 0xFFA89878,
            0xFF228B22, 0xFF006400,
            0xFF8B4513, 0xFF228B22// Earth mana/stamina
    );

    private final String displayName;
    private final String description;
    private final String icon;

    public final int bgDark;
    public final int bgMedium;
    public final int bgLight;
    public final int borderPrimary;
    public final int borderSecondary;
    public final int textTitle;
    public final int textPrimary;
    public final int textSecondary;
    public final int xpBarFill;
    public final int xpBarBorder;
    public final int manaColor;
    public final int staminaColor;

    // Константы для UI (общие для всех элементов)
    public static final int TEXT_SUCCESS = 0xFF50c878;
    public static final int TEXT_ERROR = 0xFFc85050;
    public static final int TEXT_WARNING = 0xFFe8d050;

    MagicElement(String displayName, String description, String icon,
            int bgDark, int bgMedium, int bgLight,
            int borderPrimary, int borderSecondary,
            int textTitle, int textPrimary, int textSecondary,
            int xpBarFill, int xpBarBorder,
            int manaColor, int staminaColor) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.bgDark = bgDark;
        this.bgMedium = bgMedium;
        this.bgLight = bgLight;
        this.borderPrimary = borderPrimary;
        this.borderSecondary = borderSecondary;
        this.textTitle = textTitle;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.xpBarFill = xpBarFill;
        this.xpBarBorder = xpBarBorder;
        this.manaColor = manaColor;
        this.staminaColor = staminaColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}