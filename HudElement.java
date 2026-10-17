package com.example.hudmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class HudElement {
    public final String id, name;
    public boolean enabled, defEnabled;
    public int x, y, defX, defY, idx, interval = 10;
    public boolean ready, hidden;
    public final List<Setting> settings = new ArrayList<>();
    protected int cw, ch;
    protected int autoColor = 0xFFFFFFFF;
    protected Setting sBg, sAlpha, sColor, sShadow, sAccent, sAccentColor, sRound, sPad, sScale;

    protected HudElement(String id, String name, boolean enabled) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        this.defEnabled = enabled;
    }

    public HudElement at(int x, int y) {
        this.x = this.defX = x;
        this.y = this.defY = y;
        return this;
    }

    public void add(Setting s) { settings.add(s); }

    /** Ortak görünüm ayarlarını ekler (elemana özel ayarlardan sonra). */
    public void finish() {
        settings.add(Setting.num("scale", "Boyut", 50, 300, 10, 100, "%"));
        settings.add(Setting.bool("bg", "Arkaplan", true));
        settings.add(Setting.num("bgAlpha", "Arkaplan koyuluğu", 0, 255, 5, 144, ""));
        settings.add(Setting.color("color", "Yazı rengi", 0));
        settings.add(Setting.bool("shadow", "Yazı gölgesi", true));
        settings.add(Setting.bool("accent", "Yan çizgi", true));
        settings.add(Setting.color("accentColor", "Çizgi rengi", 5));
        settings.add(Setting.bool("round", "Yuvarlak köşe", true));
        settings.add(Setting.num("pad", "İç boşluk", 1, 10, 1, 4, ""));
        sScale = s("scale");
        sBg = s("bg");
        sAlpha = s("bgAlpha");
        sColor = s("color");
        sShadow = s("shadow");
        sAccent = s("accent");
        sAccentColor = s("accentColor");
        sRound = s("round");
        sPad = s("pad");
    }

    public Setting s(String key) {
        for (Setting st : settings) if (st.key.equals(key)) return st;
        return null;
    }

    protected static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }

    public float scale() { return sScale.value / 100f * HudConfig.gscale(); }
    public int w() { return cw + 2 * sPad.value + (sAccent.on() ? 3 : 0); }
    public int h() { return ch + 2 * sPad.value; }
    public int sw() { return Math.round(w() * scale()); }
    public int sh() { return Math.round(h() * scale()); }
    public int rx(int scrW) { return Math.max(0, Math.min(x, scrW - sw())); }
    public int ry(int scrH) { return Math.max(0, Math.min(y, scrH - sh())); }

    public boolean contains(double mx, double my) {
        return mx >= x && mx <= x + sw() && my >= y && my <= y + sh();
    }

    public final void update(MinecraftClient mc) {
        refresh(mc);
        ready = true;
    }

    protected abstract void refresh(MinecraftClient mc);

    protected abstract void content(DrawContext c, TextRenderer tr, int ox, int oy, int color, boolean shadow);

    protected int pick(int i) {
        if (i < Setting.COLORS.length) return Setting.COLORS[i];
        if (i == Setting.RAINBOW)
            return 0xFF000000 | MathHelper.hsvToRgb((System.currentTimeMillis() % 4000) / 4000f, 0.65f, 1f);
        return autoColor;
    }

    public void render(DrawContext c, TextRenderer tr, boolean dim, boolean editor, int scrW, int scrH) {
        if (hidden && !editor) return;
        draw(c, tr, rx(scrW), ry(scrH), dim);
    }

    public void draw(DrawContext c, TextRenderer tr, int px, int py, boolean dim) {
        int ww = w(), hh = h();
        float s = scale();
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(px, py);
        m.scale(s, s);
        boolean r = sRound.on();
        int a = sAlpha.value;
        if (dim) a /= 2;
        if (sBg.on() && a > 0) Gfx.rrect(c, 0, 0, ww, hh, (a << 24) | 0x0C0C14, r);
        if (sAccent.on()) {
            int ac = pick(sAccentColor.value);
            if (dim) ac = Gfx.alpha(ac, 110);
            c.fill(0, r ? 1 : 0, 2, hh - (r ? 1 : 0), ac);
        }
        int col = pick(sColor.value);
        if (dim) col = Gfx.alpha(col, 120);
        content(c, tr, sPad.value + (sAccent.on() ? 3 : 0), sPad.value, col, sShadow.on());
        m.popMatrix();
    }
}
