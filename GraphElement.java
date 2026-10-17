package com.example.hudmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class GraphElement extends HudElement {
    private final Setting sW, sH, sVal;

    public GraphElement(boolean en) {
        super("graph", "FPS Grafiği", en);
        interval = 20;
        sW = Setting.num("gw", "Genişlik", 40, 120, 10, 100, "");
        sH = Setting.num("gh", "Yükseklik", 20, 80, 10, 30, "");
        sVal = Setting.bool("gval", "FPS yazısı", true);
        add(sW);
        add(sH);
        add(sVal);
    }

    @Override
    protected void refresh(MinecraftClient mc) {
        cw = sW.value;
        ch = sH.value;
    }

    @Override
    protected void content(DrawContext c, TextRenderer tr, int ox, int oy, int color, boolean shadow) {
        boolean auto = sColor.value == Setting.AUTO;
        int w = cw, h = ch;
        for (int i = 0; i < w; i++) {
            float ms = FrameStats.get(w - 1 - i);
            if (ms <= 0) continue;
            int bh = (int) Math.min(h, Math.max(1, ms / 33.3f * h));
            int col = auto ? (ms <= 16.7f ? 0xFF55FF55 : ms <= 33.4f ? 0xFFFFFF55 : 0xFFFF5555) : color;
            c.fill(ox + i, oy + h - bh, ox + i + 1, oy + h, col);
        }
        if (sVal.on()) c.drawText(tr, String.valueOf(MinecraftClient.getInstance().getCurrentFps()), ox + 1, oy + 1, 0xFFFFFFFF, true);
    }
}
