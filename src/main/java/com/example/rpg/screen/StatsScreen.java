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

    private int panelWidth, panelHeight, panelX, panelY;
    private int sidebarWidth, contentX, contentY, contentWidth, contentHeight;
    private float uiScale;

    private enum Tab {
        STATS, SKILLS, MAGIC
    }

    private Tab currentTab = Tab.STATS;
    private PlayerStatsData cachedData;

    public StatsScreen() {
        super(Text.literal("RPG Hub"));
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

    private PlayerStatsData getPlayerData() {
        if (this.client != null && this.client.player instanceof IPlayerStatsAccessor accessor) {
            return accessor.rpg_getStatsData();
        }
        return null;
    }

    private void calculateAdaptiveSizes() {
        uiScale = Math.max(0.6f, Math.min(1.0f, Math.min(
                (float) this.width / 400,
                (float) this.height / 250)));

        // Massive panel occupying most of the screen
        panelWidth = (int) (this.width * 0.85f);
        panelHeight = (int) (this.height * 0.85f);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        sidebarWidth = (int) (100 * uiScale);
        contentX = panelX + sidebarWidth;
        contentY = panelY;
        contentWidth = panelWidth - sidebarWidth;
        contentHeight = panelHeight;
    }

    private void rebuildButtons() {
        this.clearChildren();
        if (cachedData == null) {
            cachedData = getPlayerData();
            if (cachedData == null)
                return;
        }

        buildSidebar();

        if (currentTab == Tab.STATS) {
            buildStatsContent();
        } else if (currentTab == Tab.SKILLS) {
            buildSkillsContent();
        } else if (currentTab == Tab.MAGIC) {
            buildMagicContent();
        }
    }

    // ==================== SIDEBAR ====================

    private void buildSidebar() {
        int btnWidth = sidebarWidth - (int) (16 * uiScale);
        int btnHeight = (int) (24 * uiScale);
        int startX = panelX + (int) (8 * uiScale);
        int startY = panelY + (int) (50 * uiScale);
        int spacing = (int) (10 * uiScale);

        addTabBtn(RpgLocale.get("tab.stats"), Tab.STATS, startX, startY, btnWidth, btnHeight);
        addTabBtn(RpgLocale.get("tab.skills"), Tab.SKILLS, startX, startY + btnHeight + spacing, btnWidth, btnHeight);
        addTabBtn(RpgLocale.get("tab.magic"), Tab.MAGIC, startX, startY + (btnHeight + spacing) * 2, btnWidth,
                btnHeight);

        // Sidebar Bottom Buttons
        int bottomY = panelY + panelHeight - btnHeight - (int) (10 * uiScale);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§c" + RpgLocale.get("menu.close")),
                b -> close()).dimensions(startX, bottomY, btnWidth, btnHeight).build());
    }

    private void addTabBtn(String name, Tab tab, int x, int y, int w, int h) {
        String prefix = currentTab == tab ? "§e▶ " : "  ";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(prefix + name), b -> {
            currentTab = tab;
            rebuildButtons();
        }).dimensions(x, y, w, h).build());
    }

    // ==================== CONTENT: STATS ====================

    private void buildStatsContent() {
        int btnWidth = (int) (20 * uiScale);
        int btnHeight = (int) (16 * uiScale);
        int rowHeight = (int) (32 * uiScale);
        int startY = contentY + (int) (60 * uiScale);
        int startX = contentX + contentWidth - (int) (40 * uiScale);

        for (int i = 0; i < StatType.values().length; i++) {
            StatType stat = StatType.values()[i];
            int y = startY + (i * rowHeight);
            this.addDrawableChild(ButtonWidget.builder(Text.literal("+"),
                    b -> {
                        StatsNetworking.sendUpgradeRequest(stat);
                        cachedData = getPlayerData();
                    })
                    .dimensions(startX, y + (rowHeight - btnHeight) / 2, btnWidth, btnHeight).build());
        }

        // Reset Stats Form
        int bottomY = contentY + contentHeight - (int) (30 * uiScale);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§c" + RpgLocale.get("menu.reset")),
                b -> {
                    StatsNetworking.sendResetRequest();
                    cachedData = getPlayerData();
                }).dimensions(contentX + (int) (20 * uiScale), bottomY, (int) (100 * uiScale), (int) (20 * uiScale))
                .build());
    }

    // ==================== CONTENT: SKILLS ====================

    private void buildSkillsContent() {
        int btnWidth = (int) (20 * uiScale);
        int btnHeight = (int) (16 * uiScale);
        int rowHeight = (int) (32 * uiScale);
        int startY = contentY + (int) (60 * uiScale);
        int startX = contentX + contentWidth - (int) (40 * uiScale);

        int index = 0;
        for (Ability ability : AbilityRegistry.getAll()) {
            int y = startY + (index * rowHeight);
            String abilityId = ability.getId();
            this.addDrawableChild(ButtonWidget.builder(Text.literal("+"),
                    b -> {
                        StatsNetworking.sendUpgradeAbility(abilityId);
                        cachedData = getPlayerData();
                    })
                    .dimensions(startX, y + (rowHeight - btnHeight) / 2, btnWidth, btnHeight).build());
            index++;
        }
    }

    // ==================== CONTENT: MAGIC ====================

    private void buildMagicContent() {
        if (cachedData == null)
            return;
        MagicElement element = cachedData.getElement();
        List<MagicAbility> abilities = MagicSkillRegistry.getAbilitiesForElement(element);
        if (abilities.isEmpty())
            return;

        // Radial layout originating from a core center
        int cx = contentX + contentWidth / 2;
        int cy = contentY + contentHeight / 2 + (int) (20 * uiScale);
        int btnWidth = (int) (80 * uiScale);
        int btnHeight = (int) (20 * uiScale);

        for (MagicAbility ability : abilities) {
            int tier = ability.getTier();
            int branch = ability.getBranch();

            int radius = tier * (int) (60 * uiScale);
            // Distribute branches angularly. Branch 1, 2, 3 -> angles
            double angle = Math.PI / 2; // Upwards for tier 0
            if (tier > 0) {
                // Spread out based on branch. Let's say 3 branches.
                angle = Math.PI + (branch - 2) * (Math.PI / 4.0); // Simple fan out upwards
            }
            int x = cx + (int) (Math.sin(angle) * radius) - btnWidth / 2;
            int y = cy - (int) (Math.cos(angle) * radius) - btnHeight / 2;

            int level = cachedData.getMagicSkillLevel(ability.getId());
            boolean isMax = level >= ability.getMaxLevel();
            boolean meetsReqs = cachedData.meetsRequirements(ability.getRequiredSkills());
            String prefix = isMax ? "§6✓ " : !meetsReqs ? "§8" : level > 0 ? "§a" : "";

            String name = RpgLocale.getSkillName(ability.getId());
            if (this.textRenderer.getWidth(name) > btnWidth - 10)
                name = name.substring(0, 5) + "..";

            this.addDrawableChild(ButtonWidget.builder(Text.literal(prefix + name), b -> {
                if (meetsReqs && !isMax) {
                    StatsNetworking.sendUpgradeMagicSkill(ability.getId());
                    cachedData = getPlayerData();
                }
            }).dimensions(x, y, btnWidth, btnHeight).build());
        }

        int bottomY = contentY + contentHeight - (int) (30 * uiScale);
        String resetText = RpgConfig.get().isRussian() ? "Сброс стихии" : "Reset Element";
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§c" + resetText), b -> {
            StatsNetworking.sendResetElement();
            MinecraftClient.getInstance()
                    .execute(() -> MinecraftClient.getInstance().setScreen(new ElementSelectionScreen()));
        }).dimensions(contentX + (int) (20 * uiScale), bottomY, (int) (100 * uiScale), (int) (20 * uiScale)).build());
    }

    // ==================== RENDERING ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        cachedData = getPlayerData();
        if (cachedData == null) {
            super.render(context, mouseX, mouseY, delta);
            return;
        }
        MagicElement el = cachedData.getElement();

        // Dark fullscreen backdrop
        context.fill(0, 0, this.width, this.height, 0xDD000000);

        // Sidebar Panel background
        context.fillGradient(panelX, panelY, contentX, panelY + panelHeight, 0xEE111111, 0xEE050505);
        RenderUtils.drawBorder(context, panelX, panelY, sidebarWidth, panelHeight, 0xFF333333);

        // Content Area Background (Glassmorphism with element tint)
        int bgDark = RenderUtils.withAlpha(el.bgDark, 0.85f);
        int bgMed = RenderUtils.withAlpha(el.bgMedium, 0.65f);
        context.fillGradient(contentX, contentY, contentX + contentWidth, contentY + contentHeight, bgDark, bgMed);

        float glow = (float) (Math.sin(System.currentTimeMillis() % 4000 / 4000.0 * Math.PI * 2) * 0.5 + 0.5);
        int borderC = RenderUtils.blendColors(el.borderSecondary, el.borderPrimary, glow);
        RenderUtils.drawBorder(context, contentX, contentY, contentWidth, contentHeight, borderC);

        // Sidebar Title
        context.drawCenteredTextWithShadow(this.textRenderer, RpgLocale.get("menu.title"), panelX + sidebarWidth / 2,
                panelY + (int) (20 * uiScale), 0xFFFFD700);

        // Draw Content
        if (currentTab == Tab.STATS)
            drawStatsDetails(context, cachedData, el);
        else if (currentTab == Tab.SKILLS)
            drawSkillsDetails(context, cachedData, el);
        else
            drawMagicDetails(context, cachedData, el);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawStatsDetails(DrawContext context, PlayerStatsData data, MagicElement el) {
        context.drawTextWithShadow(this.textRenderer, "Points: " + data.getSkillPoints(),
                contentX + (int) (20 * uiScale), contentY + (int) (20 * uiScale), 0xFF55FF55);
        int rowHeight = (int) (32 * uiScale);
        int startY = contentY + (int) (60 * uiScale);
        int startX = contentX + (int) (20 * uiScale);
        int barMaxWidth = contentWidth - (int) (100 * uiScale);

        for (int i = 0; i < StatType.values().length; i++) {
            StatType stat = StatType.values()[i];
            int y = startY + (i * rowHeight);

            String label = RpgLocale.get("stat." + stat.name().toLowerCase());
            context.drawTextWithShadow(this.textRenderer, label, startX, y, 0xFFFFFFFF);

            int level = data.getStatLevel(stat);
            String val = String.valueOf(level);
            context.drawTextWithShadow(this.textRenderer, val, startX + (int) (80 * uiScale), y, 0xFFAAAAAA);

            // Blocky segmented progress bar
            int pX = startX + (int) (110 * uiScale);
            int pWidth = barMaxWidth - (int) (110 * uiScale);
            int pHeight = (int) (6 * uiScale);
            int pY = y + (this.textRenderer.fontHeight - pHeight) / 2;

            context.fill(pX, pY, pX + pWidth, pY + pHeight, 0x55000000);
            int fillW = (int) (pWidth * Math.min(1.0f, level / 50.0f));
            if (fillW > 0) {
                context.fillGradient(pX, pY, pX + fillW, pY + pHeight, el.borderPrimary, el.borderSecondary);
            }
        }
    }

    private void drawSkillsDetails(DrawContext context, PlayerStatsData data, MagicElement el) {
        context.drawTextWithShadow(this.textRenderer, "Skill Points: " + data.getSkillPoints(),
                contentX + (int) (20 * uiScale), contentY + (int) (20 * uiScale), 0xFF55FF55);

        int rowHeight = (int) (32 * uiScale);
        int startY = contentY + (int) (60 * uiScale);
        int startX = contentX + (int) (20 * uiScale);

        int index = 0;
        for (Ability ability : AbilityRegistry.getAll()) {
            int y = startY + (index * rowHeight);
            String name = RpgLocale.getAbilityName(ability.getId());
            int lvl = data.getAbilityLevel(ability.getId());

            context.drawTextWithShadow(this.textRenderer, name, startX, y, 0xFFFFFFFF);

            // Skill icon
            context.drawTextWithShadow(this.textRenderer, ability.getIcon(), startX + (int) (120 * uiScale), y,
                    0xFFFFFFFF);

            // Level
            String lvlStr = "Lvl " + lvl + " / " + ability.getMaxLevel();
            int color = lvl >= ability.getMaxLevel() ? 0xFFFFD700 : 0xFFAAAAAA;
            context.drawTextWithShadow(this.textRenderer, lvlStr, startX + (int) (160 * uiScale), y, color);
            index++;
        }
    }

    private void drawMagicDetails(DrawContext context, PlayerStatsData data, MagicElement el) {
        context.drawTextWithShadow(this.textRenderer, "Magic Nodes: " + data.getSkillPoints(),
                contentX + (int) (20 * uiScale), contentY + (int) (20 * uiScale), 0xFF55FF55);
        context.drawTextWithShadow(this.textRenderer, "Element: " + RpgLocale.getElementName(el.name().toLowerCase()),
                contentX + (int) (20 * uiScale), contentY + (int) (35 * uiScale), el.textPrimary);
        // Draw Radial Tree Tethers
        List<MagicAbility> abilities = MagicSkillRegistry.getAbilitiesForElement(el);
        int cx = contentX + contentWidth / 2;
        int cy = contentY + contentHeight / 2 + (int) (20 * uiScale);

        for (MagicAbility ability : abilities) {
            int tier = ability.getTier();
            if (tier == 0)
                continue; // Root

            int branch = ability.getBranch();
            int radius = tier * (int) (60 * uiScale);
            double angle = Math.PI / 2;
            if (tier > 0)
                angle = Math.PI + (branch - 2) * (Math.PI / 4.0);

            int x = cx + (int) (Math.sin(angle) * radius);
            int y = cy - (int) (Math.cos(angle) * radius);

            int parentTier = tier - 1;
            int parentRadius = parentTier * (int) (60 * uiScale);
            double parentAngle = Math.PI / 2;
            if (parentTier > 0)
                parentAngle = Math.PI + (branch - 2) * (Math.PI / 4.0);

            int px = cx + (int) (Math.sin(parentAngle) * parentRadius);
            int py = cy - (int) (Math.cos(parentAngle) * parentRadius);

            boolean unlocked = data.getMagicSkillLevel(ability.getId()) > 0;
            int tetherColor = unlocked ? el.borderPrimary : 0x55555555;

            // Simple drawn tether line simulation using thin fills (approximate)
            int minX = Math.min(x, px);
            int minY = Math.min(y, py);
            int maxX = Math.max(x, px);
            int maxY = Math.max(y, py);
            if (Math.abs(x - px) > Math.abs(y - py)) {
                context.fill(minX, y, maxX, y + 1, tetherColor); // Horiz-dominant step
                context.fill(px, minY, px + 1, maxY, tetherColor); // Vert link
            } else {
                context.fill(x, minY, x + 1, maxY, tetherColor);
                context.fill(minX, py, maxX, py + 1, tetherColor);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}