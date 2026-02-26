package com.example.rpg.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    // --- Basic drawing ---

    public static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    // --- Panels ---

    /** Стекломорфная панель с градиентом, тенью и двойной рамкой */
    public static void drawGlassPanel(DrawContext context, int x, int y, int w, int h,
            int bgTop, int bgBot, int borderPrimary, int borderSecondary) {
        // Shadow
        context.fill(x + 4, y + 4, x + w + 4, y + h + 4, 0x66000000);
        // Background gradient
        context.fillGradient(x, y, x + w, y + h, bgTop, bgBot);
        // Double border
        drawBorder(context, x - 1, y - 1, w + 2, h + 2, borderPrimary);
        drawBorder(context, x, y, w, h, borderSecondary);
    }

    /** Панель с пульсирующей рамкой (animated glow) */
    public static void drawGlassPanelAnimated(DrawContext context, int x, int y, int w, int h,
            int bgTop, int bgBot, int borderPrimary, int borderSecondary) {
        float glow = (float) (Math.sin(System.currentTimeMillis() % 4000 / 4000.0 * Math.PI * 2) * 0.5 + 0.5);
        int animBorder = blendColors(borderSecondary, borderPrimary, glow);
        drawGlassPanel(context, x, y, w, h, bgTop, bgBot, animBorder, borderSecondary);
    }

    // --- Progress bars ---

    /** Прогресс-бар с gradient заливкой */
    public static void drawProgressBar(DrawContext context, int x, int y, int width, int height,
            float progress, int fillStart, int fillEnd, int bgColor) {
        context.fill(x, y, x + width, y + height, bgColor);
        int fillW = (int) (width * Math.min(1.0f, Math.max(0f, progress)));
        if (fillW > 0) {
            context.fillGradient(x, y, x + fillW, y + height, fillStart, fillEnd);
        }
        drawBorder(context, x, y, width, height, withAlpha(fillStart, 0.3f));
    }

    // --- Custom buttons ---

    /** Кастомная кнопка с hover-эффектом */
    public static boolean drawCustomButton(DrawContext context, TextRenderer textRenderer,
            int x, int y, int w, int h,
            String text, int mouseX, int mouseY,
            int bgColor, int hoverColor, int textColor, int borderColor) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hovered ? hoverColor : bgColor;
        context.fill(x, y, x + w, y + h, bg);
        drawBorder(context, x, y, w, h, hovered ? brighten(borderColor, 40) : borderColor);

        int textW = textRenderer.getWidth(text);
        int textX = x + (w - textW) / 2;
        int textY = y + (h - textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(textRenderer, text, textX, textY, textColor);
        return hovered;
    }

    /** Маленькая кнопка «+» для апгрейда */
    public static boolean drawUpgradeButton(DrawContext context, TextRenderer textRenderer,
            int x, int y, int size, int mouseX, int mouseY,
            int accentColor, boolean enabled) {
        boolean hovered = mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
        int bg = enabled
                ? (hovered ? withAlpha(accentColor, 0.5f) : withAlpha(accentColor, 0.25f))
                : 0x33333333;
        context.fill(x, y, x + size, y + size, bg);
        drawBorder(context, x, y, size, size, enabled ? accentColor : 0xFF444444);

        int textColor = enabled ? 0xFFFFFFFF : 0xFF666666;
        int textX = x + (size - textRenderer.getWidth("+")) / 2;
        int textY = y + (size - textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(textRenderer, "+", textX, textY, textColor);
        return hovered && enabled;
    }

    /** Строка настроек с hover-подсветкой и полоской слева при наведении */
    public static boolean drawSettingsRow(DrawContext context, int x, int y, int w, int h,
            int mouseX, int mouseY, int accentColor) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        if (hovered) {
            context.fill(x, y, x + w, y + h, 0x15FFFFFF);
            context.fill(x, y + 2, x + 2, y + h - 2, accentColor);
        }
        return hovered;
    }

    /** Кнопка бинда в стиле стекломорфизма, с пульсацией при waitingForBind */
    public static boolean drawBindButton(DrawContext context, TextRenderer textRenderer,
            int x, int y, int w, int h, String text,
            int mouseX, int mouseY, int accentColor, boolean isWaiting) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        if (isWaiting) {
            // Pulsing glow when waiting for key press
            float pulse = (float) (Math.sin(System.currentTimeMillis() % 1200 / 1200.0 * Math.PI * 2) * 0.5 + 0.5);
            int bg = blendColors(withAlpha(accentColor, 0.15f), withAlpha(accentColor, 0.4f), pulse);
            context.fill(x, y, x + w, y + h, bg);
            int border = blendColors(accentColor, brighten(accentColor, 60), pulse);
            drawBorder(context, x, y, w, h, border);
        } else {
            int bg = hovered ? 0x33FFFFFF : 0x22FFFFFF;
            context.fill(x, y, x + w, y + h, bg);
            int border = hovered ? brighten(accentColor, 30) : withAlpha(accentColor, 0.5f);
            drawBorder(context, x, y, w, h, border);
        }

        int textColor = isWaiting ? 0xFFFFFF55 : (hovered ? 0xFFFFFFFF : 0xFFCCCCCC);
        int textW = textRenderer.getWidth(text);
        int textX = x + (w - textW) / 2;
        int textY = y + (h - textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(textRenderer, text, textX, textY, textColor);
        return hovered;
    }

    // --- Scissor ---

    public static void enableScissor(DrawContext context, int x, int y, int width, int height) {
        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int windowHeight = MinecraftClient.getInstance().getWindow().getHeight();
        int scissorX = (int) (x * scale);
        int scissorY = (int) (windowHeight - (y + height) * scale);
        int scissorWidth = (int) (width * scale);
        int scissorHeight = (int) (height * scale);
        context.enableScissor(scissorX, scissorY, scissorX + scissorWidth, scissorY + scissorHeight);
    }

    public static void disableScissor(DrawContext context) {
        context.disableScissor();
    }

    // --- Color utilities ---

    public static float getUiScale(int screenWidth) {
        float baseWidth = 854f;
        float scale = screenWidth / baseWidth;
        return Math.max(0.7f, Math.min(1.3f, scale));
    }

    public static int withAlpha(int color, float alpha) {
        int a = (int) (Math.max(0f, Math.min(1f, alpha)) * 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int brighten(int color, int amount) {
        return shiftColor(color, amount);
    }

    public static int darken(int color, int amount) {
        return shiftColor(color, -amount);
    }

    private static int shiftColor(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, Math.min(255, ((color >> 16) & 0xFF) + amount));
        int g = Math.max(0, Math.min(255, ((color >> 8) & 0xFF) + amount));
        int b = Math.max(0, Math.min(255, (color & 0xFF) + amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int blendColors(int color1, int color2, float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int a = (int) (((color1 >> 24) & 0xFF) * (1 - ratio) + ((color2 >> 24) & 0xFF) * ratio);
        int r = (int) (((color1 >> 16) & 0xFF) * (1 - ratio) + ((color2 >> 16) & 0xFF) * ratio);
        int g = (int) (((color1 >> 8) & 0xFF) * (1 - ratio) + ((color2 >> 8) & 0xFF) * ratio);
        int b = (int) ((color1 & 0xFF) * (1 - ratio) + (color2 & 0xFF) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Плавная интерполяция для анимаций */
    public static float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
}