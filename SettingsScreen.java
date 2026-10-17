package com.example.hudmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {
    private static final int PW = 280, RH = 16, TOP = 34;

    private static class Row {
        final int kind; // 0 ayar, 1 aktif, 2 sıfırla
        final Setting s;
        Row(int kind, Setting s) { this.kind = kind; this.s = s; }
    }

    private final HudElement el;
    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private int scroll = 0;
    private Setting slider;

    public SettingsScreen(HudElement el, Screen parent) {
        super(Text.literal("Ayarlar"));
        this.el = el;
        this.parent = parent;
        if (el != null) {
            rows.add(new Row(1, null));
            for (Setting s : el.settings) rows.add(new Row(0, s));
            rows.add(new Row(2, null));
        } else {
            for (Setting s : HudConfig.GLOBAL) rows.add(new Row(0, s));
            rows.add(new Row(2, null));
        }
    }

    @Override
    protected void init() { if (el != null) el.update(client); }

    @Override
    public boolean shouldPause() { return false; }

    private int px() { return width / 2 - PW / 2 - (el != null ? 60 : 0); }
    private int vis() { return Math.max(1, Math.min(rows.size(), (height - TOP - 40) / RH)); }

    private int rowAt(double mx, double my) {
        if (mx < px() || mx > px() + PW || my < TOP) return -1;
        int i = (int) ((my - TOP) / RH);
        if (i >= vis()) return -1;
        int ri = i + scroll;
        return ri < rows.size() ? ri : -1;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xC0000000);
        String title = el != null ? el.name + " — ayarlar" : "Genel ayarlar";
        Gfx.center(c, textRenderer, title, width / 2, 12, 0xFF55FFFF, true);

        int px = px(), v = vis();
        Gfx.rrect(c, px - 4, TOP - 4, PW + 8, v * RH + 6, 0xE0101018, true);

        for (int i = 0; i < v; i++) {
            int ri = i + scroll;
            if (ri >= rows.size()) break;
            Row r = rows.get(ri);
            int y = TOP + i * RH;
            boolean hov = Gfx.in(mx, my, px, y, PW, RH - 2);
            c.fill(px, y, px + PW, y + RH - 2, hov ? 0x55FFFFFF : 0x40000000);

            String label, val;
            int vcol = 0xFFFFFFFF;
            Setting s = r.s;
            if (r.kind == 1) {
                label = "Aktif";
                val = el.enabled ? "AÇIK" : "KAPALI";
                vcol = el.enabled ? 0xFF55FF55 : 0xFFFF5555;
            } else if (r.kind == 2) {
                label = "Varsayılana sıfırla";
                val = "[tıkla]";
                vcol = 0xFFFFAA00;
            } else {
                label = s.label;
                val = s.text();
                if (s.type == Setting.Type.BOOL) vcol = s.on() ? 0xFF55FF55 : 0xFFFF5555;
                if (s.type == Setting.Type.INT) c.fill(px, y, px + (int) (PW * s.fraction()), y + RH - 2, 0x6040A0FF);
            }
            c.drawText(textRenderer, label, px + 6, y + 4, 0xFFFFFFFF, false);
            int vw = textRenderer.getWidth(val);
            c.drawText(textRenderer, val, px + PW - 6 - vw, y + 4, vcol, false);
            if (s != null && s.isColor()) {
                int sc = s.value < Setting.COLORS.length ? Setting.COLORS[s.value]
                        : s.value == Setting.RAINBOW
                        ? (0xFF000000 | MathHelper.hsvToRgb((System.currentTimeMillis() % 4000) / 4000f, 0.65f, 1f))
                        : 0xFF777777;
                c.fill(px + PW - 6 - vw - 12, y + 3, px + PW - 6 - vw - 4, y + 11, sc);
            }
        }

        // Canlı önizleme
        if (el != null) {
            int ox = px + PW + 14;
            c.drawText(textRenderer, "Önizleme", ox, TOP - 2, 0xFFAAAAAA, false);
            el.draw(c, textRenderer, ox, TOP + 12, !el.enabled);
        }

        // Geri butonu
        int bx = width / 2 - 40, by = height - 22;
        boolean bh = Gfx.in(mx, my, bx, by, 80, 14);
        Gfx.rrect(c, bx, by, 80, 14, bh ? 0xFF3A6FA0 : 0xFF202030, true);
        Gfx.center(c, textRenderer, "Geri", width / 2, by + 3, 0xFFFFFFFF, false);
        Gfx.center(c, textRenderer, "Sol tık: değiştir/ileri  |  Sağ tık: geri  |  Tekerlek: kaydır", width / 2, by - 12, 0xFF888888, false);
    }

    private void changed(Setting s) {
        if (el != null) el.update(client);
        else if (s != null && s.key.equals("theme")) HudConfig.applyTheme(s.value);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int b = click.button();
        if (Gfx.in(mx, my, width / 2 - 40, height - 22, 80, 14)) { close(); return true; }
        int ri = rowAt(mx, my);
        if (ri < 0) return super.mouseClicked(click, doubled);
        Row r = rows.get(ri);
        if (r.kind == 1) {
            el.enabled = !el.enabled;
            if (el.enabled) el.update(client);
        } else if (r.kind == 2) {
            if (el != null) { for (Setting s : el.settings) s.reset(); el.update(client); }
            else for (Setting s : HudConfig.GLOBAL) s.reset();
        } else {
            Setting s = r.s;
            if (s.type == Setting.Type.INT && b == 0) {
                slider = s;
                s.setFraction((mx - px()) / PW);
            } else {
                s.click(b == 1);
            }
            changed(s);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (slider != null) {
            slider.setFraction((click.x() - px()) / PW);
            changed(slider);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        boolean was = slider != null;
        slider = null;
        return was || super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        int max = Math.max(0, rows.size() - vis());
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(vAmount)));
        return true;
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void removed() { HudConfig.save(); }
}
