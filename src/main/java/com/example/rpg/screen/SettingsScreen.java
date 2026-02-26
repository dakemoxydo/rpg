package com.example.rpg.screen;

import com.example.rpg.RpgClient;
import com.example.rpg.ability.Ability;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgConfig;
import com.example.rpg.config.RpgLocale;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.utils.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {

    private final Screen parent;
    private String waitingForBindId = null;

    private int panelX, panelY, panelW, panelH, pad;
    private float uiScale;
    private double scrollOffset = 0;
    private int maxScroll = 0;

    // Scroll area bounds
    private int listAreaTop, listAreaH;

    // Custom bind entries (no ButtonWidget!)
    private final List<BindEntry> bindEntries = new ArrayList<>();

    // Back button rect
    private int backBtnX, backBtnY, backBtnW, backBtnH;
    // Language button rect
    private int langBtnX, langBtnY, langBtnW, langBtnH;

    // Cached element colors
    private int accentColor, borderP, borderS, titleColor;

    private record BindEntry(String id, String label, int relativeY, boolean isHeader,
            int btnX, int btnW, int btnH, String keyName) {
    }

    public SettingsScreen(Screen parent) {
        super(Text.literal("RPG Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        calculateLayout();
        bindEntries.clear();
        scrollOffset = 0;
        buildEntries();
    }

    private PlayerStatsData getPlayerData() {
        if (this.client != null && this.client.player instanceof IPlayerStatsAccessor accessor) {
            return accessor.rpg_getStatsData();
        }
        return null;
    }

    private void calculateLayout() {
        uiScale = Math.max(0.6f, Math.min(1.0f, Math.min(
                (float) this.width / 500, (float) this.height / 400)));
        panelW = (int) (360 * uiScale);
        panelH = (int) (320 * uiScale);
        pad = (int) (16 * uiScale);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
    }

    private void buildEntries() {
        PlayerStatsData data = getPlayerData();
        MagicElement el = data != null ? data.getElement() : MagicElement.NONE;
        accentColor = el != MagicElement.NONE ? el.borderPrimary : 0xFF4488FF;
        borderP = el != MagicElement.NONE ? el.borderPrimary : 0xFF555555;
        borderS = el != MagicElement.NONE ? el.borderSecondary : 0xFF333333;
        titleColor = el != MagicElement.NONE ? el.textTitle : 0xFFFFD700;

        int btnW = (int) (70 * uiScale);
        int btnH = (int) (18 * uiScale);
        int rowH = (int) (24 * uiScale);
        int btnX = panelX + panelW - pad - btnW;

        // Language button position (fixed, outside scroll)
        langBtnX = btnX;
        langBtnY = panelY + (int) (42 * uiScale);
        langBtnW = btnW;
        langBtnH = btnH;

        // Scroll area starts below language row
        listAreaTop = panelY + (int) (70 * uiScale);
        listAreaH = panelH - (int) (70 * uiScale) - (int) (36 * uiScale);

        int listY = 0; // relative Y for scroll content

        // === Main section ===
        bindEntries.add(new BindEntry(null, "🎮 " + RpgLocale.get("settings.category.main"), listY, true,
                0, 0, 0, null));
        listY += (int) (20 * uiScale);

        // Menu key
        int menuKey = data != null ? data.getOpenMenuKey() : RpgConfig.get().getOpenMenuKey();
        bindEntries.add(new BindEntry("MENU", RpgLocale.get("settings.menu_key"), listY, false,
                btnX, btnW, btnH, getShortKeyName(menuKey)));
        listY += rowH;
        listY += (int) (8 * uiScale);

        // === Physical skills ===
        if (data != null) {
            boolean hasSkills = false;
            for (Ability ability : AbilityRegistry.getPhysicalAbilities()) {
                if (data.hasSkill(ability.getId())) {
                    hasSkills = true;
                    break;
                }
            }
            if (hasSkills) {
                bindEntries.add(new BindEntry(null, "⚔ " + RpgLocale.get("settings.category.skills"), listY, true,
                        0, 0, 0, null));
                listY += (int) (20 * uiScale);
                for (Ability ability : AbilityRegistry.getPhysicalAbilities()) {
                    if (data.hasSkill(ability.getId())) {
                        int key = data.getKeybind(ability.getId(), ability.getDefaultKey());
                        bindEntries.add(new BindEntry(ability.getId(),
                                RpgLocale.getAbilityName(ability.getId()), listY, false,
                                btnX, btnW, btnH, getShortKeyName(key)));
                        listY += rowH;
                    }
                }
                listY += (int) (8 * uiScale);
            }
        }

        // === Magic abilities ===
        if (data != null && data.hasElement()) {
            MagicElement element = data.getElement();
            if (element != MagicElement.NONE) {
                List<MagicAbility> abilities = AbilityRegistry.getMagicAbilities(element);
                boolean hasActive = false;
                for (MagicAbility a : abilities) {
                    if (a.getManaCost() > 0 && data.hasSkill(a.getId())) {
                        hasActive = true;
                        break;
                    }
                }
                if (hasActive) {
                    bindEntries.add(new BindEntry(null,
                            "✦ " + RpgLocale.get("settings.category.element")
                                    + RpgLocale.getElementName(element.name()),
                            listY, true, 0, 0, 0, null));
                    listY += (int) (20 * uiScale);
                    for (MagicAbility ability : abilities) {
                        if (ability.getManaCost() > 0 && data.hasSkill(ability.getId())) {
                            int key = data.getKeybind(ability.getId(), ability.getDefaultKey());
                            bindEntries.add(new BindEntry(ability.getId(),
                                    RpgLocale.getSkillName(ability.getId()), listY, false,
                                    btnX, btnW, btnH, getShortKeyName(key)));
                            listY += rowH;
                        }
                    }
                }
            }
        }

        maxScroll = Math.max(0, listY - listAreaH);

        // Back button (fixed)
        backBtnW = (int) (80 * uiScale);
        backBtnH = btnH;
        backBtnX = panelX + (panelW - backBtnW) / 2;
        backBtnY = panelY + panelH - (int) (28 * uiScale);
    }

    // ==================== RENDERING ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        PlayerStatsData data = getPlayerData();
        MagicElement el = data != null ? data.getElement() : MagicElement.NONE;

        // Dark overlay
        context.fill(0, 0, this.width, this.height, 0xCC000000);

        // Glass panel
        int bgTop = RenderUtils.withAlpha(el != MagicElement.NONE ? el.bgDark : 0xFF080808, 0.95f);
        int bgBot = RenderUtils.withAlpha(el != MagicElement.NONE ? el.bgMedium : 0xFF141414, 0.85f);
        RenderUtils.drawGlassPanelAnimated(context, panelX, panelY, panelW, panelH,
                bgTop, bgBot, borderP, borderS);

        // Title header
        int headerH = (int) (30 * uiScale);
        context.fillGradient(panelX, panelY, panelX + panelW, panelY + headerH, 0x22FFFFFF, 0x05FFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "⚙ " + RpgLocale.get("settings.title"),
                panelX + panelW / 2, panelY + (int) (10 * uiScale), titleColor);
        context.fill(panelX + pad, panelY + headerH, panelX + panelW - pad, panelY + headerH + 1, borderS);

        // Language row (fixed, above scroll area)
        int langLabelY = langBtnY + (langBtnH - textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(this.textRenderer, RpgLocale.get("settings.language"),
                panelX + pad, langLabelY, 0xFFDDDDDD);

        // Language button — custom rendered
        boolean isRu = data != null ? data.isRussian() : RpgConfig.get().isRussian();
        String langText = isRu ? "Русский" : "English";
        RenderUtils.drawBindButton(context, this.textRenderer,
                langBtnX, langBtnY, langBtnW, langBtnH, langText,
                mouseX, mouseY, accentColor, false);

        // Separator
        int sepY = langBtnY + langBtnH + (int) (4 * uiScale);
        context.fillGradient(panelX + pad, sepY, panelX + panelW / 2 - pad, sepY + 1,
                borderS, 0x00000000);
        context.fillGradient(panelX + panelW / 2 + pad, sepY, panelX + panelW - pad, sepY + 1,
                0x00000000, borderS);

        // -- Scrollable bind list with scissor --
        context.enableScissor(panelX + 1, listAreaTop, panelX + panelW - 1, listAreaTop + listAreaH);

        int rowH = (int) (24 * uiScale);
        for (BindEntry entry : bindEntries) {
            int y = (int) (listAreaTop + entry.relativeY - scrollOffset);

            boolean visible = (y + rowH > listAreaTop) && (y < listAreaTop + listAreaH);
            if (!visible)
                continue;

            if (entry.isHeader) {
                // Category header with gradient background and accent line
                int hdrH = (int) (18 * uiScale);
                context.fillGradient(panelX + pad, y,
                        panelX + panelW - pad, y + hdrH,
                        0x18FFFFFF, 0x00FFFFFF);
                context.drawTextWithShadow(this.textRenderer, entry.label,
                        panelX + pad + (int) (4 * uiScale), y + (int) (4 * uiScale), titleColor);
                // Gradient divider under header
                context.fillGradient(panelX + pad, y + hdrH,
                        panelX + panelW - pad, y + hdrH + 1,
                        RenderUtils.withAlpha(accentColor, 0.4f), 0x00000000);
            } else {
                // Row hover highlight
                int rowLeft = panelX + pad;
                int rowRight = panelX + panelW - pad;
                RenderUtils.drawSettingsRow(context, rowLeft, y, rowRight - rowLeft, rowH,
                        mouseX, mouseY, accentColor);

                // Label text
                context.drawTextWithShadow(this.textRenderer, entry.label,
                        panelX + pad + (int) (10 * uiScale),
                        y + (rowH - textRenderer.fontHeight) / 2,
                        0xFFCCCCCC);

                // Bind button (custom rendered)
                boolean isWaiting = entry.id != null && entry.id.equals(waitingForBindId);
                String displayText = isWaiting ? "..." : entry.keyName;
                int btnY = y + (rowH - entry.btnH) / 2;
                RenderUtils.drawBindButton(context, this.textRenderer,
                        entry.btnX, btnY, entry.btnW, entry.btnH, displayText,
                        mouseX, mouseY, accentColor, isWaiting);
            }
        }

        context.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            int sbW = Math.max(3, (int) (4 * uiScale));
            int sbX = panelX + panelW - pad + (int) (2 * uiScale);
            int totalContent = listAreaH + maxScroll;
            int sbH = Math.max(20, (int) ((float) (listAreaH * listAreaH) / totalContent));
            int sbY = listAreaTop + (int) ((listAreaH - sbH) * (scrollOffset / maxScroll));

            // Track
            context.fill(sbX, listAreaTop, sbX + sbW, listAreaTop + listAreaH, 0x22FFFFFF);
            // Thumb with glow
            boolean sbHovered = mouseX >= sbX && mouseX <= sbX + sbW + 4
                    && mouseY >= listAreaTop && mouseY <= listAreaTop + listAreaH;
            int thumbColor = sbHovered
                    ? RenderUtils.brighten(accentColor, 20)
                    : RenderUtils.withAlpha(accentColor, 0.7f);
            context.fillGradient(sbX, sbY, sbX + sbW, sbY + sbH,
                    RenderUtils.brighten(thumbColor, 20), RenderUtils.darken(thumbColor, 10));
        }

        // Waiting-for-bind hint banner
        if (waitingForBindId != null) {
            int hintH = (int) (18 * uiScale);
            int hintY = panelY + panelH - (int) (48 * uiScale);
            // Pulsing background
            float pulse = (float) (Math.sin(System.currentTimeMillis() % 2000 / 2000.0 * Math.PI * 2) * 0.5 + 0.5);
            int hintBg = RenderUtils.blendColors(0x22FFFF00, 0x44FFFF00, pulse);
            context.fill(panelX + pad, hintY, panelX + panelW - pad, hintY + hintH, hintBg);
            RenderUtils.drawBorder(context, panelX + pad, hintY, panelW - pad * 2, hintH,
                    RenderUtils.withAlpha(0xFFFFFF55, 0.4f));
            context.drawCenteredTextWithShadow(this.textRenderer, RpgLocale.get("settings.cancel_hint"),
                    panelX + panelW / 2, hintY + (hintH - textRenderer.fontHeight) / 2, 0xFFFFFF55);
        }

        // Back button (custom, red-accent like StatsScreen close button)
        RenderUtils.drawCustomButton(context, this.textRenderer,
                backBtnX, backBtnY, backBtnW, backBtnH,
                RpgLocale.get("settings.back"), mouseX, mouseY,
                0x33FF4444, 0x55FF4444, 0xFFFF6666, 0xFF883333);
    }

    // ==================== INPUT ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;

        // If waiting for a bind, apply mouse button as bind
        if (waitingForBindId != null) {
            applyBind(waitingForBindId, button);
            waitingForBindId = null;
            playClickSound();
            rebuildEntries();
            return true;
        }

        // Language button
        if (mx >= langBtnX && mx <= langBtnX + langBtnW && my >= langBtnY && my <= langBtnY + langBtnH) {
            PlayerStatsData data = getPlayerData();
            String newLang = (data != null && data.isRussian()) ? "en" : "ru";
            StatsNetworking.sendUpdateSetting("language", newLang);
            RpgConfig.get().setLanguage(newLang);
            playClickSound();
            this.init();
            return true;
        }

        // Back button
        if (mx >= backBtnX && mx <= backBtnX + backBtnW && my >= backBtnY && my <= backBtnY + backBtnH) {
            playClickSound();
            close();
            return true;
        }

        // Bind buttons in scroll area
        if (my >= listAreaTop && my < listAreaTop + listAreaH) {
            int rowH = (int) (24 * uiScale);
            for (BindEntry entry : bindEntries) {
                if (entry.isHeader)
                    continue;
                int y = (int) (listAreaTop + entry.relativeY - scrollOffset);
                int btnY = y + (rowH - entry.btnH) / 2;
                if (mx >= entry.btnX && mx <= entry.btnX + entry.btnW
                        && my >= btnY && my <= btnY + entry.btnH) {
                    waitingForBindId = entry.id;
                    playClickSound();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForBindId != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForBindId = null;
            } else {
                applyBind(waitingForBindId, keyCode);
                waitingForBindId = null;
                rebuildEntries();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - amount * 18));
            return true;
        }
        return false;
    }

    // ==================== LOGIC ====================

    private void applyBind(String bindId, int key) {
        if ("MENU".equals(bindId)) {
            StatsNetworking.sendUpdateSetting("openMenuKey", String.valueOf(key));
            RpgConfig.get().setOpenMenuKey(key);
            RpgClient.updateMenuKeyBind(key);
        } else {
            StatsNetworking.sendUpdateSetting("keybind", bindId + ":" + key);
            RpgConfig.get().setKeybind(bindId, key);
        }
    }

    private void rebuildEntries() {
        bindEntries.clear();
        buildEntries();
    }

    private void playClickSound() {
        if (this.client != null) {
            this.client.getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    private String getShortKeyName(int keyCode) {
        String name = RpgConfig.getKeyName(keyCode);
        return name.length() > 8 ? name.substring(0, 6) + ".." : name;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}