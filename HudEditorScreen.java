package com.example.hudmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class HudEditorScreen extends Screen {
    private HudElement dragging;
    private double grabX, grabY;

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0x70000000);
        for (HudElement e : HudConfig.ELEMENTS) {
            e.render(c, textRenderer, !e.enabled);
            int x = e.x, y = e.y, w = e.width(textRenderer), h = e.height();
            int col = (e == dragging) ? 0xFFFFFF55 : e.contains(textRenderer, mouseX, mouseY) ? 0xFF55FFFF : 0xFF888888;
            c.fill(x, y, x + w, y + 1, col);
            c.fill(x, y + h - 1, x + w, y + h, col);
            c.fill(x, y, x + 1, y + h, col);
            c.fill(x + w - 1, y, x + w, y + h, col);
        }
        c.drawCenteredTextWithShadow(textRenderer, "Surukle: Sol Tik  |  Ac/Kapa: Sag Tik  |  Boyut: Fare Tekerlegi  |  Cikis: ESC", width / 2, 6, 0xFFFFFFFF);
        super.render(c, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for (int i = HudConfig.ELEMENTS.size() - 1; i >= 0; i--) {
            HudElement e = HudConfig.ELEMENTS.get(i);
            if (e.contains(textRenderer, click.x(), click.y())) {
                if (click.button() == 0) {
                    dragging = e;
                    grabX = click.x() - e.x;
                    grabY = click.y() - e.y;
                } else if (click.button() == 1) {
                    e.enabled = !e.enabled;
                }
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null && click.button() == 0) {
            int nx = (int) (click.x() - grabX);
            int ny = (int) (click.y() - grabY);
            dragging.x = Math.max(0, Math.min(width - dragging.width(textRenderer), nx));
            dragging.y = Math.max(0, Math.min(height - dragging.height(), ny));
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        for (HudElement e : HudConfig.ELEMENTS) {
            if (e.contains(textRenderer, mouseX, mouseY)) {
                e.scale = Math.max(0.5f, Math.min(3f, e.scale + (float) vAmount * 0.1f));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override
    public void removed() {
        HudConfig.save();
    }
}
