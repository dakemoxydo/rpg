package com.example.rpg.effect;

import com.example.rpg.ability.Ability;
import com.example.rpg.ability.AbilityCooldownManager;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.MagicSkillRegistry;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgConfig;
import com.example.rpg.config.RpgLocale;
import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class RpgHudRenderer {

    // ==================== LEVEL UP EFFECT STATE ====================

    private static int levelUpTicksRemaining = 0;
    private static int levelUpLevel = 0;
    private static int levelUpMaxTicks = 0;
    private static final int LEVEL_UP_DISPLAY_TIME = 500;
    private static final int LEVEL_UP_FADE_IN = 120;
    private static final int LEVEL_UP_FADE_OUT = 120;

    // ==================== XP POPUP STATE ====================

    private static final List<XpPopupEntry> xpPopups = new ArrayList<>();
    private static final int MAX_POPUPS = 6;
    private static final int POPUP_DURATION = 200;

    // ==================== LAYOUT CONSTANTS ====================

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 12;
    private static final int BAR_SPACING = 6;
    private static final int BAR_MARGIN_RIGHT = 10;
    private static final int BAR_MARGIN_BOTTOM = 40;

    private static final int ICON_SIZE = 28;
    private static final int ICON_SPACING = 6;
    private static final int ICON_MARGIN_FROM_BARS = 12;

    private static final int LEVEL_UP_MARGIN_TOP = 20;
    private static final int POPUP_MARGIN_BOTTOM = 70;

    // Helper record for unified rendering
    private record AbilityView(String id, String name, String icon, int defaultKey, boolean usesStamina) {}

    // ==================== REGISTRATION ====================

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null || client.options.hudHidden) return;

            tickLevelUp();
            tickXpPopups();

            PlayerStatsData data = getPlayerData(client);
            if (data == null) return;

            renderResourceBars(drawContext, client, data);
            renderCooldowns(drawContext, client, data);
            renderXpPopups(drawContext, client, data);

            if (isLevelUpActive()) {
                renderLevelUpEffect(drawContext, client, data);
            }
        });
    }

    private static PlayerStatsData getPlayerData(MinecraftClient client) {
        if (client.player instanceof IPlayerStatsAccessor accessor) {
            return accessor.rpg_getStatsData();
        }
        return null;
    }

    // ==================== COOLDOWNS ====================

    private static void renderCooldowns(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        float uiScale = RenderUtils.getUiScale(screenWidth);

        List<AbilityView> activeAbilities = new ArrayList<>();

        // 1. Regular abilities
        for (Ability ability : AbilityRegistry.getAll()) {
            if (data.getAbilityLevel(ability.getId()) > 0) {
                activeAbilities.add(new AbilityView(
                        ability.getId(),
                        RpgLocale.getAbilityName(ability.getId()),
                        ability.getIcon(),
                        ability.getDefaultKey(),
                        ability.usesStamina()
                ));
            }
        }

        // 2. Magic abilities
        MagicElement element = data.getElement();
        if (element != MagicElement.NONE) {
            for (MagicAbility ability : MagicSkillRegistry.getAbilitiesForElement(element)) {
                if (ability.getManaCost() > 0 && data.getMagicSkillLevel(ability.getId()) > 0) {
                    activeAbilities.add(new AbilityView(
                            ability.getId(),
                            RpgLocale.getSkillName(ability.getId()),
                            ability.getIcon(),
                            ability.getDefaultKey(),
                            false
                    ));
                }
            }
        }

        if (activeAbilities.isEmpty()) return;

        int iconSize = (int)(ICON_SIZE * uiScale);
        int spacing = (int)(ICON_SPACING * uiScale);
        int marginFromBars = (int)(ICON_MARGIN_FROM_BARS * uiScale);

        int barHeight = (int)(BAR_HEIGHT * uiScale);
        int barSpacing = (int)(BAR_SPACING * uiScale);
        int barMarginRight = (int)(BAR_MARGIN_RIGHT * uiScale);
        int barMarginBottom = (int)(BAR_MARGIN_BOTTOM * uiScale);

        // Calculate Y position above resource bars
        int barsTotalHeight = barHeight * 2 + barSpacing + barMarginBottom;
        int startX = screenWidth - barMarginRight - iconSize;
        int startY = screenHeight - barsTotalHeight - marginFromBars - iconSize;

        UUID playerId = client.player.getUuid();
        int index = 0;

        for (AbilityView ability : activeAbilities) {
            int y = startY - index * (iconSize + spacing + (int)(12 * uiScale));
            renderAbilityIcon(context, textRenderer, ability, startX, y, iconSize, playerId, uiScale, element);
            index++;
        }
    }

    private static void renderAbilityIcon(DrawContext context, TextRenderer textRenderer,
                                          AbilityView ability, int x, int y, int iconSize,
                                          UUID playerId, float uiScale, MagicElement element) {
        boolean onCooldown = AbilityCooldownManager.isOnCooldown(playerId, ability.id);
        float cdProgress = AbilityCooldownManager.getCooldownProgress(playerId, ability.id);

        int bgColor = onCooldown ? 0xEE2a1515 : (ability.usesStamina ? 0xEE222222 : (0xEE000000 | (element.bgMedium & 0x00FFFFFF)));
        int borderColor = 0xFF882222;

        if (!onCooldown) {
            float pulse = (System.currentTimeMillis() % 2000) / 2000f;
            int baseBorder = ability.usesStamina ? 0xFFFFAA00 : element.borderPrimary;
            borderColor = RenderUtils.blendColors(0xFF444444, baseBorder, (float)(Math.sin(pulse * Math.PI * 2) * 0.5 + 0.5));
        }

        context.fill(x, y, x + iconSize, y + iconSize, bgColor);
        RenderUtils.drawBorder(context, x - 1, y - 1, iconSize + 2, iconSize + 2, borderColor);

        int iconWidth = textRenderer.getWidth(ability.icon);
        int iconColor = onCooldown ? 0xFF888888 : 0xFFFFFFFF;
        context.drawTextWithShadow(textRenderer, ability.icon, x + (iconSize - iconWidth) / 2, y + (iconSize - 8) / 2, iconColor);

        if (cdProgress > 0) {
            int cdHeight = (int)(iconSize * cdProgress);
            context.fill(x + 1, y + 1, x + iconSize - 1, y + 1 + cdHeight, 0xAA000000);

            int remaining = (int)Math.ceil(AbilityCooldownManager.getCooldown(playerId, ability.id) / 20.0);
            String cdText = String.valueOf(remaining);
            context.drawTextWithShadow(textRenderer, cdText,
                    x + (iconSize - textRenderer.getWidth(cdText)) / 2, y + (iconSize - 8) / 2, 0xFFFF5555);
        }

        String keyName = RpgConfig.getKeyName(RpgConfig.get().getKeybind(ability.id, ability.defaultKey));
        if (!keyName.isEmpty()) {
            keyName = keyName.length() > 3 ? keyName.substring(0, 3) : keyName;
            int keyWidth = textRenderer.getWidth(keyName);
            int keyBgX = x - keyWidth - 10;
            int keyBgY = y + (iconSize - 12) / 2;

            context.fill(keyBgX, keyBgY - 1, x - 3, keyBgY + 11, 0xBB000000);
            RenderUtils.drawBorder(context, keyBgX, keyBgY - 1, x - 3 - keyBgX, 12, 0xFF555555);
            context.drawTextWithShadow(textRenderer, keyName, keyBgX + 3, keyBgY + 1, 0xFFCCCCCC);
        }

        int nameY = y + iconSize + (int)(3 * uiScale);
        String displayName = ability.name;
        if (textRenderer.getWidth(displayName) > iconSize + 20) displayName = displayName.substring(0, 3) + "..";
        int nameX = x + (iconSize - textRenderer.getWidth(displayName)) / 2;

        context.fill(nameX - 2, nameY - 1, nameX + textRenderer.getWidth(displayName) + 2, nameY + 9, 0x99000000);
        context.drawTextWithShadow(textRenderer, displayName, nameX, nameY, 0xFFAAAAAA);
    }

    // ==================== RESOURCE BARS ====================

    private static void renderResourceBars(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        MagicElement element = data.getElement();
        float uiScale = RenderUtils.getUiScale(screenWidth);

        int barWidth = (int)(BAR_WIDTH * uiScale);
        int barHeight = (int)(BAR_HEIGHT * uiScale);
        int spacing = (int)(BAR_SPACING * uiScale);
        int marginRight = (int)(BAR_MARGIN_RIGHT * uiScale);
        int marginBottom = (int)(BAR_MARGIN_BOTTOM * uiScale);

        int baseX = screenWidth - barWidth - marginRight;
        int baseY = screenHeight - marginBottom;

        renderBar(context, textRenderer, baseX, baseY, barWidth, barHeight,
                data.getStaminaProgress(), (int) data.getCurrentStamina(), data.getMaxStamina(),
                element.bgDark, element.getStaminaColor(), "⚡", uiScale);

        renderBar(context, textRenderer, baseX, baseY - barHeight - spacing, barWidth, barHeight,
                data.getManaProgress(), (int) data.getCurrentMana(), data.getMaxMana(),
                element.bgDark, element.getManaColor(), "✧", uiScale);
    }

    private static void renderBar(DrawContext context, TextRenderer textRenderer,
                                  int x, int y, int width, int height,
                                  float progress, int current, int max,
                                  int bgColor, int fillColor, String icon, float uiScale) {
        int iconOffset = (int)(14 * uiScale);

        context.drawTextWithShadow(textRenderer, icon, x - iconOffset, y + (height - 8) / 2, fillColor);
        context.fill(x, y, x + width, y + height, 0xDD000000 | (bgColor & 0x00FFFFFF));

        int filledWidth = (int)(width * Math.min(1.0f, progress));
        if (filledWidth > 0) {
            context.fill(x, y, x + filledWidth, y + height, fillColor);
            context.fill(x, y, x + filledWidth, y + 2, RenderUtils.brighten(fillColor, 50));
            context.fill(x, y + height - 2, x + filledWidth, y + height, RenderUtils.darken(fillColor, 40));
        }

        RenderUtils.drawBorder(context, x - 1, y - 1, width + 2, height + 2, RenderUtils.darken(fillColor, 80));

        String valueText = current + "/" + max;
        int textWidth = textRenderer.getWidth(valueText);

        if (textWidth < width - 4) {
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 7) / 2;
            context.drawText(textRenderer, valueText, textX + 1, textY + 1, 0x55000000, false);
            context.drawText(textRenderer, valueText, textX, textY, 0xFFFFFFFF, false);
        } else {
            int textX = x + width + 4;
            int textY = y + (height - 7) / 2;
            context.drawTextWithShadow(textRenderer, valueText, textX, textY, 0xFFCCCCCC);
        }
    }

    // ==================== LEVEL UP EFFECT ====================

    public static void triggerLevelUp(int level) {
        levelUpTicksRemaining = LEVEL_UP_DISPLAY_TIME;
        levelUpMaxTicks = LEVEL_UP_DISPLAY_TIME;
        levelUpLevel = level;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private static void tickLevelUp() {
        if (levelUpTicksRemaining > 0) levelUpTicksRemaining--;
    }

    private static boolean isLevelUpActive() {
        return levelUpTicksRemaining > 0 && getLevelUpAlpha() > 0.02f;
    }

    private static float getLevelUpAlpha() {
        if (levelUpTicksRemaining <= 0 || levelUpMaxTicks <= 0) return 0f;
        int elapsed = levelUpMaxTicks - levelUpTicksRemaining;
        if (elapsed < LEVEL_UP_FADE_IN) return Math.min(1f, (float) elapsed / LEVEL_UP_FADE_IN);
        if (levelUpTicksRemaining < LEVEL_UP_FADE_OUT) return Math.min(1f, (float) levelUpTicksRemaining / LEVEL_UP_FADE_OUT);
        return 1.0f;
    }

    private static float getLevelUpScale() {
        int elapsed = levelUpMaxTicks - levelUpTicksRemaining;
        if (elapsed < 6) return 0.3f + (elapsed / 6.0f) * 0.9f;
        if (elapsed < 12) return 1.2f - ((elapsed - 6) / 6.0f) * 0.2f;
        return 1.0f;
    }

    private static void renderLevelUpEffect(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();

        float alpha = getLevelUpAlpha();
        if (alpha < 0.03f) return;

        MagicElement element = (data != null) ? data.getElement() : MagicElement.NONE;
        float scale = getLevelUpScale();
        float uiScale = RenderUtils.getUiScale(screenWidth);

        int panelWidth = (int)(260 * uiScale);
        int panelHeight = (int)(90 * uiScale);
        int centerX = screenWidth / 2;
        int topMargin = (int)(LEVEL_UP_MARGIN_TOP * uiScale);
        int centerY = topMargin + panelHeight / 2;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-centerX, -centerY, 0);

        int panelX = centerX - panelWidth / 2;
        int panelY = topMargin;

        int bgAlpha = (int)(alpha * 245);
        if (bgAlpha > 0) {
            int bgColor = (bgAlpha << 24) | (element.bgDark & 0x00FFFFFF);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgColor);
        }

        if (alpha > 0.1f) {
            int primaryBorder = RenderUtils.withAlpha(element.borderPrimary, alpha);
            RenderUtils.drawBorder(context, panelX - 3, panelY - 3, panelWidth + 6, panelHeight + 6, primaryBorder);
        }

        if (alpha > 0.05f) {
            int titleColor = RenderUtils.withAlpha(element.textTitle, alpha);
            int primaryColor = RenderUtils.withAlpha(element.textPrimary, alpha);

            String title = RpgLocale.get("levelup.title");
            context.drawCenteredTextWithShadow(textRenderer, title, centerX, panelY + (int)(22 * uiScale), titleColor);

            String levelText = RpgLocale.get("levelup.level") + " " + levelUpLevel;
            context.drawCenteredTextWithShadow(textRenderer, levelText, centerX, panelY + (int)(38 * uiScale),
                    RenderUtils.withAlpha(MagicElement.TEXT_SUCCESS, alpha));

            String hint = RpgLocale.get("levelup.skillpoint");
            context.drawCenteredTextWithShadow(textRenderer, hint, centerX, panelY + (int)(55 * uiScale), primaryColor);
        }

        matrices.pop();
    }

    // ==================== XP POPUP ====================

    public static void addXpPopup(int xpAmount, String source) {
        while (xpPopups.size() >= MAX_POPUPS) xpPopups.remove(0);
        for (XpPopupEntry popup : xpPopups) popup.targetOffsetY += 16;
        xpPopups.add(new XpPopupEntry(xpAmount, source, POPUP_DURATION));
    }

    private static void tickXpPopups() {
        Iterator<XpPopupEntry> iter = xpPopups.iterator();
        while (iter.hasNext()) {
            XpPopupEntry popup = iter.next();
            popup.ticksRemaining--;
            popup.currentOffsetY += (popup.targetOffsetY - popup.currentOffsetY) * 0.12f;
            popup.targetOffsetY += 0.03f;
            if (popup.ticksRemaining <= 0 || popup.getAlpha() < 0.02f) iter.remove();
        }
    }

    private static void renderXpPopups(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        if (xpPopups.isEmpty()) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        float uiScale = RenderUtils.getUiScale(screenWidth);

        MagicElement element = (data != null) ? data.getElement() : MagicElement.NONE;
        int baseX = screenWidth / 2;
        int baseY = screenHeight - (int)(POPUP_MARGIN_BOTTOM * uiScale);

        for (XpPopupEntry popup : xpPopups) {
            float alpha = popup.getAlpha();
            if (alpha < 0.03f) continue;

            int y = (int)(baseY - popup.currentOffsetY);
            String xpText = "+" + popup.xpAmount + " " + RpgLocale.get("xp.popup");
            String translatedSource = (popup.source != null && !popup.source.isEmpty()) ?
                    RpgLocale.getXpSource(popup.source) : "";
            String sourceText = !translatedSource.isEmpty() ? " (" + translatedSource + ")" : "";

            int xpWidth = textRenderer.getWidth(xpText);
            int sourceWidth = textRenderer.getWidth(sourceText);
            int totalWidth = xpWidth + sourceWidth;
            int textX = baseX - totalWidth / 2;

            int bgAlpha = (int)(alpha * 160);
            if (bgAlpha > 0) {
                int bgColor = (bgAlpha << 24) | (element.bgDark & 0x00FFFFFF);
                context.fill(textX - 6, y - 3, textX + totalWidth + 6, y + 14, bgColor);
            }

            int xpColor = popup.xpAmount >= 50 ? element.textTitle :
                    popup.xpAmount >= 20 ? MagicElement.TEXT_SUCCESS : element.textPrimary;
            context.drawTextWithShadow(textRenderer, xpText, textX, y, RenderUtils.withAlpha(xpColor, alpha));

            if (!sourceText.isEmpty()) {
                context.drawTextWithShadow(textRenderer, sourceText, textX + xpWidth, y,
                        RenderUtils.withAlpha(element.textSecondary, alpha));
            }
        }
    }

    private static class XpPopupEntry {
        int xpAmount;
        String source;
        int ticksRemaining;
        float currentOffsetY = -12;
        float targetOffsetY = 0;

        XpPopupEntry(int xpAmount, String source, int duration) {
            this.xpAmount = xpAmount;
            this.source = source;
            this.ticksRemaining = duration;
        }

        float getAlpha() {
            if (ticksRemaining <= 0) return 0f;
            int elapsed = POPUP_DURATION - ticksRemaining;
            if (elapsed < 8) return Math.min(1f, elapsed / 8.0f);
            if (ticksRemaining < 50) return Math.min(1f, ticksRemaining / 50.0f);
            return 1.0f;
        }
    }
}