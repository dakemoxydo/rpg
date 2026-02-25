package com.example.rpg.screen;

import com.example.rpg.ability.Ability;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.MagicSkillRegistry;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgConfig;
import com.example.rpg.config.RpgLocale;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.*;
import com.example.rpg.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class StatsScreen extends Screen {

    private int panelWidth, panelHeight, statRowHeight, panelX, panelY, padding;
    private float uiScale;

    private enum Tab {
        STATS, SKILLS, MAGIC
    }

    private Tab currentTab = Tab.STATS;
    private PlayerStatsData cachedData;

    public StatsScreen() {
        super(Text.literal("RPG Stats"));
    }

    @Override
    protected void init() {
        super.init();
        calculateAdaptiveSizes();
        cachedData = getPlayerData();

        if (cachedData != null && !cachedData.hasElement()) {
            MinecraftClient.getInstance()
                    .execute(() -> MinecraftClient.getInstance().setScreen(new ElementSelectionScreen()));
            return;
        }
        rebuildButtons();
    }

    private void calculateAdaptiveSizes() {
        uiScale = Math.max(0.6f, Math.min(1.0f, Math.min(
                (float) (this.width - 40) / 340,
                (float) (this.height - 40) / 360)));
        panelWidth = (int) (340 * uiScale);
        panelHeight = (int) (360 * uiScale);
        statRowHeight = (int) (26 * uiScale);
        padding = (int) (12 * uiScale);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;
    }

    private void rebuildButtons() {
        this.clearChildren();
        if (cachedData == null)
            cachedData = getPlayerData();
        if (cachedData == null)
            return;

        int tabSpacing = (int) (6 * uiScale);
        int totalTabWidth = panelWidth - padding * 2;
        int tabWidth = (totalTabWidth - tabSpacing * 2) / 3;
        int tabHeight = (int) (18 * uiScale);
        int tabY = panelY + (int) (50 * uiScale);

        addTabButton(RpgLocale.get("tab.stats"), Tab.STATS, panelX + padding, tabY, tabWidth, tabHeight);
        addTabButton(RpgLocale.get("tab.skills"), Tab.SKILLS, panelX + padding + tabWidth + tabSpacing, tabY, tabWidth,
                tabHeight);
        addTabButton(RpgLocale.get("tab.magic"), Tab.MAGIC, panelX + padding + (tabWidth + tabSpacing) * 2, tabY,
                tabWidth, tabHeight);

        if (currentTab == Tab.STATS) {
            buildStatsButtons();
            buildStatsBottomButtons();
        } else if (currentTab == Tab.SKILLS) {
            buildSkillsButtons();
            buildSkillsBottomButtons();
        } else {
            buildMagicTreeButtons(cachedData);
            buildMagicBottomButtons();
        }
    }

    private void addTabButton(String name, Tab tab, int x, int y, int w, int h) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(currentTab == tab ? "§a[" + name + "]" : name),
                button -> {
                    currentTab = tab;
                    rebuildButtons();
                }).dimensions(x, y, w, h).build());
    }

    // ==================== STATS TAB ====================

    private void buildStatsButtons() {
        int buttonWidth = (int) (24 * uiScale);
        int buttonHeight = (int) (18 * uiScale);
        int buttonX = panelX + panelWidth - padding - buttonWidth;
        int startY = panelY + (int) (82 * uiScale);

        for (int i = 0; i < StatType.values().length; i++) {
            StatType stat = StatType.values()[i];
            int y = startY + (i * statRowHeight);
            this.addDrawableChild(ButtonWidget.builder(Text.literal("+"),
                    button -> StatsNetworking.sendUpgradeRequest(stat))
                    .dimensions(buttonX, y + (statRowHeight - buttonHeight) / 2, buttonWidth, buttonHeight).build());
        }
    }

    private void buildStatsBottomButtons() {
        int bottomY = panelY + panelHeight - (int) (32 * uiScale);
        int btnSpacing = (int) (8 * uiScale);
        int totalBtnWidth = panelWidth - padding * 2;
        int btnWidth = (totalBtnWidth - btnSpacing) / 2;
        int btnHeight = (int) (20 * uiScale);

        // Ресет статов - только в этой вкладке
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§c" + RpgLocale.get("menu.reset")),
                button -> StatsNetworking.sendResetRequest()).dimensions(panelX + padding, bottomY, btnWidth, btnHeight)
                .build());

        // Закрыть
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§f" + RpgLocale.get("menu.close")),
                button -> close()).dimensions(panelX + padding + btnWidth + btnSpacing, bottomY, btnWidth, btnHeight)
                .build());
    }

    // ==================== SKILLS TAB ====================

    private void buildSkillsButtons() {
        int buttonWidth = (int) (24 * uiScale);
        int buttonHeight = (int) (18 * uiScale);
        int buttonX = panelX + panelWidth - padding - buttonWidth;
        int startY = panelY + (int) (82 * uiScale);

        int index = 0;
        for (Ability ability : AbilityRegistry.getAll()) {
            int y = startY + (index * statRowHeight);
            String abilityId = ability.getId();
            this.addDrawableChild(ButtonWidget.builder(Text.literal("+"),
                    button -> StatsNetworking.sendUpgradeAbility(abilityId))
                    .dimensions(buttonX, y + (statRowHeight - buttonHeight) / 2, buttonWidth, buttonHeight).build());
            index++;
        }
    }

    private void buildSkillsBottomButtons() {
        int bottomY = panelY + panelHeight - (int) (32 * uiScale);
        int btnSpacing = (int) (8 * uiScale);
        int totalBtnWidth = panelWidth - padding * 2;
        int btnWidth = (totalBtnWidth - btnSpacing) / 2;
        int btnHeight = (int) (20 * uiScale);

        // Настройки
        this.addDrawableChild(ButtonWidget
                .builder(Text.literal("§7⚙ " + RpgLocale.get("settings.title").replace("⚙ ", "")),
                        button -> this.client.setScreen(new SettingsScreen(this)))
                .dimensions(panelX + padding, bottomY, btnWidth, btnHeight).build());

        // Закрыть
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§f" + RpgLocale.get("menu.close")),
                button -> close()).dimensions(panelX + padding + btnWidth + btnSpacing, bottomY, btnWidth, btnHeight)
                .build());
    }

    // ==================== MAGIC TAB ====================

    private void buildMagicTreeButtons(PlayerStatsData data) {
        MagicElement element = data.getElement();
        List<MagicAbility> abilities = MagicSkillRegistry.getAbilitiesForElement(element);

        if (abilities.isEmpty()) {
            return;
        }

        int centerX = panelX + panelWidth / 2;
        int startY = panelY + (int) (100 * uiScale);
        int buttonWidth = (int) (80 * uiScale);
        int buttonHeight = (int) (20 * uiScale);
        int tierSpacing = (int) (45 * uiScale);
        int branchSpacing = (int) (85 * uiScale);

        for (MagicAbility ability : abilities) {
            int x = ability.getTier() == 0 ? centerX - buttonWidth / 2
                    : centerX + (ability.getBranch() - 2) * branchSpacing - buttonWidth / 2;
            int y = startY + ability.getTier() * tierSpacing;

            int level = data.getMagicSkillLevel(ability.getId());
            boolean isMax = level >= ability.getMaxLevel();
            boolean meetsReqs = data.meetsRequirements(ability.getRequiredSkills());

            String prefix = isMax ? "§6✓ " : !meetsReqs ? "§8" : level > 0 ? "§a" : "";
            String skillId = ability.getId();

            String displayName = RpgLocale.getSkillName(skillId);
            int maxChars = (int) (buttonWidth / 6);
            if (displayName.length() > maxChars) {
                displayName = displayName.substring(0, maxChars - 2) + "..";
            }

            final String finalDisplayName = displayName;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(prefix + finalDisplayName),
                    button -> {
                        if (meetsReqs && !isMax) {
                            StatsNetworking.sendUpgradeMagicSkill(skillId);
                            cachedData = getPlayerData();
                            rebuildButtons();
                        }
                    }).dimensions(x, y, buttonWidth, buttonHeight).build());
        }
    }

    private void buildMagicBottomButtons() {
        int bottomY = panelY + panelHeight - (int) (32 * uiScale);
        int btnSpacing = (int) (8 * uiScale);
        int totalBtnWidth = panelWidth - padding * 2;
        int btnWidth = (totalBtnWidth - btnSpacing) / 2;
        int btnHeight = (int) (20 * uiScale);

        // Ресет стихии - только в этой вкладке
        String resetElementText = RpgConfig.get().isRussian() ? "⟲ Сброс стихии" : "⟲ Reset Element";
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§c" + resetElementText),
                button -> {
                    StatsNetworking.sendResetElement();
                    // После сброса открываем выбор элемента
                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient.getInstance().setScreen(new ElementSelectionScreen());
                    });
                }).dimensions(panelX + padding, bottomY, btnWidth, btnHeight).build());

        // Закрыть
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§f" + RpgLocale.get("menu.close")),
                button -> close()).dimensions(panelX + padding + btnWidth + btnSpacing, bottomY, btnWidth, btnHeight)
                .build());
    }

    // ==================== RENDER ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        cachedData = getPlayerData();
        if (cachedData == null) {
            super.render(context, mouseX, mouseY, delta);
            return;
        }

        MagicElement el = cachedData.getElement();
        context.fill(0, 0, this.width, this.height, 0xBB000000);

        context.fill(panelX + 4, panelY + 4, panelX + panelWidth + 4, panelY + panelHeight + 4, 0x60000000);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, el.bgDark);
        context.fill(panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + (int) (45 * uiScale), 0x15FFFFFF);
        RenderUtils.drawBorder(context, panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, el.borderPrimary);
        RenderUtils.drawBorder(context, panelX - 1, panelY - 1, panelWidth + 2, panelHeight + 2, el.borderSecondary);

        drawHeader(context, cachedData, el);

        if (currentTab == Tab.STATS)
            drawStats(context, cachedData, mouseX, mouseY, el);
        else if (currentTab == Tab.SKILLS)
            drawSkills(context, cachedData, mouseX, mouseY, el);
        else
            drawMagicTree(context, cachedData, el);

        super.render(context, mouseX, mouseY, delta);

        if (currentTab == Tab.MAGIC)
            drawMagicSkillLevels(context, cachedData, el);
    }

    private void drawHeader(DrawContext context, PlayerStatsData data, MagicElement el) {
        String elementIcon = data.hasElement() ? data.getElement().getIcon() + " " : "";
        String title = elementIcon + RpgLocale.get("menu.title");
        int titleWidth = this.textRenderer.getWidth(title);
        int maxTitleWidth = panelWidth - padding * 2;
        if (titleWidth > maxTitleWidth) {
            title = RpgLocale.get("menu.title");
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title),
                panelX + panelWidth / 2, panelY + (int) (6 * uiScale), el.textTitle);

        int infoY = panelY + (int) (18 * uiScale);
        String levelText = RpgLocale.get("menu.level") + data.getCurrentLevel();
        context.drawTextWithShadow(this.textRenderer, Text.literal(levelText),
                panelX + padding, infoY, el.textPrimary);

        String pointsText = RpgLocale.get("menu.sp") + data.getSkillPoints();
        int pointsWidth = this.textRenderer.getWidth(pointsText);
        context.drawTextWithShadow(this.textRenderer, Text.literal(pointsText),
                panelX + panelWidth - padding - pointsWidth, infoY, MagicElement.TEXT_SUCCESS);

        drawXpBar(context, data, el);
    }

    private void drawXpBar(DrawContext context, PlayerStatsData data, MagicElement el) {
        int barX = panelX + padding;
        int barY = panelY + (int) (32 * uiScale);
        int barWidth = panelWidth - padding * 2;
        int barHeight = (int) (6 * uiScale);

        context.fill(barX, barY, barX + barWidth, barY + barHeight, el.bgDark);
        RenderUtils.drawBorder(context, barX - 1, barY - 1, barWidth + 2, barHeight + 2, el.xpBarBorder);

        int filledWidth = (int) (barWidth * data.getXpProgress());
        if (filledWidth > 0) {
            context.fill(barX, barY, barX + filledWidth, barY + barHeight, el.xpBarFill);
            context.fill(barX, barY, barX + filledWidth, barY + 2, RenderUtils.brighten(el.xpBarFill, 40));
        }

        String xpText = data.getCurrentLevelXp() + "/" + data.getXpForNextLevel() + " XP";
        int textWidth = this.textRenderer.getWidth(xpText);
        int textX = barX + barWidth - textWidth;
        int textY = barY + barHeight + 2;

        context.drawTextWithShadow(this.textRenderer, Text.literal(xpText), textX, textY, el.textSecondary);
    }

    private void drawStats(DrawContext context, PlayerStatsData data, int mouseX, int mouseY, MagicElement el) {
        int startY = panelY + (int) (82 * uiScale);

        for (int i = 0; i < StatType.values().length; i++) {
            StatType stat = StatType.values()[i];
            int y = startY + (i * statRowHeight);
            int level = data.getStatLevel(stat);
            boolean isMax = level >= stat.getMaxLevel();
            boolean canUpgrade = data.canUpgrade(stat);
            boolean isHovered = mouseX >= panelX + padding && mouseX <= panelX + panelWidth - padding
                    && mouseY >= y && mouseY <= y + statRowHeight - 2;

            int rowBg = isHovered ? el.bgLight : (i % 2 == 0) ? el.bgMedium : el.bgDark;
            context.fill(panelX + padding, y, panelX + panelWidth - padding, y + statRowHeight - 2, rowBg);

            int statColor = el.getStatColor(stat);
            context.fill(panelX + padding, y, panelX + padding + 3, y + statRowHeight - 2, statColor);

            String icon = getStatIcon(stat);
            context.drawTextWithShadow(this.textRenderer, Text.literal(icon),
                    panelX + padding + 6, y + 4, statColor);

            String statName = RpgLocale.getStatName(stat.name());
            int maxNameWidth = (int) (60 * uiScale);
            int nameWidth = this.textRenderer.getWidth(statName);
            if (nameWidth > maxNameWidth) {
                while (this.textRenderer.getWidth(statName + "..") > maxNameWidth && statName.length() > 3) {
                    statName = statName.substring(0, statName.length() - 1);
                }
                statName += "..";
            }
            context.drawTextWithShadow(this.textRenderer, Text.literal(statName),
                    panelX + padding + 18, y + 4, el.textPrimary);

            int progBarX = panelX + padding + 18;
            int progBarY = y + (int) (14 * uiScale);
            int progBarWidth = (int) (55 * uiScale);
            int progBarHeight = (int) (4 * uiScale);

            context.fill(progBarX, progBarY, progBarX + progBarWidth, progBarY + progBarHeight, el.bgDark);
            int filled = (int) (progBarWidth * ((float) level / stat.getMaxLevel()));
            if (filled > 0) {
                context.fill(progBarX, progBarY, progBarX + filled, progBarY + progBarHeight, statColor);
            }
            RenderUtils.drawBorder(context, progBarX - 1, progBarY - 1, progBarWidth + 2, progBarHeight + 2,
                    el.borderSecondary);

            String levelStr = level + "/" + stat.getMaxLevel();
            context.drawTextWithShadow(this.textRenderer, Text.literal(levelStr),
                    progBarX + progBarWidth + 4, y + (int) (11 * uiScale), isMax ? el.textTitle : el.textSecondary);

            int costX = panelX + panelWidth - padding - (int) (50 * uiScale);
            if (!isMax) {
                String costStr = stat.getCostPerPoint() + "SP";
                context.drawTextWithShadow(this.textRenderer, Text.literal(costStr),
                        costX, y + 4, canUpgrade ? MagicElement.TEXT_WARNING : MagicElement.TEXT_ERROR);
            } else {
                context.drawTextWithShadow(this.textRenderer, Text.literal(RpgLocale.get("stat.max")),
                        costX, y + 4, el.textTitle);
            }
        }
    }

    private void drawSkills(DrawContext context, PlayerStatsData data, int mouseX, int mouseY, MagicElement el) {
        int startY = panelY + (int) (82 * uiScale);
        int index = 0;

        for (Ability ability : AbilityRegistry.getAll()) {
            int y = startY + (index * statRowHeight);
            int level = data.getAbilityLevel(ability.getId());
            boolean isMax = level >= ability.getMaxLevel();
            boolean canUpgrade = data.canUpgradeAbility(ability.getId(), ability.getMaxLevel(), 2);
            boolean isHovered = mouseX >= panelX + padding && mouseX <= panelX + panelWidth - padding
                    && mouseY >= y && mouseY <= y + statRowHeight - 2;

            int rowBg = isHovered ? el.bgLight : (index % 2 == 0) ? el.bgMedium : el.bgDark;
            context.fill(panelX + padding, y, panelX + panelWidth - padding, y + statRowHeight - 2, rowBg);

            int iconColor = level > 0 ? el.borderPrimary : 0xFF555555;
            context.fill(panelX + padding, y, panelX + padding + 3, y + statRowHeight - 2, iconColor);

            context.drawTextWithShadow(this.textRenderer, Text.literal(ability.getIcon()),
                    panelX + padding + 6, y + 4, iconColor);

            String abilityName = RpgLocale.getAbilityName(ability.getId());
            int maxNameWidth = (int) (65 * uiScale);
            if (this.textRenderer.getWidth(abilityName) > maxNameWidth) {
                while (this.textRenderer.getWidth(abilityName + "..") > maxNameWidth && abilityName.length() > 3) {
                    abilityName = abilityName.substring(0, abilityName.length() - 1);
                }
                abilityName += "..";
            }
            context.drawTextWithShadow(this.textRenderer, Text.literal(abilityName),
                    panelX + padding + 18, y + 4, el.textPrimary);

            String resourceType = ability.usesStamina() ? "§6⚡" : "§9✧";
            String info = resourceType + ability.getResourceCost() + " §7CD:" + ability.getCooldownSeconds() + "s";
            context.drawTextWithShadow(this.textRenderer, Text.literal(info),
                    panelX + padding + 18, y + (int) (13 * uiScale), el.textSecondary);

            String levelStr = level + "/" + ability.getMaxLevel();
            int levelX = panelX + panelWidth - padding - (int) (75 * uiScale);
            context.drawTextWithShadow(this.textRenderer, Text.literal(levelStr),
                    levelX, y + 4, isMax ? el.textTitle : el.textSecondary);

            int costX = panelX + panelWidth - padding - (int) (45 * uiScale);
            if (!isMax) {
                context.drawTextWithShadow(this.textRenderer, Text.literal("2SP"),
                        costX, y + 4, canUpgrade ? MagicElement.TEXT_WARNING : MagicElement.TEXT_ERROR);
            } else {
                context.drawTextWithShadow(this.textRenderer, Text.literal(RpgLocale.get("stat.max")),
                        costX, y + 4, el.textTitle);
            }
            index++;
        }
    }

    private void drawMagicTree(DrawContext context, PlayerStatsData data, MagicElement el) {
        String magicTitle = el.getIcon() + " " + RpgLocale.getElementName(el.name());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(magicTitle),
                panelX + panelWidth / 2, panelY + (int) (84 * uiScale), el.textTitle);

        List<MagicAbility> abilities = MagicSkillRegistry.getAbilitiesForElement(el);
        if (abilities.isEmpty()) {
            String noSkills = RpgConfig.get().isRussian() ? "Скиллы в разработке..." : "Skills coming soon...";
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7" + noSkills),
                    panelX + panelWidth / 2, panelY + panelHeight / 2, el.textSecondary);
            return;
        }

        // Рисуем линии древа
        int centerX = panelX + panelWidth / 2;
        int startY = panelY + (int) (100 * uiScale);
        int tierSpacing = (int) (45 * uiScale);
        int branchSpacing = (int) (85 * uiScale);
        int buttonHeight = (int) (20 * uiScale);

        // Находим максимальный тир
        int maxTier = abilities.stream().mapToInt(MagicAbility::getTier).max().orElse(0);

        if (maxTier >= 1) {
            // Вертикальная линия от базового скилла
            int baseY = startY + buttonHeight;
            int tier1Y = startY + tierSpacing;
            context.fill(centerX - 1, baseY, centerX + 1, tier1Y - 3, el.borderSecondary);

            // Горизонтальная линия
            List<MagicAbility> tier1Skills = MagicSkillRegistry.getAbilitiesByTier(el, 1);
            if (!tier1Skills.isEmpty()) {
                int minBranch = tier1Skills.stream().mapToInt(MagicAbility::getBranch).min().orElse(2);
                int maxBranch = tier1Skills.stream().mapToInt(MagicAbility::getBranch).max().orElse(2);
                int leftX = centerX + (minBranch - 2) * branchSpacing;
                int rightX = centerX + (maxBranch - 2) * branchSpacing;
                context.fill(leftX, tier1Y - 3, rightX, tier1Y - 1, el.borderSecondary);

                // Вертикальные линии к веткам
                for (MagicAbility ability : tier1Skills) {
                    int branchX = centerX + (ability.getBranch() - 2) * branchSpacing;
                    context.fill(branchX - 1, tier1Y - 3, branchX + 1, tier1Y, el.borderSecondary);
                }
            }
        }

        if (maxTier >= 2) {
            // Линии от tier 1 к tier 2
            List<MagicAbility> tier2Skills = MagicSkillRegistry.getAbilitiesByTier(el, 2);
            for (MagicAbility ability : tier2Skills) {
                int branchX = centerX + (ability.getBranch() - 2) * branchSpacing;
                int tier1Y = startY + tierSpacing + buttonHeight;
                int tier2Y = startY + 2 * tierSpacing;
                context.fill(branchX - 1, tier1Y, branchX + 1, tier2Y, el.borderSecondary);
            }
        }
    }

    private void drawMagicSkillLevels(DrawContext context, PlayerStatsData data, MagicElement el) {
        List<MagicAbility> abilities = MagicSkillRegistry.getAbilitiesForElement(el);
        if (abilities.isEmpty())
            return;

        int centerX = panelX + panelWidth / 2;
        int startY = panelY + (int) (100 * uiScale);
        int tierSpacing = (int) (45 * uiScale);
        int branchSpacing = (int) (85 * uiScale);
        int buttonHeight = (int) (20 * uiScale);

        for (MagicAbility ability : abilities) {
            int x = ability.getTier() == 0 ? centerX : centerX + (ability.getBranch() - 2) * branchSpacing;
            int y = startY + ability.getTier() * tierSpacing + buttonHeight + 2;
            int level = data.getMagicSkillLevel(ability.getId());
            String levelText = level + "/" + ability.getMaxLevel();
            int levelWidth = this.textRenderer.getWidth(levelText);
            int levelColor = level >= ability.getMaxLevel() ? el.textTitle
                    : level > 0 ? MagicElement.TEXT_SUCCESS : el.textSecondary;

            context.fill(x - levelWidth / 2 - 2, y - 1, x + levelWidth / 2 + 2, y + 9, 0x99000000);
            context.drawTextWithShadow(this.textRenderer, Text.literal(levelText),
                    x - levelWidth / 2, y, levelColor);
        }
    }

    private String getStatIcon(StatType stat) {
        return switch (stat) {
            case HEALTH -> "♥";
            case STRENGTH -> "⚔";
            case SPEED -> "»";
            case JUMP -> "↑";
            case MANA -> "✧";
            case STAMINA -> "⚡";
            case FORTUNE -> "◆";
            case LOOTING -> "★";
        };
    }

    private PlayerStatsData getPlayerData() {
        if (this.client != null && this.client.player instanceof IPlayerStatsAccessor accessor) {
            return accessor.rpg_getStatsData();
        }
        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == RpgConfig.get().getOpenMenuKey()) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}