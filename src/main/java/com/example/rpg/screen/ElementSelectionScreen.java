package com.example.rpg.screen;

import com.example.rpg.config.RpgLocale;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.utils.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ElementSelectionScreen extends Screen {

    private float uiScale;
    private final List<TarotCard> cards = new ArrayList<>();

    // For smooth background transitions
    private float bgTopR = 5f, bgTopG = 5f, bgTopB = 5f;
    private float bgBotR = 5f, bgBotG = 5f, bgBotB = 5f;
    private TarotCard lastHovered = null;
    private float textFade = 0f;

    private class TarotCard {
        MagicElement element;
        int x, y, width, height, currentY;

        TarotCard(MagicElement el, int x, int y, int w, int h) {
            this.element = el;
            this.x = x;
            this.y = y;
            this.currentY = y;
            this.width = w;
            this.height = h;
        }

        boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + height;
        }
    }

    public ElementSelectionScreen() {
        super(Text.literal("Awakening"));
    }

    @Override
    protected void init() {
        super.init();
        calculateAdaptiveSizes();
        cards.clear();

        MagicElement[] elements = { MagicElement.FIRE, MagicElement.WATER, MagicElement.WIND, MagicElement.LIGHTNING,
                MagicElement.EARTH };

        int cardWidth = (int) (100 * uiScale);
        int cardHeight = (int) (180 * uiScale);
        int spacing = (int) (15 * uiScale);

        int totalWidth = elements.length * cardWidth + (elements.length - 1) * spacing;
        int startX = (this.width - totalWidth) / 2;
        int startY = (this.height - cardHeight) / 2 + (int) (20 * uiScale);

        for (int i = 0; i < elements.length; i++) {
            MagicElement el = elements[i];
            int cx = startX + i * (cardWidth + spacing);
            cards.add(new TarotCard(el, cx, startY, cardWidth, cardHeight));

            this.addDrawableChild(ButtonWidget.builder(Text.literal(""), b -> {
                StatsNetworking.sendSetElement(el);
                this.close();
            }).dimensions(cx, startY, cardWidth, cardHeight).build());
        }
    }

    private void calculateAdaptiveSizes() {
        uiScale = Math.max(0.6f, Math.min(1.0f, Math.min((float) this.width / 640, (float) this.height / 360)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        TarotCard hovered = null;
        for (TarotCard card : cards) {
            if (card.isHovered(mouseX, mouseY)) {
                hovered = card;
            }
        }

        if (hovered != null) {
            lastHovered = hovered;
        }

        // Target Background Colors
        int targetBgTop = lastHovered != null ? RenderUtils.withAlpha(lastHovered.element.bgDark, 0.4f) : 0xFF050505;
        int targetBgBot = 0xFF050505;

        // Lerp Background Colors
        float lerpSpeed = 0.1f;
        bgTopR += (((targetBgTop >> 16) & 0xFF) - bgTopR) * lerpSpeed;
        bgTopG += (((targetBgTop >> 8) & 0xFF) - bgTopG) * lerpSpeed;
        bgTopB += ((targetBgTop & 0xFF) - bgTopB) * lerpSpeed;

        bgBotR += (((targetBgBot >> 16) & 0xFF) - bgBotR) * lerpSpeed;
        bgBotG += (((targetBgBot >> 8) & 0xFF) - bgBotG) * lerpSpeed;
        bgBotB += ((targetBgBot & 0xFF) - bgBotB) * lerpSpeed;

        int currentBgTop = 0xFF000000 | ((int) bgTopR << 16) | ((int) bgTopG << 8) | (int) bgTopB;
        int currentBgBot = 0xFF000000 | ((int) bgBotR << 16) | ((int) bgBotG << 8) | (int) bgBotB;

        context.fillGradient(0, 0, this.width, this.height, currentBgTop, currentBgBot);

        // Lerp Text Fade
        float targetTextFade = lastHovered != null ? 1f : 0f;
        textFade += (targetTextFade - textFade) * 0.15f;

        // Render Title/Desc
        if (textFade > 0.01f && lastHovered != null) {
            String title = RpgLocale.getElementName(lastHovered.element.name().toLowerCase());
            String desc = RpgLocale.getElementDesc(lastHovered.element.name().toLowerCase());

            int alpha = (int) (textFade * 255);
            int titleColor = (alpha << 24) | (lastHovered.element.textTitle & 0x00FFFFFF);
            int descColor = (alpha << 24) | 0x00AAAAAA;

            context.drawCenteredTextWithShadow(this.textRenderer, lastHovered.element.getIcon() + " " + title,
                    this.width / 2, (int) (40 * uiScale), titleColor);
            context.drawCenteredTextWithShadow(this.textRenderer, desc, this.width / 2, (int) (60 * uiScale),
                    descColor);
        }

        if (textFade < 0.99f) {
            String choose = RpgLocale.get("element.title");
            int alpha = (int) ((1f - textFade) * 255);
            int chooseColor = (alpha << 24) | 0x00FFFFFF;
            context.drawCenteredTextWithShadow(this.textRenderer, choose, this.width / 2, (int) (50 * uiScale),
                    chooseColor);
        }

        // Render Cards
        for (TarotCard card : cards) {
            boolean isHovered = card == lastHovered;

            // Interaction Animation: Lift card up by 15 scaled pixels
            int targetY = isHovered ? card.y - (int) (15 * uiScale) : card.y;
            card.currentY += (targetY - card.currentY) * 0.3f; // Smooth interpolation

            int cx = card.x;
            int cy = card.currentY;
            int cw = card.width;
            int ch = card.height;

            // Card Shadow
            context.fill(cx + 4, cy + 4, cx + cw + 4, cy + ch + 4, 0x88000000);

            // Card Background
            int cBgTop = RenderUtils.withAlpha(card.element.bgDark, 0.9f);
            int cBgBot = RenderUtils.withAlpha(card.element.bgMedium, 0.9f);
            context.fillGradient(cx, cy, cx + cw, cy + ch, cBgTop, cBgBot);

            // Borders
            int borderColor = isHovered ? card.element.borderPrimary : card.element.borderSecondary;
            RenderUtils.drawBorder(context, cx, cy, cw, ch, borderColor);
            RenderUtils.drawBorder(context, cx + 2, cy + 2, cw - 4, ch - 4, RenderUtils.withAlpha(borderColor, 0.5f));

            // Card Icon & Inner Element Display
            int iconScale = (int) (2 * uiScale);
            context.getMatrices().push();
            context.getMatrices().translate(cx + cw / 2.0f, cy + ch / 2.0f - (int) (20 * uiScale), 0);
            context.getMatrices().scale(iconScale, iconScale, 1f);
            float pulse = isHovered
                    ? (float) (Math.sin(System.currentTimeMillis() % 1000 / 1000.0 * Math.PI * 2) * 0.5 + 0.5)
                    : 1f;
            int iconColor = isHovered ? RenderUtils.blendColors(card.element.textPrimary, card.element.textTitle, pulse)
                    : 0xFFAAAAAA;

            int iconW = this.textRenderer.getWidth(card.element.getIcon());
            context.drawTextWithShadow(this.textRenderer, card.element.getIcon(), -iconW / 2,
                    -this.textRenderer.fontHeight / 2, iconColor);
            context.getMatrices().pop();

            // Card Name at bottom of card
            String name = RpgLocale.getElementName(card.element.name().toLowerCase());
            if (this.textRenderer.getWidth(name) > cw - 8)
                name = name.substring(0, 3) + "..";
            context.drawCenteredTextWithShadow(this.textRenderer, name, cx + cw / 2, cy + ch - (int) (25 * uiScale),
                    card.element.textTitle);
        }

        // Call super to handle button clicks (since we added invisible buttons over the
        // cards)
        // Wait, super.render draws the buttons (which have no text but have standard
        // button styling).
        // Let's hide the typical button rendering by NOT calling super.render, but
        // manually handling clicks,
        // OR we can just let standard buttons render their hover overlay?
        // Actually, we added ButtonWidget with Text.literal(""). Minecraft will draw a
        // vanilla button over our cards!
        // We do NOT want that. We should make the buttons invisible or not use
        // super.render.
        // Let's NOT call super.render, but how do clicks work? Activity is handled by
        // Screen's mouseClicked inherently traversing children.
        // So we don't need super.render to make the buttons clickable!
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}