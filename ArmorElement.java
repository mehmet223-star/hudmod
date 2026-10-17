package com.example.hudmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ArmorElement extends HudElement {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final ItemStack[] st = new ItemStack[5];
    private final String[] tx = new String[5];
    private final int[] dc = new int[5];
    private int n;
    private final Setting sDir, sDur, sHeld;

    public ArmorElement(boolean en) {
        super("armor", "Zırh / Eşya", en);
        interval = 5;
        sDir = Setting.choice("dir", "Yön", 0, "Dikey", "Yatay");
        sDur = Setting.choice("dur", "Dayanıklılık", 0, "Sayı", "Yüzde", "Yok");
        sHeld = Setting.bool("held", "Elindeki eşyayı da göster", false);
        add(sDir);
        add(sDur);
        add(sHeld);
    }

    private void put(ItemStack s) {
        st[n] = s;
        String t = "";
        int col = 0xFFFFFFFF;
        if (s.isDamageable()) {
            int max = s.getMaxDamage();
            int rem = max - s.getDamage();
            int pct = rem * 100 / Math.max(1, max);
            col = pct >= 60 ? 0xFF55FF55 : pct >= 30 ? 0xFFFFFF55 : pct >= 15 ? 0xFFFFAA00 : 0xFFFF5555;
            if (sDur.value == 0) t = String.valueOf(rem);
            else if (sDur.value == 1) t = pct + "%";
        } else if (s.getCount() > 1) {
            t = "x" + s.getCount();
        }
        tx[n] = t;
        dc[n] = col;
        n++;
    }

    @Override
    protected void refresh(MinecraftClient mc) {
        n = 0;
        PlayerEntity p = mc.player;
        TextRenderer tr = tr();
        if (p != null) {
            for (EquipmentSlot sl : SLOTS) {
                ItemStack s = p.getEquippedStack(sl);
                if (!s.isEmpty()) put(s);
            }
            if (sHeld.on()) {
                ItemStack s = p.getMainHandStack();
                if (!s.isEmpty()) put(s);
            }
        }
        hidden = p != null && n == 0;
        if (n == 0) {
            cw = tr.getWidth("Zırh");
            ch = 9;
            return;
        }
        int mw = 0;
        for (int i = 0; i < n; i++) mw = Math.max(mw, tr.getWidth(tx[i]));
        if (sDir.value == 0) {
            cw = 16 + (mw > 0 ? 3 + mw : 0);
            ch = n * 18 - 2;
        } else {
            int cell = Math.max(16, mw) + 4;
            cw = n * cell - 4;
            ch = 16 + (mw > 0 ? 10 : 0);
        }
    }

    @Override
    protected void content(DrawContext c, TextRenderer tr, int ox, int oy, int color, boolean shadow) {
        if (n == 0) {
            c.drawText(tr, "Zırh", ox, oy, color, shadow);
            return;
        }
        boolean auto = sColor.value == Setting.AUTO;
        if (sDir.value == 0) {
            for (int i = 0; i < n; i++) {
                c.drawItem(st[i], ox, oy + i * 18);
                c.drawText(tr, tx[i], ox + 19, oy + i * 18 + 4, auto ? dc[i] : color, shadow);
            }
        } else {
            int mw = 0;
            for (int i = 0; i < n; i++) mw = Math.max(mw, tr.getWidth(tx[i]));
            int cell = Math.max(16, mw) + 4;
            for (int i = 0; i < n; i++) {
                int cx = ox + i * cell + (cell - 4) / 2;
                c.drawItem(st[i], cx - 8, oy);
                Gfx.center(c, tr, tx[i], cx, oy + 17, auto ? dc[i] : color, shadow);
            }
        }
    }
}
