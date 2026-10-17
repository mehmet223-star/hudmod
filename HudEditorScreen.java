package com.example.hudmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class HudEditorScreen extends Screen {
    private static final String[] BTN = {"Liste", "Genel Ayarlar", "Hepsini Aç", "Hepsini Kapat", "Sıfırla"};
    private static final int PW = 130, ROW = 12, PY = 22;

    private HudElement dragging;
    private double grabX, grabY;
    private int guideX = -1, guideY = -1;
    private boolean panel = true;
    private int panelScroll = 0;

    public HudEditorScreen() {
        super(Text.literal("HUD Editörü"));
    }

    @Override
    protected void init() {
        for (HudElement e : HudConfig.ELEMENTS) {
            e.update(client);
            e.x = e.rx(width);
            e.y = e.ry(height);
        }
    }

    @Override
    public boolean shouldPause() { return false; }

    private int bw() { return Math.max(40, Math.min(78, (width - 20 - 16) / 5)); }
    private int bx0() { return (width - (5 * bw() + 16)) / 2; }
    private int by() { return height - 20; }
    private int px() { return width - PW - 4; }
    private int vis() { return Math.max(1, Math.min(HudConfig.ELEMENTS.size(), (height - PY - 14 - 30) / ROW)); }

    private int buttonAt(double mx, double my) {
        for (int i = 0; i < BTN.length; i++)
            if (Gfx.in(mx, my, bx0() + i * (bw() + 4), by(), bw(), 14)) return i;
        return -1;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x90000000);
        c.fill(width / 2, 0, width / 2 + 1, height, 0x22FFFFFF);
        c.fill(0, height / 2, width, height / 2 + 1, 0x22FFFFFF);

        List<HudElement> all = HudConfig.ELEMENTS;
        HudElement hover = null;
        if (dragging == null && !(panel && Gfx.in(mx, my, px(), PY, PW, 14 + vis() * ROW))) {
            for (int i = all.size() - 1; i >= 0; i--)
                if (all.get(i).contains(mx, my)) { hover = all.get(i); break; }
        }

        for (HudElement e : all) {
            e.render(c, textRenderer, !e.enabled, true, width, height);
            int col = e == dragging ? 0xFFFFFF55 : e == hover ? 0xFF55FFFF : 0x66888888;
            Gfx.border(c, e.x, e.y, e.sw(), e.sh(), col);
        }

        if (dragging != null) {
            if (guideX >= 0) c.fill(guideX, 0, guideX + 1, height, 0xCCFF55FF);
            if (guideY >= 0) c.fill(0, guideY, width, guideY + 1, 0xCCFF55FF);
        }

        Gfx.center(c, textRenderer, "Sürükle: taşı  |  Sağ tık: ayarlar  |  Orta tık: aç/kapa  |  Tekerlek: boyut  |  ESC: kaydet & çık",
                width / 2, 6, 0xFFFFFFFF, true);
        if (hover != null) {
            String tip = hover.name + (hover.enabled ? "" : " (kapalı)");
            int ty = Math.max(16, hover.y - 11);
            c.drawText(textRenderer, tip, hover.x, ty, 0xFF55FFFF, true);
        }

        // Eleman listesi
        if (panel) {
            int px = px(), v = vis();
            Gfx.rrect(c, px, PY, PW, 14 + v * ROW + 2, 0xE0101018, true);
            c.drawText(textRenderer, "Elemanlar", px + 6, PY + 3, 0xFF55FFFF, false);
            for (int i = 0; i < v; i++) {
                int ei = i + panelScroll;
                if (ei >= all.size()) break;
                HudElement e = all.get(ei);
                int ry = PY + 14 + i * ROW;
                if (Gfx.in(mx, my, px, ry, PW, ROW)) c.fill(px + 2, ry, px + PW - 2, ry + ROW, 0x30FFFFFF);
                c.fill(px + 5, ry + 3, px + 10, ry + 8, e.enabled ? 0xFF55FF55 : 0xFF666666);
                c.drawText(textRenderer, e.name, px + 14, ry + 2, e.enabled ? 0xFFFFFFFF : 0xFF999999, false);
            }
        }

        // Alt butonlar
        for (int i = 0; i < BTN.length; i++) {
            int x = bx0() + i * (bw() + 4), y = by();
            boolean h = Gfx.in(mx, my, x, y, bw(), 14);
            Gfx.rrect(c, x, y, bw(), 14, h ? 0xFF3A6FA0 : 0xFF202030, true);
            String label = i == 0 ? (panel ? "Listeyi gizle" : "Liste") : BTN[i];
            Gfx.center(c, textRenderer, label, x + bw() / 2, y + 3, 0xFFFFFFFF, false);
        }
    }

    private void onButton(int i) {
        switch (i) {
            case 0 -> panel = !panel;
            case 1 -> client.setScreen(new SettingsScreen(null, this));
            case 2 -> { for (HudElement e : HudConfig.ELEMENTS) { e.enabled = true; e.update(client); } }
            case 3 -> { for (HudElement e : HudConfig.ELEMENTS) e.enabled = false; }
            case 4 -> {
                HudConfig.resetAll();
                for (HudElement e : HudConfig.ELEMENTS) e.update(client);
            }
            default -> { }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int b = click.button();

        int bi = buttonAt(mx, my);
        if (bi >= 0 && b == 0) { onButton(bi); return true; }

        if (panel && Gfx.in(mx, my, px(), PY, PW, 14 + vis() * ROW)) {
            int row = (int) ((my - PY - 14) / ROW);
            int ei = row + panelScroll;
            if (my >= PY + 14 && row < vis() && ei >= 0 && ei < HudConfig.ELEMENTS.size()) {
                HudElement e = HudConfig.ELEMENTS.get(ei);
                if (b == 0) { e.enabled = !e.enabled; if (e.enabled) e.update(client); }
                else if (b == 1) client.setScreen(new SettingsScreen(e, this));
            }
            return true;
        }

        for (int i = HudConfig.ELEMENTS.size() - 1; i >= 0; i--) {
            HudElement e = HudConfig.ELEMENTS.get(i);
            if (!e.contains(mx, my)) continue;
            if (b == 0) {
                dragging = e;
                grabX = mx - e.x;
                grabY = my - e.y;
            } else if (b == 1) {
                client.setScreen(new SettingsScreen(e, this));
            } else if (b == 2) {
                e.enabled = !e.enabled;
                if (e.enabled) e.update(client);
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging == null || click.button() != 0) return super.mouseDragged(click, offsetX, offsetY);
        int sw = dragging.sw(), sh = dragging.sh();
        int nx = (int) (click.x() - grabX), ny = (int) (click.y() - grabY);

        if (HudConfig.g("snap").on()) {
            int g = HudConfig.g("grid").value;
            nx = Math.round(nx / (float) g) * g;
            ny = Math.round(ny / (float) g) * g;
        }

        guideX = -1;
        guideY = -1;
        if (HudConfig.g("guides").on()) {
            int[] mineX = {nx, nx + sw / 2, nx + sw};
            int[] mineY = {ny, ny + sh / 2, ny + sh};
            int bestX = 4, bestY = 4, snapX = nx, snapY = ny;
            for (HudElement o : HudConfig.ELEMENTS) {
                if (o == dragging || !o.enabled) continue;
                int[] ox = {o.x, o.x + o.sw() / 2, o.x + o.sw()};
                int[] oy = {o.y, o.y + o.sh() / 2, o.y + o.sh()};
                for (int a = 0; a < 3; a++) {
                    for (int bb = 0; bb < 3; bb++) {
                        int dx = ox[bb] - mineX[a];
                        if (Math.abs(dx) < bestX) { bestX = Math.abs(dx); snapX = nx + dx; guideX = ox[bb]; }
                        int dy = oy[bb] - mineY[a];
                        if (Math.abs(dy) < bestY) { bestY = Math.abs(dy); snapY = ny + dy; guideY = oy[bb]; }
                    }
                }
            }
            int dxc = width / 2 - mineX[1];
            if (Math.abs(dxc) < bestX) { snapX = nx + dxc; guideX = width / 2; }
            int dyc = height / 2 - mineY[1];
            if (Math.abs(dyc) < bestY) { snapY = ny + dyc; guideY = height / 2; }
            nx = snapX;
            ny = snapY;
        }

        dragging.x = Math.max(0, Math.min(width - sw, nx));
        dragging.y = Math.max(0, Math.min(height - sh, ny));
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        boolean was = dragging != null;
        dragging = null;
        guideX = -1;
        guideY = -1;
        return was || super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if (panel && Gfx.in(mx, my, px(), PY, PW, 14 + vis() * ROW)) {
            int max = Math.max(0, HudConfig.ELEMENTS.size() - vis());
            panelScroll = Math.max(0, Math.min(max, panelScroll - (int) Math.signum(vAmount)));
            return true;
        }
        for (int i = HudConfig.ELEMENTS.size() - 1; i >= 0; i--) {
            HudElement e = HudConfig.ELEMENTS.get(i);
            if (e.contains(mx, my)) {
                Setting s = e.s("scale");
                s.value = Math.max(s.min, Math.min(s.max, s.value + (int) Math.signum(vAmount) * s.step));
                e.x = e.rx(width);
                e.y = e.ry(height);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }

    @Override
    public void removed() { HudConfig.save(); }
}
