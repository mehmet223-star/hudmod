package com.example.hudmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;

public class KeystrokesElement extends HudElement {
    private static final int B = 18, G = 2;
    private final Setting sMouse, sCps, sSpace, sShift, sKeyAlpha;

    public KeystrokesElement(boolean en) {
        super("keys", "Tuşlar (WASD/Fare)", en);
        interval = 20;
        sMouse = Setting.bool("mouse", "Fare tuşları (LMB/RMB)", true);
        sCps = Setting.bool("kcps", "Tuşlarda CPS göster", true);
        sSpace = Setting.bool("space", "Boşluk tuşu", true);
        sShift = Setting.bool("shift", "Shift / Koşu tuşları", false);
        sKeyAlpha = Setting.num("kAlpha", "Tuş koyuluğu", 0, 255, 5, 110, "");
        add(sMouse);
        add(sCps);
        add(sSpace);
        add(sShift);
        add(sKeyAlpha);
    }

    @Override
    protected void refresh(MinecraftClient mc) {
        cw = 3 * B + 2 * G;
        int hh = 2 * B + G;
        if (sMouse.on()) hh += G + (sCps.on() ? 20 : B);
        if (sSpace.on()) hh += G + 11;
        if (sShift.on()) hh += G + 11;
        ch = hh;
    }

    private void box(DrawContext c, TextRenderer tr, int x, int y, int w, int h, String label, boolean down, int color, boolean shadow) {
        int bg = down ? 0xB0FFFFFF : (sKeyAlpha.value << 24) | 0x000000;
        Gfx.rrect(c, x, y, w, h, bg, sRound.on());
        Gfx.center(c, tr, label, x + w / 2, y + (h - 8) / 2, down ? 0xFF111111 : color, down ? false : shadow);
    }

    @Override
    protected void content(DrawContext c, TextRenderer tr, int ox, int oy, int color, boolean shadow) {
        GameOptions o = MinecraftClient.getInstance().options;
        int y = oy;
        box(c, tr, ox + B + G, y, B, B, "W", o.forwardKey.isPressed(), color, shadow);
        y += B + G;
        box(c, tr, ox, y, B, B, "A", o.leftKey.isPressed(), color, shadow);
        box(c, tr, ox + B + G, y, B, B, "S", o.backKey.isPressed(), color, shadow);
        box(c, tr, ox + 2 * (B + G), y, B, B, "D", o.rightKey.isPressed(), color, shadow);
        y += B + G;
        if (sMouse.on()) {
            int mw = (cw - G) / 2;
            int mh = sCps.on() ? 20 : B;
            boolean l = CpsTracker.leftDown, r = CpsTracker.rightDown;
            if (sCps.on()) {
                boxTwo(c, tr, ox, y, mw, mh, "LMB", String.valueOf(CpsTracker.left()), l, color, shadow);
                boxTwo(c, tr, ox + mw + G, y, mw, mh, "RMB", String.valueOf(CpsTracker.right()), r, color, shadow);
            } else {
                box(c, tr, ox, y, mw, mh, "LMB", l, color, shadow);
                box(c, tr, ox + mw + G, y, mw, mh, "RMB", r, color, shadow);
            }
            y += mh + G;
        }
        if (sSpace.on()) {
            box(c, tr, ox, y, cw, 11, "SPACE", o.jumpKey.isPressed(), color, shadow);
            y += 11 + G;
        }
        if (sShift.on()) {
            int mw = (cw - G) / 2;
            box(c, tr, ox, y, mw, 11, "SHIFT", o.sneakKey.isPressed(), color, shadow);
            box(c, tr, ox + mw + G, y, mw, 11, "KOŞU", o.sprintKey.isPressed(), color, shadow);
        }
    }

    private void boxTwo(DrawContext c, TextRenderer tr, int x, int y, int w, int h, String top, String bottom, boolean down, int color, boolean shadow) {
        int bg = down ? 0xB0FFFFFF : (sKeyAlpha.value << 24);
        Gfx.rrect(c, x, y, w, h, bg, sRound.on());
        int tc = down ? 0xFF111111 : color;
        Gfx.center(c, tr, top, x + w / 2, y + 2, tc, !down && shadow);
        Gfx.center(c, tr, bottom, x + w / 2, y + 11, down ? 0xFF333333 : 0xFFAAAAAA, false);
    }
}
