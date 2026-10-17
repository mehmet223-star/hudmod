package com.example.hudmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class TextElement extends HudElement {
    public interface Updater { void run(MinecraftClient mc, TextElement t); }

    private final Updater up;
    private String[] lines = {""};
    private final Setting sLabel;

    public TextElement(String id, String name, boolean en, int interval, Updater up, Setting... extra) {
        super(id, name, en);
        this.interval = interval;
        this.up = up;
        sLabel = Setting.bool("label", "Etiket göster", true);
        settings.add(sLabel);
        for (Setting s : extra) settings.add(s);
    }

    public String fmt(String label, String value) {
        return sLabel.on() ? label + ": " + value : value;
    }

    public void set(String... l) {
        lines = l;
        TextRenderer tr = tr();
        int mw = 0;
        for (String s : l) mw = Math.max(mw, tr.getWidth(s));
        cw = mw;
        ch = l.length * 10 - 1;
    }

    @Override
    protected void refresh(MinecraftClient mc) { up.run(mc, this); }

    @Override
    protected void content(DrawContext c, TextRenderer tr, int ox, int oy, int color, boolean shadow) {
        for (int i = 0; i < lines.length; i++) c.drawText(tr, lines[i], ox, oy + i * 10, color, shadow);
    }
}
