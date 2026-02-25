package com.example.rpg.screen;

import com.example.rpg.config.RpgLocale;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.util.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ElementSelectionScreen extends Screen {

    private int panelWidth, panelHeight, panelX, panelY;
    private float uiScale;
    private final List<ElementButtonInfo> elementButtons = new ArrayList<>();

    private record ElementButtonInfo(MagicElement element, int x, int y, int width, int height) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    public ElementSelectionScreen() {
        super(Text.literal("Choose Your Element"));
    }

    @Override
    protected void init() {
        super.init();
        calculateAdaptiveSizes();
        elementButtons.clear();

        MagicElement[] elements = { MagicElement.FIRE, MagicElement.WATER, MagicElement.WIND, MagicElement.LIGHTNING, MagicElement.EARTH };
        int buttonWidth = (int)(130 * uiScale);
        int buttonHeight = (int)(28 * uiScale);
        int hSpacing = (int)(10 * uiScale);
        int vSpacing = (int)(8 * uiScale);
        int gridWidth = buttonWidth * 2 + hSpacing;
        int gridStartX = panelX + (panelWidth - gridWidth) / 2;
        int startY = panelY + (int)(90 * uiScale);

        for (int i = 0; i < elements.length; i++) {
            MagicElement element = elements[i];
            int x = i == 4 ? panelX + (panelWidth - buttonWidth) / 2 : gridStartX + (i % 2) * (buttonWidth + hSpacing);
            int y = i == 4 ? startY + 2 * (buttonHeight + vSpacing) : startY + (i / 2) * (buttonHeight + vSpacing);

            elementButtons.add(new ElementButtonInfo(element, x, y, buttonWidth, buttonHeight));

            // Локализованное имя элемента
            String displayName = RpgLocale.getElementName(element.name());
            String buttonText = element.getIcon() + " " + displayName;
            int maxTextWidth = buttonWidth - 10;
            while (this.textRenderer.getWidth(buttonText) > maxTextWidth && displayName.length() > 3) {
                displayName = displayName.substring(0, displayName.length() - 1);
                buttonText = element.getIcon() + " " + displayName + "..";
            }

            final String finalText = buttonText;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(finalText),
                    button -> { StatsNetworking.sendSetElement(element); this.close(); }
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
        }

        int closeWidth = (int)(70 * uiScale);
        int closeHeight = (int)(18 * uiScale);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§7" + RpgLocale.get("menu.close")), button -> close())
                .dimensions(panelX + (panelWidth - closeWidth) / 2, panelY + panelHeight - (int)(32 * uiScale), closeWidth, closeHeight).build());
    }

    private void calculateAdaptiveSizes() {
        uiScale = Math.max(0.6f, Math.min(1.0f, Math.min(
                (float)(this.width - 40) / 300,
                (float)(this.height - 40) / 260)));
        panelWidth = (int)(300 * uiScale);
        panelHeight = (int)(260 * uiScale);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xDD000000);

        context.fill(panelX + 4, panelY + 4, panelX + panelWidth + 4, panelY + panelHeight + 4, 0x60000000);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF1a1a1a);
        RenderUtils.drawBorder(context, panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, 0xFF555555);
        RenderUtils.drawBorder(context, panelX - 1, panelY - 1, panelWidth + 2, panelHeight + 2, 0xFF333333);

        int headerHeight = (int)(42 * uiScale);
        context.fill(panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + headerHeight, 0xFF252525);

        // Локализованные тексты
        String title = RpgLocale.get("element.title");
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title),
                panelX + panelWidth / 2, panelY + (int)(10 * uiScale), 0xFFFFD700);

        String subtitle = RpgLocale.get("element.subtitle");
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(subtitle),
                panelX + panelWidth / 2, panelY + (int)(24 * uiScale), 0xFF888888);

        context.fill(panelX + (int)(15 * uiScale), panelY + headerHeight + 2,
                panelX + panelWidth - (int)(15 * uiScale), panelY + headerHeight + 3, 0xFF444444);

        String info = RpgLocale.get("element.info");
        int infoWidth = this.textRenderer.getWidth(info);
        int maxInfoWidth = panelWidth - (int)(20 * uiScale);
        if (infoWidth > maxInfoWidth) {
            info = info.substring(0, Math.min(info.length(), 25)) + "..";
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(info),
                panelX + panelWidth / 2, panelY + (int)(58 * uiScale), 0xFFAAAAAA);

        // Описание при наведении
        MagicElement hoveredElement = elementButtons.stream()
                .filter(b -> b.contains(mouseX, mouseY))
                .map(ElementButtonInfo::element)
                .findFirst().orElse(null);

        if (hoveredElement != null) {
            int descY = panelY + panelHeight - (int)(55 * uiScale);
            String desc = RpgLocale.getElementDesc(hoveredElement.name());
            int descWidth = this.textRenderer.getWidth(desc);
            int maxDescWidth = panelWidth - (int)(20 * uiScale);

            if (descWidth > maxDescWidth) {
                while (this.textRenderer.getWidth(desc + "..") > maxDescWidth && desc.length() > 10) {
                    desc = desc.substring(0, desc.length() - 1);
                }
                desc += "..";
                descWidth = this.textRenderer.getWidth(desc);
            }

            context.fill(panelX + (panelWidth - descWidth) / 2 - 6, descY - 2,
                    panelX + (panelWidth + descWidth) / 2 + 6, descY + 12, 0x90000000);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(desc),
                    panelX + panelWidth / 2, descY, hoveredElement.textTitle);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}