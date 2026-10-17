package com.example.hudmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public class Combat {
    public static int combo, hits;
    public static long last;
    public static float reach;

    public static void onAttack(Entity e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        combo = (now - last < 2000) ? combo + 1 : 1;
        last = now;
        hits++;
        reach = mc.player.distanceTo(e);
    }

    public static void reset() { combo = 0; }
}
