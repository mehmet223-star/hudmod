package com.example.hudmod;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public class HudElement {
    public final String id, name;
    public final Supplier<String> text;
    public boolean enabled = true;
    public int x, y;
    public float scale = 1f;

    public HudElement(String id, String name, Supplier<String> text) {
        this.id = id;
        this.name = name;
        this.text = text;
    }

    public int width(TextRenderer tr) { return Math.round((tr.getWidth(text.get()) + 8) * scale); }
    public int height() { return Math.round(15 * scale); }

    public boolean contains(TextRenderer tr, double mx, double my) {
        return mx >= x && mx <= x + width(tr) && my >= y && my <= y + height();
    }

    public void render(DrawContext c, TextRenderer tr, boolean dim) {
        String s = text.get();
        int w = tr.getWidth(s) + 8;
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(scale, scale);
        c.fill(0, 0, w, 15, dim ? 0x40000000 : 0x90000000);
        c.drawText(tr, s, 4, 3, dim ? 0x80FFFFFF : 0xFFFFFFFF, true);
        m.popMatrix();
    }
}
