package com.example.rpg.effect;

import com.example.rpg.ability.AbilityCooldownManager;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.Ability;
import com.example.rpg.ability.IAbility;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgConfig;
import com.example.rpg.config.RpgLocale;
import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.utils.RenderUtils;
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

    // ==================== COMBO HUD STATE ====================

    private static float comboDamage = 0f;
    private static int comboTicksRemaining = 0;
    private static final int COMBO_MAX_TICKS = 100; // 5 seconds
    private static float comboScale = 1.0f;

    // ==================== LAYOUT CONSTANTS ====================

    private static final int ICON_SIZE = 28;
    private static final int ICON_SPACING = 6;
    private static final int POPUP_MARGIN_BOTTOM = 70;

    // ==================== REGISTRATION ====================

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null || client.options.hudHidden)
                return;

            tickLevelUp();
            tickXpPopups();
            tickCombo();

            PlayerStatsData data = getPlayerData(client);
            if (data == null)
                return;

            renderResourceBars(drawContext, client, data);
            renderCooldowns(drawContext, client, data);
            renderXpPopups(drawContext, client, data);
            renderComboHUD(drawContext, client, data);

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

        // Собираем активные способности через единый реестр
        List<IAbility> activeAbilities = new ArrayList<>();

        // 1. Физические способности
        for (Ability ability : AbilityRegistry.getPhysicalAbilities()) {
            if (data.hasSkill(ability.getId())) {
                activeAbilities.add(ability);
            }
        }

        // 2. Магические способности выбранной стихии
        MagicElement element = data.getElement();
        if (element != MagicElement.NONE) {
            for (MagicAbility ability : AbilityRegistry.getMagicAbilities(element)) {
                if (ability.getManaCost() > 0 && data.hasSkill(ability.getId())) {
                    activeAbilities.add(ability);
                }
            }
        }

        if (activeAbilities.isEmpty())
            return;

        int iconSize = (int) (ICON_SIZE * uiScale);
        int spacing = (int) (ICON_SPACING * uiScale);

        // Calculate vertical position on the right edge of the screen
        int totalAbilitiesHeight = activeAbilities.size() * (iconSize + spacing) - spacing;
        int startX = screenWidth - iconSize - (int) (15 * uiScale);
        int startY = (screenHeight - totalAbilitiesHeight) / 2;

        // Draw sleek minimal dock background
        int scaledPadding = (int) (6 * uiScale);
        int dockLeft = startX - scaledPadding;
        int dockRight = startX + iconSize + scaledPadding;
        int dockTop = startY - scaledPadding;
        int dockBottom = startY + totalAbilitiesHeight + scaledPadding;

        // Premium curved/rounded dock look
        context.fillGradient(dockLeft - 2, dockTop - 2, dockRight + 2, dockBottom + 2, 0x66000000, 0x00000000);
        int dockBgTop = element != MagicElement.NONE ? RenderUtils.withAlpha(element.bgDark, 0.85f) : 0xDD050505;
        int dockBgBot = element != MagicElement.NONE ? RenderUtils.withAlpha(element.bgMedium, 0.5f) : 0x88111111;
        context.fillGradient(dockLeft, dockTop, dockRight, dockBottom, dockBgTop, dockBgBot);
        int dockBorder = element != MagicElement.NONE ? RenderUtils.withAlpha(element.borderSecondary, 0.8f)
                : 0xFF333333;
        RenderUtils.drawBorder(context, dockLeft - 1, dockTop - 1, dockRight - dockLeft + 2, dockBottom - dockTop + 2,
                dockBorder);

        // Glowing rim
        context.fillGradient(dockLeft, dockTop, dockLeft + 1, dockBottom, element.borderPrimary, 0x00000000);
        context.fillGradient(dockLeft + 1, dockTop, dockLeft + 2, dockBottom,
                RenderUtils.withAlpha(element.borderSecondary, 0.5f), 0x00000000);

        UUID playerId = client.player.getUuid();
        int index = 0;

        for (IAbility ability : activeAbilities) {
            int y = startY + index * (iconSize + spacing);
            int level = data.getSkillLevel(ability.getId());
            renderAbilityIcon(context, textRenderer, ability, startX, y, iconSize, playerId, level, uiScale, element,
                    data);
            index++;
        }
    }

    private static void renderAbilityIcon(DrawContext context, TextRenderer textRenderer,
            IAbility ability, int x, int y, int iconSize,
            UUID playerId, int level, float uiScale, MagicElement element, PlayerStatsData data) {
        boolean onCooldown = AbilityCooldownManager.isOnCooldown(playerId, ability.getId());
        float cdProgress = AbilityCooldownManager.getCooldownProgress(playerId, ability.getId(), level, data);

        int topBgColor = onCooldown ? 0xDD111111
                : (ability.usesStamina() ? 0xDD2a2a2a : (0xDD000000 | (element.bgMedium & 0x00FFFFFF)));
        int bottomBgColor = onCooldown ? 0xEE050505
                : (ability.usesStamina() ? 0xEE111111 : (0xEE000000 | (element.bgDark & 0x00FFFFFF)));

        int borderColor = RenderUtils.withAlpha(ability.getThemeColor(), 0.4f);

        if (!onCooldown) {
            float pulse = (System.currentTimeMillis() % 2000) / 2000f;
            borderColor = RenderUtils.blendColors(0xFF444444, ability.getThemeColor(),
                    (float) (Math.sin(pulse * Math.PI * 2) * 0.5 + 0.5));
        }

        context.fillGradient(x, y, x + iconSize, y + iconSize, topBgColor, bottomBgColor);
        RenderUtils.drawBorder(context, x - 1, y - 1, iconSize + 2, iconSize + 2, borderColor);

        int iconWidth = textRenderer.getWidth(ability.getIcon());
        int iconColor = onCooldown ? 0xFF666666 : 0xFFFFFFFF;

        // Drop shadow
        int shadowOffset = Math.max(1, (int) (1 * uiScale));
        context.drawText(textRenderer, ability.getIcon(), x + (iconSize - iconWidth) / 2 + shadowOffset,
                y + (iconSize - 8) / 2 + shadowOffset,
                0xAA000000, false);
        context.drawTextWithShadow(textRenderer, ability.getIcon(), x + (iconSize - iconWidth) / 2,
                y + (iconSize - 8) / 2,
                iconColor);

        // Cooldown overlay
        if (cdProgress > 0) {
            int cdHeight = (int) (iconSize * cdProgress);
            context.fillGradient(x + 1, y + iconSize - cdHeight, x + iconSize - 1, y + iconSize - 1, 0xDD000000,
                    0xAA000000);

            int remaining = (int) Math.ceil(AbilityCooldownManager.getCooldown(playerId, ability.getId()) / 20.0);
            String cdText = String.valueOf(remaining);
            context.drawTextWithShadow(textRenderer, cdText,
                    x + (iconSize - textRenderer.getWidth(cdText)) / 2, y + (iconSize - 8) / 2, 0xFFFF5555);
        }

        String keyName = RpgConfig.getKeyName(RpgConfig.get().getKeybind(ability.getId(), ability.getDefaultKey()));
        if (!keyName.isEmpty()) {
            keyName = keyName.length() > 3 ? keyName.substring(0, 3) : keyName;
            int keyWidth = textRenderer.getWidth(keyName);
            int keyBgX = x - keyWidth - (int) (5 * uiScale);
            int keyBgY = y + (iconSize - 8) / 2;

            context.drawTextWithShadow(textRenderer, keyName, keyBgX, keyBgY, 0xFFAAAAAA);
        }
    }

    // ==================== COMBO HUD ====================

    public static void addComboDamage(float damage) {
        if (comboTicksRemaining <= 0) {
            comboDamage = 0f;
        }
        comboDamage += damage;
        comboTicksRemaining = COMBO_MAX_TICKS;
        comboScale = 1.3f; // Bounce effect
    }

    private static void tickCombo() {
        if (comboTicksRemaining > 0) {
            comboTicksRemaining--;
        }
        if (comboScale > 1.0f) {
            comboScale -= 0.05f;
            if (comboScale < 1.0f)
                comboScale = 1.0f;
        }
    }

    private static void renderComboHUD(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        if (comboTicksRemaining <= 0 || comboDamage <= 0.01f)
            return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        float uiScale = RenderUtils.getUiScale(screenWidth);
        MagicElement element = (data != null) ? data.getElement() : MagicElement.NONE;

        float alpha = 1.0f;
        if (comboTicksRemaining < 20) {
            alpha = comboTicksRemaining / 20.0f;
        }

        int startX = (int) (20 * uiScale);
        int startY = screenHeight / 2 - (int) (10 * uiScale);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(startX, startY, 0);
        matrices.scale(comboScale * uiScale * 1.5f, comboScale * uiScale * 1.5f, 1.0f);

        String dmgStr = String.format(java.util.Locale.US, "%.1f", comboDamage);
        if (dmgStr.endsWith(".0"))
            dmgStr = dmgStr.substring(0, dmgStr.length() - 2);

        int textColor = RenderUtils.withAlpha(element != MagicElement.NONE ? element.textTitle : 0xFFFFFFFF, alpha);

        context.drawTextWithShadow(textRenderer, "Combo", 0, -10,
                RenderUtils.withAlpha(element != MagicElement.NONE ? element.textSecondary : 0xFFAAAAAA, alpha));
        context.drawTextWithShadow(textRenderer, dmgStr, 0, 0, textColor);

        matrices.pop();

        int barWidth = (int) (60 * uiScale);
        int barHeight = Math.max(2, (int) (3 * uiScale));
        float progress = comboTicksRemaining / (float) COMBO_MAX_TICKS;

        int barY = startY + (int) (25 * uiScale);

        int bgAlpha = (int) (alpha * 120);
        context.fillGradient(startX, barY, startX + barWidth, barY + barHeight, (bgAlpha << 24) | 0x000000,
                (bgAlpha << 24) | 0x000000);

        int fillAlpha = (int) (alpha * 220);
        int elementColor = element != MagicElement.NONE ? element.manaColor : 0xAAAAAA;
        int fillColor = (fillAlpha << 24) | (elementColor & 0xFFFFFF);

        context.fillGradient(startX, barY, startX + (int) (barWidth * progress), barY + barHeight, fillColor,
                RenderUtils.darken(fillColor, 20));
    }

    // ==================== RESOURCE BARS ====================

    private static void renderResourceBars(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        MagicElement element = data.getElement();
        float uiScale = RenderUtils.getUiScale(screenWidth);

        int barWidth = (int) (110 * uiScale);
        int barHeight = Math.max(4, (int) (6 * uiScale));

        int hotbarOffset = 91 + (int) (15 * uiScale);
        int baseY = screenHeight - (int) (18 * uiScale);

        // Left Bar: Stamina
        int staminaX = (screenWidth / 2) - hotbarOffset - barWidth;
        String staminaLabel = RpgLocale.get("stat.stamina");
        if (staminaLabel.equals("stat.stamina"))
            staminaLabel = "Stamina";
        int staminaLabelColor = RenderUtils.brighten(element.staminaColor, 40);
        context.drawTextWithShadow(textRenderer, staminaLabel, staminaX, baseY - (int) (14 * uiScale),
                staminaLabelColor);
        // Numeric value above bar (right-aligned)
        String staminaVal = (int) data.getCurrentStamina() + "/" + data.getMaxStamina();
        int staminaValX = staminaX + barWidth - textRenderer.getWidth(staminaVal);
        context.drawTextWithShadow(textRenderer, staminaVal, staminaValX, baseY - (int) (14 * uiScale), 0xFFCCCCCC);

        renderHorizontalBar(context, textRenderer, staminaX, baseY, barWidth, barHeight,
                data.getStaminaProgress(), (int) data.getCurrentStamina(), data.getMaxStamina(),
                element.bgDark, element.staminaColor, uiScale, false);

        // Right Bar: Mana
        int manaX = (screenWidth / 2) + hotbarOffset;
        String manaLabel = RpgLocale.get("stat.mana");
        if (manaLabel.equals("stat.mana"))
            manaLabel = "Mana";
        int manaLabelColor = RenderUtils.brighten(element.manaColor, 40);
        // Numeric value above bar (left-aligned)
        String manaVal = (int) data.getCurrentMana() + "/" + data.getMaxMana();
        context.drawTextWithShadow(textRenderer, manaVal, manaX, baseY - (int) (14 * uiScale), 0xFFCCCCCC);
        // Label (right-aligned)
        context.drawTextWithShadow(textRenderer, manaLabel, manaX + barWidth - textRenderer.getWidth(manaLabel),
                baseY - (int) (14 * uiScale), manaLabelColor);

        renderHorizontalBar(context, textRenderer, manaX, baseY, barWidth, barHeight,
                data.getManaProgress(), (int) data.getCurrentMana(), data.getMaxMana(),
                element.bgDark, element.manaColor, uiScale, true);
    }

    private static void renderHorizontalBar(DrawContext context, TextRenderer textRenderer,
            int x, int y, int width, int height,
            float progress, int current, int max,
            int bgColor, int fillColor, float uiScale, boolean fillFromLeft) {

        context.fillGradient(x - 2, y - 2, x + width + 2, y + height + 2, 0x88000000, 0x00000000);
        context.fillGradient(x, y, x + width, y + height, 0xDD020202, 0xAA111111);
        RenderUtils.drawBorder(context, x - 1, y - 1, width + 2, height + 2, 0xCC333333);

        int filledWidth = (int) (width * Math.min(1.0f, Math.max(0, progress)));
        if (filledWidth > 0) {
            int fillX = fillFromLeft ? x : (x + width - filledWidth);
            context.fillGradient(fillX, y, fillX + filledWidth, y + height, fillColor,
                    RenderUtils.darken(fillColor, 40));
            context.fillGradient(fillX, y, fillX + filledWidth, y + 1, RenderUtils.brighten(fillColor, 80),
                    RenderUtils.brighten(fillColor, 40));
            context.fill(fillX, y + height - 1, fillX + filledWidth, y + height, 0x55000000);

            int edgeX = fillFromLeft ? (fillX + filledWidth - 1) : fillX;
            context.fill(edgeX, y, edgeX + 1, y + height, 0xFFFFFFFF);
            context.fillGradient(edgeX - 1, y - 1, edgeX + 2, y + height + 1, RenderUtils.withAlpha(fillColor, 0.9f),
                    0x00000000);
        }

        // Segmented tick marks
        int segments = 10;
        for (int i = 1; i < segments; i++) {
            int tickX = x + (width * i / segments);
            context.fill(tickX, y + 1, tickX + 1, y + height - 1, 0x77000000);
            context.fill(tickX + 1, y + 1, tickX + 2, y + height - 1, 0x22FFFFFF);
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
        if (levelUpTicksRemaining > 0)
            levelUpTicksRemaining--;
    }

    private static boolean isLevelUpActive() {
        return levelUpTicksRemaining > 0 && getLevelUpAlpha() > 0.02f;
    }

    private static float getLevelUpAlpha() {
        if (levelUpTicksRemaining <= 0 || levelUpMaxTicks <= 0)
            return 0f;
        int elapsed = levelUpMaxTicks - levelUpTicksRemaining;
        if (elapsed < LEVEL_UP_FADE_IN)
            return Math.min(1f, (float) elapsed / LEVEL_UP_FADE_IN);
        if (levelUpTicksRemaining < LEVEL_UP_FADE_OUT)
            return Math.min(1f, (float) levelUpTicksRemaining / LEVEL_UP_FADE_OUT);
        return 1.0f;
    }

    private static float getLevelUpScale() {
        int elapsed = levelUpMaxTicks - levelUpTicksRemaining;
        if (elapsed < 6)
            return 0.3f + (elapsed / 6.0f) * 0.9f;
        if (elapsed < 12)
            return 1.2f - ((elapsed - 6) / 6.0f) * 0.2f;
        return 1.0f;
    }

    private static void renderLevelUpEffect(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        float alpha = getLevelUpAlpha();
        if (alpha < 0.03f)
            return;

        float scale = getLevelUpScale();
        float uiScale = RenderUtils.getUiScale(screenWidth);

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 - (int) (20 * uiScale);

        int bgAlphaStart = (int) (alpha * 120) << 24;
        int bgAlphaEnd = (int) (alpha * 220) << 24;
        if (alpha > 0) {
            context.fillGradient(0, 0, screenWidth, screenHeight, bgAlphaStart, bgAlphaEnd);
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.scale(scale * 2.5f * uiScale, scale * 2.5f * uiScale, 1.0f);
        matrices.translate(-centerX, -centerY, 0);

        if (alpha > 0.05f) {
            String title = RpgLocale.get("levelup.title");
            context.drawCenteredTextWithShadow(textRenderer, title, centerX, centerY - 15,
                    RenderUtils.withAlpha(0xFFFFD700, alpha));
        }
        matrices.pop();

        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.scale(scale * 1.5f * uiScale, scale * 1.5f * uiScale, 1.0f);
        matrices.translate(-centerX, -centerY, 0);

        if (alpha > 0.05f) {
            String levelText = RpgLocale.get("levelup.level") + " " + levelUpLevel;
            context.drawCenteredTextWithShadow(textRenderer, levelText, centerX, centerY + 10,
                    RenderUtils.withAlpha(0xFFFFFFFF, alpha));

            String hint = RpgLocale.get("levelup.skillpoint");
            context.drawCenteredTextWithShadow(textRenderer, hint, centerX, centerY + 30,
                    RenderUtils.withAlpha(0xFF55FF55, alpha));
        }

        matrices.pop();
    }

    // ==================== XP POPUP ====================

    public static void addXpPopup(int xpAmount, String source) {
        while (xpPopups.size() >= MAX_POPUPS)
            xpPopups.remove(0);
        for (XpPopupEntry popup : xpPopups)
            popup.targetOffsetY += 16;
        xpPopups.add(new XpPopupEntry(xpAmount, source, POPUP_DURATION));

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            float pitch = 0.9f + (client.world.random.nextFloat() * 0.4f);
            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, pitch);
        }
    }

    private static void tickXpPopups() {
        Iterator<XpPopupEntry> iter = xpPopups.iterator();
        while (iter.hasNext()) {
            XpPopupEntry popup = iter.next();
            popup.ticksRemaining--;
            popup.currentOffsetY += (popup.targetOffsetY - popup.currentOffsetY) * 0.12f;
            popup.targetOffsetY += 0.03f;
            if (popup.ticksRemaining <= 0 || popup.getAlpha() < 0.02f)
                iter.remove();
        }
    }

    private static void renderXpPopups(DrawContext context, MinecraftClient client, PlayerStatsData data) {
        if (xpPopups.isEmpty())
            return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        float uiScale = RenderUtils.getUiScale(screenWidth);

        MagicElement element = (data != null) ? data.getElement() : MagicElement.NONE;
        int baseX = screenWidth / 2;
        int baseY = screenHeight - (int) (POPUP_MARGIN_BOTTOM * uiScale);

        for (XpPopupEntry popup : xpPopups) {
            float alpha = popup.getAlpha();
            if (alpha < 0.03f)
                continue;

            int y = (int) (baseY - popup.currentOffsetY);
            String xpText = "+" + popup.xpAmount + " " + RpgLocale.get("xp.popup");
            String translatedSource = (popup.source != null && !popup.source.isEmpty())
                    ? RpgLocale.getXpSource(popup.source)
                    : "";
            String sourceText = !translatedSource.isEmpty() ? " (" + translatedSource + ")" : "";

            int xpWidth = textRenderer.getWidth(xpText);
            int sourceWidth = textRenderer.getWidth(sourceText);
            int totalWidth = xpWidth + sourceWidth;
            int textX = baseX - totalWidth / 2;

            float popScale = popup.getScale();

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(baseX, y + 8, 0); // Center translation for scale
            matrices.scale(popScale, popScale, 1.0f);
            matrices.translate(-baseX, -(y + 8), 0);

            int bgAlpha = (int) (alpha * 160);
            if (bgAlpha > 0) {
                int bgColorTop = (bgAlpha << 24) | (element.bgDark & 0x00FFFFFF);
                int bgColorBottom = (bgAlpha << 24) | 0x00111111;
                context.fillGradient(textX - 8, y - 4, textX + totalWidth + 8, y + 15, bgColorTop, bgColorBottom);
                RenderUtils.drawBorder(context, textX - 8, y - 4, totalWidth + 16, 19,
                        RenderUtils.withAlpha(element.borderSecondary, alpha));
            }

            int xpColor = popup.xpAmount >= 50 ? element.textTitle
                    : popup.xpAmount >= 20 ? MagicElement.TEXT_SUCCESS : element.textPrimary;
            if (popup.xpAmount >= 50) {
                context.drawText(textRenderer, xpText, textX, y, RenderUtils.withAlpha(xpColor, alpha * 0.4f), false);
            }
            context.drawTextWithShadow(textRenderer, xpText, textX, y, RenderUtils.withAlpha(xpColor, alpha));

            if (!sourceText.isEmpty()) {
                context.drawTextWithShadow(textRenderer, sourceText, textX + xpWidth, y,
                        RenderUtils.withAlpha(element.textSecondary, alpha));
            }

            matrices.pop();
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
            if (ticksRemaining <= 0)
                return 0f;
            int elapsed = POPUP_DURATION - ticksRemaining;
            if (elapsed < 8)
                return Math.min(1f, elapsed / 8.0f);
            if (ticksRemaining < 50)
                return Math.min(1f, ticksRemaining / 50.0f);
            return 1.0f;
        }

        float getScale() {
            int elapsed = POPUP_DURATION - ticksRemaining;
            if (elapsed < 4)
                return 0.5f + (elapsed / 4.0f) * 0.7f; // 0.5 to 1.2
            if (elapsed < 8)
                return 1.2f - ((elapsed - 4) / 4.0f) * 0.2f; // 1.2 to 1.0
            return 1.0f;
        }
    }
}