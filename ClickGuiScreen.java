package com.auramod;

import com.auramod.module.combat.KillAuraModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Animated glass-style ClickGUI for AuraMod.
 * Visual styling is original and only loosely inspired by modern Minecraft client menus.
 */
public final class ClickGuiScreen extends Screen {
    private enum Tab {
        COMBAT("Combat", "C"),
        VISUALS("Visuals", "V"),
        HUD("HUD", "H");

        private final String title;
        private final String letter;

        Tab(String title, String letter) {
            this.title = title;
            this.letter = letter;
        }
    }

    private final KillAuraModule practiceModule;
    private final Map<String, Float> switchAnimations = new HashMap<>();

    private Tab selectedTab = Tab.COMBAT;
    private float openProgress = 0.0F;
    private float animationTime = 0.0F;

    // These settings control GUI/HUD presentation, not combat behavior.
    private boolean particlesEnabled = true;
    private boolean glassGlowEnabled = true;
    private boolean gridEnabled = false;
    private boolean targetHudPreviewEnabled = true;
    private boolean arrayListPreviewEnabled = true;
    private int accentIndex = 0;

    private static final int[] ACCENTS = {
            0xFF62E8D5,
            0xFF9C83FF,
            0xFF72B7FF,
            0xFFFF86B6
    };

    public ClickGuiScreen(KillAuraModule practiceModule) {
        super(Text.literal("AuraMod ClickGUI"));
        this.practiceModule = practiceModule;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        animationTime += Math.max(0.25F, deltaTicks);
        openProgress += (1.0F - openProgress) * 0.22F;

        // Vanilla's blurred game backdrop, then a dark tinted glass layer.
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        context.fill(0, 0, width, height, withAlpha(0xFF07101C, 0.60F * openProgress));

        if (gridEnabled) {
            drawGrid(context, openProgress);
        }
        if (particlesEnabled) {
            drawParticles(context, openProgress);
        }

        Layout layout = getLayout();
        drawWindow(context, layout, mouseX, mouseY);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void drawWindow(DrawContext context, Layout l, int mouseX, int mouseY) {
        float openingAlpha = easeOut(openProgress);
        int accent = accent();

        if (glassGlowEnabled) {
            rounded(context, l.x - 5, l.y - 4, l.w + 10, l.h + 8, 18,
                    withAlpha(accent, 0.045F * openingAlpha));
            rounded(context, l.x - 2, l.y - 2, l.w + 4, l.h + 4, 15,
                    withAlpha(accent, 0.075F * openingAlpha));
        }

        rounded(context, l.x, l.y + 3, l.w, l.h, 13,
                withAlpha(0xFF000000, 0.40F * openingAlpha));
        rounded(context, l.x, l.y, l.w, l.h, 13,
                withAlpha(0xD9152031, openingAlpha));
        rounded(context, l.x + 1, l.y + 1, l.w - 2, l.h - 2, 12,
                withAlpha(0xA8111D2D, openingAlpha));
        context.drawBorder(l.x, l.y, l.w, l.h, withAlpha(0xA6D8F4FF, 0.30F * openingAlpha));

        // Fine accent strip along the top edge.
        context.fillGradient(l.x + 18, l.y + 1, l.x + l.w - 18, l.y + 3,
                withAlpha(accent, 0.94F * openingAlpha), withAlpha(ACCENTS[(accentIndex + 1) % ACCENTS.length], 0.55F * openingAlpha));

        drawSidebar(context, l, mouseX, mouseY, openingAlpha);
        drawMainPanel(context, l, mouseX, mouseY, openingAlpha);
    }

    private void drawSidebar(DrawContext context, Layout l, int mouseX, int mouseY, float alpha) {
        int accent = accent();
        rounded(context, l.x + 10, l.y + 12, l.sidebarW - 18, l.h - 24, 10,
                withAlpha(0x80101B2A, alpha));

        rounded(context, l.x + 22, l.y + 25, 34, 34, 10, withAlpha(accent, alpha));
        rounded(context, l.x + 29, l.y + 32, 20, 20, 6, withAlpha(0xFF0D1A29, alpha));
        drawText(context, "A", l.x + 35, l.y + 37, withAlpha(accent, alpha));
        drawText(context, "AURAMOD", l.x + 63, l.y + 28, withAlpha(0xFFF3FAFF, alpha));
        drawText(context, "GLASS EDITION", l.x + 63, l.y + 42, withAlpha(0xFF89A2B8, alpha));

        drawText(context, "WORKSPACE", l.x + 22, l.y + 83, withAlpha(0xFF71869D, alpha));
        int navY = l.y + 101;
        for (Tab tab : Tab.values()) {
            int y = navY + tab.ordinal() * 42;
            boolean active = selectedTab == tab;
            boolean hovered = inside(mouseX, mouseY, l.x + 17, y - 5, l.sidebarW - 34, 34);
            if (active) {
                rounded(context, l.x + 17, y - 5, l.sidebarW - 34, 34, 8,
                        withAlpha(accent, 0.15F * alpha));
                rounded(context, l.x + 17, y + 2, 3, 20, 2, withAlpha(accent, alpha));
            } else if (hovered) {
                rounded(context, l.x + 17, y - 5, l.sidebarW - 34, 34, 8,
                        withAlpha(0xFFB6D3E8, 0.07F * alpha));
            }
            rounded(context, l.x + 27, y + 2, 20, 20, 6,
                    withAlpha(active ? accent : 0xFF34465B, (active ? 0.26F : 0.60F) * alpha));
            drawText(context, tab.letter, l.x + 33, y + 8,
                    withAlpha(active ? accent : 0xFFB0C0D0, alpha));
            drawText(context, tab.title, l.x + 56, y + 8,
                    withAlpha(active ? 0xFFFFFFFF : 0xFF9AAEC1, alpha));
        }

        int footerY = l.y + l.h - 65;
        rounded(context, l.x + 20, footerY, l.sidebarW - 40, 1, 0,
                withAlpha(0xFF8EA5BA, 0.18F * alpha));
        rounded(context, l.x + 23, footerY + 14, 7, 7, 4, withAlpha(0xFF65E5AA, alpha));
        drawText(context, "LOCAL BUILD", l.x + 36, footerY + 13, withAlpha(0xFFB6C9D8, alpha));
        drawText(context, "Minecraft 1.21.8", l.x + 23, footerY + 29, withAlpha(0xFF6F879F, alpha));
    }

    private void drawMainPanel(DrawContext context, Layout l, int mouseX, int mouseY, float alpha) {
        int accent = accent();
        int contentX = l.contentX;
        int contentW = l.contentW;
        int top = l.y + 24;

        String title = selectedTab.title.toUpperCase();
        drawText(context, title, contentX, top, withAlpha(0xFFF1F8FF, alpha));
        drawText(context, subtitle(), contentX, top + 17, withAlpha(0xFF8BA0B5, alpha));

        rounded(context, l.x + l.w - 88, l.y + 17, 60, 22, 8,
                withAlpha(0xFF0D1725, alpha));
        rounded(context, l.x + l.w - 78, l.y + 25, 5, 5, 3,
                withAlpha(0xFF69E2B0, alpha));
        drawText(context, "READY", l.x + l.w - 67, l.y + 24, withAlpha(0xFFBCD4E7, alpha));
        drawText(context, "×", l.x + l.w - 19, l.y + 19, withAlpha(0xFF8FA5BA, alpha));

        int contentTop = l.y + 64;
        switch (selectedTab) {
            case COMBAT -> drawCombatTab(context, l, mouseX, mouseY, alpha, contentTop, contentW);
            case VISUALS -> drawVisualsTab(context, l, mouseX, mouseY, alpha, contentTop, contentW);
            case HUD -> drawHudTab(context, l, mouseX, mouseY, alpha, contentTop, contentW);
        }

        int footerY = l.y + l.h - 22;
        context.fill(contentX, footerY - 7, l.x + l.w - 17, footerY - 6,
                withAlpha(0xFF9FB5C9, 0.13F * alpha));
        drawText(context, "RIGHT SHIFT  /  CLOSE", contentX, footerY,
                withAlpha(0xFF6F859B, alpha));
        drawText(context, "AURAMOD  •  DEV", l.x + l.w - 112, footerY,
                withAlpha(accent, 0.82F * alpha));
    }

    private void drawCombatTab(DrawContext context, Layout l, int mouseX, int mouseY,
                               float alpha, int top, int contentW) {
        int x = l.contentX;
        int accent = accent();
        int cardW = contentW;

        // Module card.
        int cardY = top + 10;
        glassCard(context, x, cardY, cardW, 66, alpha, mouseX, mouseY);
        rounded(context, x + 13, cardY + 15, 34, 34, 9,
                withAlpha(accent, 0.19F * alpha));
        drawText(context, "A", x + 25, cardY + 27, withAlpha(accent, alpha));
        drawText(context, "Practice Aura", x + 58, cardY + 16,
                withAlpha(0xFFF0F7FF, alpha));
        drawText(context, "Hostile mobs • singleplayer only", x + 58, cardY + 34,
                withAlpha(0xFF8299AE, alpha));
        drawText(context, practiceModule.isEnabled() ? "ACTIVE" : "INACTIVE",
                x + cardW - 99, cardY + 13,
                withAlpha(practiceModule.isEnabled() ? accent : 0xFF7D91A5, alpha));
        drawSwitch(context, "aura", practiceModule.isEnabled(),
                x + cardW - 51, cardY + 34, alpha);

        int gap = 10;
        int leftW = (cardW - gap) / 2;
        int rightX = x + leftW + gap;
        int rowY = cardY + 77;
        int rowH = 94;

        // Range control.
        glassCard(context, x, rowY, leftW, rowH, alpha, mouseX, mouseY);
        drawText(context, "ATTACK RANGE", x + 13, rowY + 12,
                withAlpha(0xFF8DA4B8, alpha));
        drawText(context, String.format(java.util.Locale.ROOT, "%.1f blocks", practiceModule.getRange()),
                x + 13, rowY + 32, withAlpha(0xFFF3F8FF, alpha));
        int trackX = x + 13;
        int trackW = leftW - 26;
        int trackY = rowY + 66;
        rounded(context, trackX, trackY, trackW, 5, 3,
                withAlpha(0xFF33465B, alpha));
        float rangeT = (float) ((practiceModule.getRange() - 1.0) / 5.0);
        rounded(context, trackX, trackY, Math.max(4, (int) (trackW * rangeT)), 5, 3,
                withAlpha(accent, alpha));
        int knobX = trackX + (int) (trackW * rangeT);
        rounded(context, knobX - 5, trackY - 4, 11, 13, 5,
                withAlpha(0xFFE9FBFF, alpha));
        drawText(context, "1", trackX, rowY + 78, withAlpha(0xFF6F859B, alpha));
        drawText(context, "6", trackX + trackW - 5, rowY + 78, withAlpha(0xFF6F859B, alpha));

        // Two slim option rows.
        drawOptionRow(context, rightX, rowY, leftW, 43, "Raycast check",
                "Require crosshair lock", "raycast", practiceModule.isRaycastEnabled(), alpha, mouseX, mouseY);
        drawModeRow(context, rightX, rowY + 51, leftW, 43, alpha, mouseX, mouseY);

        int infoY = rowY + rowH + 11;
        glassCard(context, x, infoY, cardW, 45, alpha, mouseX, mouseY);
        rounded(context, x + 13, infoY + 13, 18, 18, 6,
                withAlpha(0xFF7CB8FF, 0.15F * alpha));
        drawText(context, "i", x + 20, infoY + 17, withAlpha(0xFF9DCAFF, alpha));
        drawText(context, "Practice mode", x + 41, infoY + 9, withAlpha(0xFFE3F0FB, alpha));
        drawText(context, "The module is restricted to local singleplayer worlds.",
                x + 41, infoY + 25, withAlpha(0xFF8198AD, alpha));
    }

    private void drawVisualsTab(DrawContext context, Layout l, int mouseX, int mouseY,
                                float alpha, int top, int contentW) {
        int x = l.contentX;
        int gap = 10;
        int w = (contentW - gap) / 2;
        int cardY = top + 10;
        int cardH = 84;

        drawVisualOption(context, x, cardY, w, cardH, "Ambient particles",
                "Slow drifting specks", "particles", particlesEnabled, alpha, mouseX, mouseY);
        drawVisualOption(context, x + w + gap, cardY, w, cardH, "Glass glow",
                "Soft accent halo", "glow", glassGlowEnabled, alpha, mouseX, mouseY);
        drawVisualOption(context, x, cardY + cardH + 10, w, cardH, "Background grid",
                "Fine glass texture", "grid", gridEnabled, alpha, mouseX, mouseY);

        glassCard(context, x + w + gap, cardY + cardH + 10, w, cardH, alpha, mouseX, mouseY);
        drawText(context, "ACCENT COLOR", x + w + gap + 13, cardY + cardH + 23,
                withAlpha(0xFF8DA4B8, alpha));
        String[] names = {"Mint", "Violet", "Sky", "Rose"};
        drawText(context, names[accentIndex], x + w + gap + 13, cardY + cardH + 42,
                withAlpha(0xFFF1F8FF, alpha));
        int dotStart = x + w + gap + 13;
        for (int i = 0; i < ACCENTS.length; i++) {
            rounded(context, dotStart + i * 22, cardY + cardH + 61, 15, 7, 4,
                    withAlpha(ACCENTS[i], alpha));
        }
        rounded(context, x + contentW - 46, cardY + cardH + 24, 32, 32, 9,
                withAlpha(accent(), 0.18F * alpha));
        drawText(context, "↻", x + contentW - 36, cardY + cardH + 33,
                withAlpha(accent(), alpha));

        int noteY = cardY + cardH * 2 + 25;
        glassCard(context, x, noteY, contentW, 47, alpha, mouseX, mouseY);
        drawText(context, "LIVE PREVIEW", x + 13, noteY + 9,
                withAlpha(accent(), alpha));
        drawText(context, "Flip a switch to see the glass theme update instantly.",
                x + 13, noteY + 25, withAlpha(0xFF8CA2B6, alpha));
    }

    private void drawHudTab(DrawContext context, Layout l, int mouseX, int mouseY,
                            float alpha, int top, int contentW) {
        int x = l.contentX;
        int cardY = top + 10;
        int half = (contentW - 10) / 2;

        drawVisualOption(context, x, cardY, half, 77, "Array list preview",
                "Active modules card", "array", arrayListPreviewEnabled, alpha, mouseX, mouseY);
        drawVisualOption(context, x + half + 10, cardY, half, 77, "Target HUD preview",
                "Health bar mock-up", "targethud", targetHudPreviewEnabled, alpha, mouseX, mouseY);

        int previewY = cardY + 91;
        int previewH = l.y + l.h - previewY - 43;
        glassCard(context, x, previewY, contentW, Math.max(88, previewH), alpha, mouseX, mouseY);
        drawText(context, "ON-SCREEN PREVIEWS", x + 13, previewY + 12,
                withAlpha(0xFF8DA4B8, alpha));

        if (arrayListPreviewEnabled) {
            int ax = x + 13;
            int ay = previewY + 34;
            rounded(context, ax, ay, Math.min(150, contentW / 2 - 16), 22, 6,
                    withAlpha(0xC80B1522, alpha));
            drawText(context, "AURAMOD", ax + 8, ay + 7, withAlpha(accent(), alpha));
            if (practiceModule.isEnabled()) {
                rounded(context, ax, ay + 26, Math.min(150, contentW / 2 - 16), 22, 6,
                        withAlpha(0xC80B1522, alpha));
                drawText(context, "Practice Aura", ax + 8, ay + 33,
                        withAlpha(0xFFE6F2FC, alpha));
            }
        }

        if (targetHudPreviewEnabled) {
            int hw = Math.min(190, contentW / 2 - 8);
            int hx = x + contentW - hw - 13;
            int hy = previewY + 34;
            rounded(context, hx, hy, hw, 66, 8, withAlpha(0xD20B1522, alpha));
            rounded(context, hx + 9, hy + 10, 25, 25, 7,
                    withAlpha(0xFF3A536B, alpha));
            drawText(context, "MOB", hx + 14, hy + 19, withAlpha(0xFFD7E7F5, alpha));
            drawText(context, "Training target", hx + 42, hy + 9,
                    withAlpha(0xFFE8F3FC, alpha));
            drawText(context, "20.0 / 20.0 HP", hx + 42, hy + 25,
                    withAlpha(0xFF8FA9BD, alpha));
            rounded(context, hx + 10, hy + 47, hw - 20, 6, 3,
                    withAlpha(0xFF35465A, alpha));
            rounded(context, hx + 10, hy + 47, hw - 20, 6, 3,
                    withAlpha(0xFF62E0A4, alpha));
        }

        drawText(context, "These are visual previews inside the menu.", x + 13,
                previewY + Math.max(88, previewH) - 18, withAlpha(0xFF738AA0, alpha));
    }

    private void drawOptionRow(DrawContext context, int x, int y, int w, int h,
                               String title, String description, String key, boolean value,
                               float alpha, int mouseX, int mouseY) {
        glassCard(context, x, y, w, h, alpha, mouseX, mouseY);
        drawText(context, title, x + 11, y + 7, withAlpha(0xFFE5F0FA, alpha));
        drawText(context, description, x + 11, y + 22, withAlpha(0xFF7E94A9, alpha));
        drawSwitch(context, key, value, x + w - 43, y + 13, alpha);
    }

    private void drawModeRow(DrawContext context, int x, int y, int w, int h,
                             float alpha, int mouseX, int mouseY) {
        glassCard(context, x, y, w, h, alpha, mouseX, mouseY);
        drawText(context, "Rotation", x + 11, y + 7, withAlpha(0xFFE5F0FA, alpha));
        String mode = practiceModule.getRotationMode() == KillAuraModule.RotationMode.SMOOTH
                ? "Smooth" : "Snap";
        drawText(context, "Click to change", x + 11, y + 22, withAlpha(0xFF7E94A9, alpha));
        int buttonW = 57;
        rounded(context, x + w - buttonW - 9, y + 10, buttonW, 23, 7,
                withAlpha(accent(), 0.16F * alpha));
        drawText(context, mode, x + w - buttonW + 3, y + 17,
                withAlpha(accent(), alpha));
    }

    private void drawVisualOption(DrawContext context, int x, int y, int w, int h,
                                  String title, String description, String key, boolean value,
                                  float alpha, int mouseX, int mouseY) {
        glassCard(context, x, y, w, h, alpha, mouseX, mouseY);
        drawText(context, title, x + 12, y + 13, withAlpha(0xFFE7F2FC, alpha));
        drawText(context, description, x + 12, y + 30, withAlpha(0xFF8197AC, alpha));
        drawSwitch(context, key, value, x + w - 48, y + h - 29, alpha);
    }

    private void glassCard(DrawContext context, int x, int y, int w, int h, float alpha,
                           int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, h);
        rounded(context, x, y, w, h, 9,
                withAlpha(hovered ? 0xB52D4054 : 0x92233346, alpha));
        rounded(context, x + 1, y + 1, w - 2, 1, 0,
                withAlpha(hovered ? accent() : 0xFFD8F0FF, (hovered ? 0.40F : 0.13F) * alpha));
        context.drawBorder(x, y, w, h,
                withAlpha(hovered ? accent() : 0xFFB4CCDF, (hovered ? 0.35F : 0.13F) * alpha));
    }

    private void drawSwitch(DrawContext context, String key, boolean value,
                            int x, int y, float alpha) {
        float current = switchAnimations.getOrDefault(key, value ? 1.0F : 0.0F);
        float target = value ? 1.0F : 0.0F;
        current += (target - current) * 0.24F;
        if (Math.abs(target - current) < 0.005F) current = target;
        switchAnimations.put(key, current);

        int bg = mix(0xFF334459, accent(), current);
        rounded(context, x, y, 35, 19, 9, withAlpha(bg, 0.95F * alpha));
        rounded(context, x + 2 + Math.round(current * 15.0F), y + 2, 15, 15, 7,
                withAlpha(0xFFF3FBFF, alpha));
    }

    private void drawGrid(DrawContext context, float alpha) {
        int color = withAlpha(accent(), 0.045F * alpha);
        for (int x = 0; x < width; x += 26) {
            context.fill(x, 0, x + 1, height, color);
        }
        for (int y = 0; y < height; y += 26) {
            context.fill(0, y, width, y + 1, color);
        }
    }

    private void drawParticles(DrawContext context, float alpha) {
        for (int i = 0; i < 34; i++) {
            double phase = animationTime * (0.006 + (i % 4) * 0.0015) + i * 1.73;
            int x = positiveMod((int) (i * 97 + Math.sin(phase) * 26), Math.max(1, width));
            int y = positiveMod((int) (i * 59 - animationTime * (0.15 + (i % 3) * 0.07)), Math.max(1, height));
            int size = (i % 6 == 0) ? 3 : 2;
            context.fill(x, y, x + size, y + size,
                    withAlpha(accent(), (i % 5 == 0 ? 0.30F : 0.12F) * alpha));
        }
    }

    private void rounded(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (r == 0) {
            context.fill(x, y, x + w, y + h, color);
            return;
        }
        context.fill(x + r, y, x + w - r, y + h, color);
        context.fill(x, y + r, x + w, y + h - r, color);
        for (int row = 0; row < r; row++) {
            double dy = r - row - 0.5;
            int inset = r - (int) Math.floor(Math.sqrt(Math.max(0.0, r * r - dy * dy)));
            context.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            context.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }

    private void drawText(DrawContext context, String text, int x, int y, int color) {
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    private String subtitle() {
        return switch (selectedTab) {
            case COMBAT -> "Practice controls and module settings";
            case VISUALS -> "Glass, particles and accent customisation";
            case HUD -> "Preview your on-screen widgets";
        };
    }

    private int accent() {
        return ACCENTS[accentIndex];
    }

    private static int withAlpha(int color, float multiplier) {
        int oldAlpha = (color >>> 24) & 0xFF;
        int alpha = MathHelper.clamp(Math.round(oldAlpha * multiplier), 0, 255);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static int mix(int from, int to, float amount) {
        float t = MathHelper.clamp(amount, 0.0F, 1.0F);
        int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
        int r = Math.round(((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * t);
        int g = Math.round(((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static float easeOut(float value) {
        float v = MathHelper.clamp(value, 0.0F, 1.0F);
        return 1.0F - (1.0F - v) * (1.0F - v);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static int positiveMod(int value, int mod) {
        int result = value % mod;
        return result < 0 ? result + mod : result;
    }

    private Layout getLayout() {
        int panelW = Math.min(650, Math.max(300, width - 24));
        int panelH = Math.min(390, Math.max(250, height - 24));
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2 + Math.round((1.0F - openProgress) * 8.0F);
        int sidebarW = Math.min(150, Math.max(112, panelW / 4));
        int contentX = x + sidebarW + 18;
        int contentW = Math.max(150, x + panelW - 18 - contentX);
        return new Layout(x, y, panelW, panelH, sidebarW, contentX, contentW);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        Layout l = getLayout();
        int top = l.y + 64;
        int x = l.contentX;
        int w = l.contentW;

        // Close control.
        if (inside(mouseX, mouseY, l.x + l.w - 29, l.y + 12, 20, 28)) {
            close();
            return true;
        }

        // Sidebar tabs.
        int navY = l.y + 101;
        for (Tab tab : Tab.values()) {
            int y = navY + tab.ordinal() * 42;
            if (inside(mouseX, mouseY, l.x + 17, y - 5, l.sidebarW - 34, 34)) {
                selectedTab = tab;
                return true;
            }
        }

        if (selectedTab == Tab.COMBAT) {
            int cardY = top + 10;
            if (inside(mouseX, mouseY, x + w - 62, cardY + 8, 53, 50)) {
                practiceModule.toggle();
                return true;
            }

            int gap = 10;
            int leftW = (w - gap) / 2;
            int rightX = x + leftW + gap;
            int rowY = cardY + 77;
            int trackX = x + 13;
            int trackW = leftW - 26;
            int trackY = rowY + 66;
            if (inside(mouseX, mouseY, x, rowY, leftW, 94)) {
                if (mouseY >= trackY - 12) {
                    double value = (mouseX - trackX) / (double) Math.max(1, trackW);
                    practiceModule.setRange(1.0 + MathHelper.clamp(value, 0.0, 1.0) * 5.0);
                    return true;
                }
            }

            int rayY = rowY;
            if (inside(mouseX, mouseY, rightX, rayY, leftW, 43)) {
                practiceModule.setRaycast(!practiceModule.isRaycastEnabled());
                return true;
            }
            if (inside(mouseX, mouseY, rightX, rowY + 51, leftW, 43)) {
                KillAuraModule.RotationMode next = practiceModule.getRotationMode() == KillAuraModule.RotationMode.SMOOTH
                        ? KillAuraModule.RotationMode.SNAP : KillAuraModule.RotationMode.SMOOTH;
                practiceModule.setRotationMode(next);
                return true;
            }
        } else if (selectedTab == Tab.VISUALS) {
            int cardY = top + 10;
            int gap = 10;
            int half = (w - gap) / 2;
            int h = 84;
            if (inside(mouseX, mouseY, x, cardY, half, h)) {
                particlesEnabled = !particlesEnabled;
                return true;
            }
            if (inside(mouseX, mouseY, x + half + gap, cardY, half, h)) {
                glassGlowEnabled = !glassGlowEnabled;
                return true;
            }
            if (inside(mouseX, mouseY, x, cardY + h + 10, half, h)) {
                gridEnabled = !gridEnabled;
                return true;
            }
            if (inside(mouseX, mouseY, x + half + gap, cardY + h + 10, half, h)) {
                accentIndex = (accentIndex + 1) % ACCENTS.length;
                return true;
            }
        } else if (selectedTab == Tab.HUD) {
            int cardY = top + 10;
            int half = (w - 10) / 2;
            if (inside(mouseX, mouseY, x, cardY, half, 77)) {
                arrayListPreviewEnabled = !arrayListPreviewEnabled;
                return true;
            }
            if (inside(mouseX, mouseY, x + half + 10, cardY, half, 77)) {
                targetHudPreviewEnabled = !targetHudPreviewEnabled;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void close() {
        if (client != null) {
            client.setScreen(null);
        }
    }

    private record Layout(int x, int y, int w, int h, int sidebarW, int contentX, int contentW) { }
}
