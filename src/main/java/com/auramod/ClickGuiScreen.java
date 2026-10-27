
package com.auramod;

import com.auramod.module.combat.KillAuraModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Conservative Glass GUI for Minecraft 1.21.8.
 * Uses basic DrawContext calls and does not pause the game.
 */
public final class ClickGuiScreen extends Screen {

    private enum Tab {
        COMBAT("Combat"),
        VISUALS("Visuals"),
        HUD("HUD");

        private final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    private final KillAuraModule module;

    private Tab tab = Tab.COMBAT;

    private boolean particles = true;
    private boolean glow = true;
    private boolean grid = false;
    private boolean arrayPreview = true;
    private boolean targetPreview = true;

    private int accentIndex = 0;
    private float openAmount = 0.0F;
    private float tick = 0.0F;

    private static final int[] ACCENTS = {
        0xFF65E6D2,
        0xFF9D83FF,
        0xFF72B8FF,
        0xFFFF83B4
    };

    public ClickGuiScreen(KillAuraModule module) {
        super(Text.literal("AuraMod Glass"));
        this.module = module;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(
            DrawContext ctx,
            int mouseX,
            int mouseY,
            float delta
    ) {
        tick += Math.max(0.0F, delta);
        openAmount += (1.0F - openAmount) * 0.24F;

        // Draw our own dark background.
        ctx.fill(0, 0, width, height, 0xB807101B);

        if (grid) {
            drawGrid(ctx);
        }

        if (particles) {
            drawParticles(ctx);
        }

        Layout l = layout();
        float alpha = clamp01(openAmount);

        if (glow) {
            fill(ctx,
                    l.x - 4, l.y - 4,
                    l.w + 8, l.h + 8,
                    withAlpha(accent(), alpha * 0.10F));
        }

        fill(ctx,
                l.x + 3, l.y + 4,
                l.w, l.h,
                withAlpha(0xFF000000, alpha * 0.32F));

        ctx.fillGradient(
                l.x, l.y,
                l.x + l.w, l.y + l.h,
                withAlpha(0xE8152030, alpha),
                withAlpha(0xE9091422, alpha)
        );

        ctx.drawBorder(
                l.x, l.y, l.w, l.h,
                withAlpha(0xFFD8F2FF, alpha * 0.28F)
        );

        ctx.fill(
                l.x + 16, l.y + 1,
                l.x + l.w - 16, l.y + 3,
                withAlpha(accent(), alpha)
        );

        drawSidebar(ctx, l, mouseX, mouseY, alpha);
        drawContent(ctx, l, mouseX, mouseY, alpha);
    }

    private void drawSidebar(
            DrawContext ctx,
            Layout l,
            int mx,
            int my,
            float alpha
    ) {
        fill(ctx,
                l.x + 9, l.y + 10,
                l.sidebarW - 15, l.h - 20,
                withAlpha(0xFF0B1421, alpha * 0.75F));

        fill(ctx,
                l.x + 20, l.y + 22,
                34, 34,
                withAlpha(accent(), alpha * 0.32F));

        fill(ctx,
                l.x + 25, l.y + 27,
                24, 24,
                withAlpha(0xFF101D2C, alpha));

        text(ctx, "A",
                l.x + 33, l.y + 35,
                withAlpha(accent(), alpha));

        text(ctx, "AURAMOD",
                l.x + 61, l.y + 25,
                withAlpha(0xFFF1F7FF, alpha));

        text(ctx, "GLASS EDITION",
                l.x + 61, l.y + 39,
                withAlpha(0xFF8DA4BA, alpha));

        text(ctx, "WORKSPACE",
                l.x + 19, l.y + 77,
                withAlpha(0xFF71869D, alpha));

        int navY = l.y + 94;

        for (Tab candidate : Tab.values()) {
            int y = navY + candidate.ordinal() * 39;

            boolean active = candidate == tab;
            boolean hovered = inside(
                    mx, my,
                    l.x + 15, y - 4,
                    l.sidebarW - 28, 31
            );

            if (active || hovered) {
                fill(ctx,
                        l.x + 15, y - 4,
                        l.sidebarW - 28, 31,
                        withAlpha(
                                active ? accent() : 0xFFB6D3E8,
                                alpha * (active ? 0.16F : 0.07F)
                        ));
            }

            if (active) {
                fill(ctx,
                        l.x + 15, y + 2,
                        3, 18,
                        withAlpha(accent(), alpha));
            }

            text(ctx, candidate.label,
                    l.x + 28, y + 7,
                    withAlpha(
                            active ? 0xFFFFFFFF : 0xFF9AAEC1,
                            alpha
                    ));
        }

        int footerY = l.y + l.h - 48;

        ctx.fill(
                l.x + 18, footerY,
                l.x + l.sidebarW - 14, footerY + 1,
                withAlpha(0xFF8EA5BA, alpha * 0.18F)
        );

        fill(ctx,
                l.x + 21, footerY + 13,
                7, 7,
                withAlpha(0xFF65E5AA, alpha));

        text(ctx, "LOCAL BUILD",
                l.x + 34, footerY + 11,
                withAlpha(0xFFB6C9D8, alpha));

        text(ctx, "Minecraft 1.21.8",
                l.x + 19, footerY + 28,
                withAlpha(0xFF6F879F, alpha));
    }

    private void drawContent(
            DrawContext ctx,
            Layout l,
            int mx,
            int my,
            float alpha
    ) {
        int x = l.contentX;
        int w = l.contentW;

        text(ctx, tab.label.toUpperCase(),
                x, l.y + 22,
                withAlpha(0xFFF1F8FF, alpha));

        text(ctx, subtitle(),
                x, l.y + 39,
                withAlpha(0xFF8BA0B5, alpha));

        fill(ctx,
                l.x + l.w - 86, l.y + 16,
                58, 22,
                withAlpha(0xFF0D1725, alpha));

        fill(ctx,
                l.x + l.w - 76, l.y + 24,
                5, 5,
                withAlpha(0xFF69E2B0, alpha));

        text(ctx, "READY",
                l.x + l.w - 65, l.y + 23,
                withAlpha(0xFFBCD4E7, alpha));

        text(ctx, "X",
                l.x + l.w - 19, l.y + 20,
                withAlpha(0xFF8FA5BA, alpha));

        int top = l.y + 58;

        if (tab == Tab.COMBAT) {
            drawCombat(ctx, l, mx, my, alpha, top);
        } else if (tab == Tab.VISUALS) {
            drawVisuals(ctx, l, mx, my, alpha, top);
        } else {
            drawHud(ctx, l, mx, my, alpha, top);
        }

        int footerY = l.y + l.h - 20;

        ctx.fill(
                x, footerY - 7,
                l.x + l.w - 14, footerY - 6,
                withAlpha(0xFF9FB5C9, alpha * 0.13F)
        );

        text(ctx, "RIGHT SHIFT / ESC TO CLOSE",
                x, footerY,
                withAlpha(0xFF6F859B, alpha));

        text(ctx, "AURAMOD",
                l.x + l.w - 73, footerY,
                withAlpha(accent(), alpha));
    }

    private void drawCombat(
            DrawContext ctx,
            Layout l,
            int mx,
            int my,
            float alpha,
            int top
    ) {
        int x = l.contentX;
        int w = l.contentW;
        int cardY = top + 6;

        card(ctx, x, cardY, w, 55, alpha, mx, my);

        text(ctx, "PRACTICE AURA",
                x + 12, cardY + 10,
                withAlpha(0xFFF0F7FF, alpha));

        text(ctx, "Hostile mobs - singleplayer only",
                x + 12, cardY + 29,
                withAlpha(0xFF8299AE, alpha));

        button(ctx,
                x + w - 79, cardY + 17,
                65, 24,
                module.isEnabled() ? accent() : 0xFF34465B,
                module.isEnabled() ? "ON" : "OFF",
                alpha);

        int gap = 9;
        int half = (w - gap) / 2;
        int rowY = cardY + 66;

        card(ctx, x, rowY, half, 79, alpha, mx, my);

        text(ctx, "RANGE",
                x + 11, rowY + 10,
                withAlpha(0xFF8DA4B8, alpha));

        text(ctx,
                String.format(
                        java.util.Locale.ROOT,
                        "%.1f blocks",
                        module.getRange()
                ),
                x + 11, rowY + 27,
                withAlpha(0xFFF3F8FF, alpha));

        int trackX = x + 12;
        int trackY = rowY + 54;
        int trackW = Math.max(20, half - 24);

        ctx.fill(
                trackX, trackY,
                trackX + trackW, trackY + 5,
                withAlpha(0xFF33465B, alpha)
        );

        float rangeProgress = (float)
                ((module.getRange() - 1.0) / 5.0);

        ctx.fill(
                trackX, trackY,
                trackX + Math.round(trackW * rangeProgress),
                trackY + 5,
                withAlpha(accent(), alpha)
        );

        text(ctx, "1",
                trackX, rowY + 65,
                withAlpha(0xFF6F859B, alpha));

        text(ctx, "6",
                trackX + trackW - 4, rowY + 65,
                withAlpha(0xFF6F859B, alpha));

        int rightX = x + half + gap;

        card(ctx, rightX, rowY, half, 35, alpha, mx, my);

        text(ctx, "Raycast",
                rightX + 9, rowY + 12,
                withAlpha(0xFFE5F0FA, alpha));

        text(ctx,
                module.isRaycastEnabled() ? "ON" : "OFF",
                rightX + half - 31, rowY + 12,
                withAlpha(accent(), alpha));

        card(ctx, rightX, rowY + 43, half, 36, alpha, mx, my);

        text(ctx, "Rotation",
                rightX + 9, rowY + 55,
                withAlpha(0xFFE5F0FA, alpha));

        String mode = module.getRotationMode()
                == KillAuraModule.RotationMode.SMOOTH
                ? "Smooth" : "Snap";

        text(ctx, mode,
                rightX + half - 48, rowY + 55,
                withAlpha(accent(), alpha));

        int infoY = rowY + 91;

        card(ctx, x, infoY, w, 44, alpha, mx, my);

        text(ctx, "LOCAL PRACTICE",
                x + 11, infoY + 8,
                withAlpha(accent(), alpha));

        text(ctx, "This module is disabled in multiplayer.",
                x + 11, infoY + 24,
                withAlpha(0xFF8198AD, alpha));
    }

    private void drawVisuals(
            DrawContext ctx,
            Layout l,
            int mx,
            int my,
            float alpha,
            int top
    ) {
        int x = l.contentX;
        int w = l.contentW;
        int gap = 9;
        int half = (w - gap) / 2;
        int y = top + 7;

        optionCard(ctx, x, y, half, 55,
                "Particles", particles, alpha, mx, my);

        optionCard(ctx, x + half + gap, y, half, 55,
                "Glass glow", glow, alpha, mx, my);

        optionCard(ctx, x, y + 64, half, 55,
                "Grid", grid, alpha, mx, my);

        card(ctx, x + half + gap, y + 64, half, 55,
                alpha, mx, my);

        text(ctx, "ACCENT COLOR",
                x + half + gap + 9, y + 75,
                withAlpha(0xFF8DA4B8, alpha));

        String[] names = {"Mint", "Violet", "Sky", "Rose"};

        text(ctx, names[accentIndex],
                x + half + gap + 9, y + 94,
                withAlpha(accent(), alpha));

        text(ctx, "Click to cycle",
                x + half + gap + 9, y + 107,
                withAlpha(0xFF8197AC, alpha));

        card(ctx, x, y + 128, w, 43, alpha, mx, my);

        text(ctx, "Live preview",
                x + 10, y + 137,
                withAlpha(accent(), alpha));

        text(ctx, "Changes apply immediately to this menu.",
                x + 10, y + 152,
                withAlpha(0xFF8197AC, alpha));
    }

    private void drawHud(
            DrawContext ctx,
            Layout l,
            int mx,
            int my,
            float alpha,
            int top
    ) {
        int x = l.contentX;
        int w = l.contentW;
        int half = (w - 9) / 2;
        int y = top + 7;

        optionCard(ctx, x, y, half, 55,
                "Array list preview",
                arrayPreview, alpha, mx, my);

        optionCard(ctx, x + half + 9, y, half, 55,
                "Target HUD preview",
                targetPreview, alpha, mx, my);

        int previewY = y + 68;

        card(ctx, x, previewY, w, 127, alpha, mx, my);

        text(ctx, "ON-SCREEN PREVIEW",
                x + 10, previewY + 10,
                withAlpha(0xFF8DA4B8, alpha));

        if (arrayPreview) {
            int previewW = Math.max(30, Math.min(128, w / 2 - 12));

            fill(ctx,
                    x + 10, previewY + 30,
                    previewW, 22,
                    withAlpha(0xD20B1522, alpha));

            text(ctx, "AURAMOD",
                    x + 18, previewY + 37,
                    withAlpha(accent(), alpha));
        }

        if (targetPreview) {
            int hudW = Math.min(175, Math.max(100, w / 2));
            int hudX = x + w - hudW - 10;

            fill(ctx,
                    hudX, previewY + 30,
                    hudW, 59,
                    withAlpha(0xD20B1522, alpha));

            text(ctx, "Training target",
                    hudX + 8, previewY + 39,
                    withAlpha(0xFFE8F3FC, alpha));

            text(ctx, "20.0 / 20.0 HP",
                    hudX + 8, previewY + 54,
                    withAlpha(0xFF8FA9BD, alpha));

            ctx.fill(
                    hudX + 8, previewY + 74,
                    hudX + hudW - 8, previewY + 80,
                    withAlpha(0xFF35465A, alpha)
            );

            ctx.fill(
                    hudX + 8, previewY + 74,
                    hudX + hudW - 8, previewY + 80,
                    withAlpha(0xFF62E0A4, alpha)
            );
        }

        text(ctx, "Visual placeholders only",
                x + 10, previewY + 104,
                withAlpha(0xFF8197AC, alpha));
    }

    private void optionCard(
            DrawContext ctx,
            int x, int y, int w, int h,
            String label,
            boolean enabled,
            float alpha,
            int mx, int my
    ) {
        card(ctx, x, y, w, h, alpha, mx, my);

        text(ctx, label,
                x + 9, y + 12,
                withAlpha(0xFFE6F2FC, alpha));

        text(ctx, enabled ? "ON" : "OFF",
                x + 9, y + 32,
                withAlpha(enabled ? accent() : 0xFF7D91A5, alpha));

        ctx.fill(
                x + w - 35, y + 17,
                x + w - 10, y + 35,
                withAlpha(enabled ? accent() : 0xFF334459, alpha * 0.75F)
        );
    }

    private void card(
            DrawContext ctx,
            int x, int y, int w, int h,
            float alpha,
            int mx, int my
    ) {
        if (w <= 0 || h <= 0) {
            return;
        }

        boolean hovered = inside(mx, my, x, y, w, h);

        ctx.fill(
                x, y, x + w, y + h,
                withAlpha(hovered ? 0xB52D4054 : 0x92233346, alpha)
        );

        ctx.fill(
                x, y, x + w, y + 1,
                withAlpha(
                        hovered ? accent() : 0xFFD8F0FF,
                        alpha * (hovered ? 0.35F : 0.12F)
                )
        );

        ctx.drawBorder(
                x, y, w, h,
                withAlpha(0xFFB4CCDF, alpha * 0.13F)
        );
    }

    private void button(
            DrawContext ctx,
            int x, int y, int w, int h,
            int color,
            String label,
            float alpha
    ) {
        ctx.fill(
                x, y, x + w, y + h,
                withAlpha(color, alpha * 0.85F)
        );

        ctx.drawBorder(
                x, y, w, h,
                withAlpha(0xFFD8F0FF, alpha * 0.20F)
        );

        text(ctx, label,
                x + 10, y + 8,
                withAlpha(0xFFF2FAFF, alpha));
    }

    private void drawGrid(DrawContext ctx) {
        int color = withAlpha(accent(), 0.04F);

        for (int x = 0; x < width; x += 28) {
            ctx.fill(x, 0, x + 1, height, color);
        }

        for (int y = 0; y < height; y += 28) {
            ctx.fill(0, y, width, y + 1, color);
        }
    }

    private void drawParticles(DrawContext ctx) {
        for (int i = 0; i < 22; i++) {
            int px = positiveMod(
                    i * 91 + (int) (Math.sin(tick * 0.03 + i) * 16),
                    Math.max(1, width)
            );

            int py = positiveMod(
                    i * 47 - (int) (tick * (0.12F + (i % 3) * 0.04F)),
                    Math.max(1, height)
            );

            ctx.fill(
                    px, py, px + 2, py + 2,
                    withAlpha(accent(), 0.22F)
            );
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        Layout l = layout();

        if (inside(mouseX, mouseY,
                l.x + l.w - 28, l.y + 10, 20, 30)) {
            close();
            return true;
        }

        int navY = l.y + 94;

        for (Tab candidate : Tab.values()) {
            int y = navY + candidate.ordinal() * 39;

            if (inside(mouseX, mouseY,
                    l.x + 15, y - 4,
                    l.sidebarW - 28, 31)) {
                tab = candidate;
                return true;
            }
        }

        int x = l.contentX;
        int w = l.contentW;
        int top = l.y + 58;

        if (tab == Tab.COMBAT) {
            int cardY = top + 6;

            if (inside(mouseX, mouseY,
                    x + w - 85, cardY + 10, 72, 37)) {
                module.toggle();
                return true;
            }

            int gap = 9;
            int half = (w - gap) / 2;
            int rowY = cardY + 66;
            int trackX = x + 12;
            int trackW = Math.max(20, half - 24);

            if (inside(mouseX, mouseY, x, rowY, half, 79)
                    && mouseY >= rowY + 43) {
                double ratio = (mouseX - trackX) / (double) trackW;
                ratio = Math.max(0.0, Math.min(1.0, ratio));

                module.setRange(1.0 + ratio * 5.0);
                return true;
            }

            int rightX = x + half + gap;

            if (inside(mouseX, mouseY, rightX, rowY, half, 35)) {
                module.setRaycast(!module.isRaycastEnabled());
                return true;
            }

            if (inside(mouseX, mouseY, rightX, rowY + 43, half, 36)) {
                KillAuraModule.RotationMode next =
                        module.getRotationMode()
                                == KillAuraModule.RotationMode.SMOOTH
                                ? KillAuraModule.RotationMode.SNAP
                                : KillAuraModule.RotationMode.SMOOTH;

                module.setRotationMode(next);
                return true;
            }

        } else if (tab == Tab.VISUALS) {
            int gap = 9;
            int half = (w - gap) / 2;
            int y = top + 7;

            if (inside(mouseX, mouseY, x, y, half, 55)) {
                particles = !particles;
                return true;
            }

            if (inside(mouseX, mouseY,
                    x + half + gap, y, half, 55)) {
                glow = !glow;
                return true;
            }

            if (inside(mouseX, mouseY, x, y + 64, half, 55)) {
                grid = !grid;
                return true;
            }

            if (inside(mouseX, mouseY,
                    x + half + gap, y + 64, half, 55)) {
                accentIndex = (accentIndex + 1) % ACCENTS.length;
                return true;
            }

        } else if (tab == Tab.HUD) {
            int half = (w - 9) / 2;
            int y = top + 7;

            if (inside(mouseX, mouseY, x, y, half, 55)) {
                arrayPreview = !arrayPreview;
                return true;
            }

            if (inside(mouseX, mouseY,
                    x + half + 9, y, half, 55)) {
                targetPreview = !targetPreview;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }

    private Layout layout() {
        int panelW = Math.max(280, Math.min(620, width - 24));
        int panelH = Math.max(300, Math.min(390, height - 24));

        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2
                + Math.round((1.0F - openAmount) * 5.0F);

        int sidebarW = Math.min(145, Math.max(100, panelW / 4));
        int contentX = x + sidebarW + 16;
        int contentW = Math.max(130, x + panelW - 14 - contentX);

        return new Layout(
                x, y, panelW, panelH,
                sidebarW, contentX, contentW
        );
    }

    private String subtitle() {
        return switch (tab) {
            case COMBAT -> "Practice settings";
            case VISUALS -> "Glass and accent settings";
            case HUD -> "HUD widget previews";
        };
    }

    private int accent() {
        return ACCENTS[accentIndex];
    }

    private void text(
            DrawContext ctx,
            String value,
            int x, int y,
            int color
    ) {
        ctx.drawTextWithShadow(
                this.textRenderer, value, x, y, color
        );
    }

    private static boolean inside(
            double mx, double my,
            int x, int y, int w, int h
    ) {
        return mx >= x && mx <= x + w
                && my >= y && my <= y + h;
    }

    private static int withAlpha(int color, float factor) {
        int baseAlpha = (color >>> 24) & 0xFF;
        int alpha = Math.max(
                0,
                Math.min(255, Math.round(baseAlpha * factor))
        );

        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static void fill(
            DrawContext ctx,
            int x, int y, int w, int h,
            int color
    ) {
        if (w > 0 && h > 0) {
            ctx.fill(x, y, x + w, y + h, color);
        }
    }

    private static int positiveMod(int value, int mod) {
        int result = value % mod;
        return result < 0 ? result + mod : result;
    }

    private record Layout(
            int x, int y, int w, int h,
            int sidebarW, int contentX, int contentW
    ) { }
}
