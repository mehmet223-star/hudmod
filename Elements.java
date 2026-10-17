package com.example.hudmod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class Elements {
    private Elements() {}

    static final int GREEN = 0xFF55FF55, YELLOW = 0xFFFFFF55, ORANGE = 0xFFFFAA00, RED = 0xFFFF5555;
    private static int slot = 0;
    private static boolean done = false;

    static String f(double v, int d) { return String.format(Locale.ROOT, "%." + d + "f", v); }

    /** Yüksek = iyi. */
    static int hi(double v, double g, double y, double o) {
        return v >= g ? GREEN : v >= y ? YELLOW : v >= o ? ORANGE : RED;
    }

    /** Düşük = iyi. */
    static int lo(double v, double g, double y, double o) {
        return v <= g ? GREEN : v <= y ? YELLOW : v <= o ? ORANGE : RED;
    }

    static String pretty(String s) {
        StringBuilder sb = new StringBuilder();
        boolean up = true;
        for (char ch : s.toCharArray()) {
            if (ch == '_') { sb.append(' '); up = true; }
            else { sb.append(up ? Character.toUpperCase(ch) : ch); up = false; }
        }
        return sb.toString();
    }

    static String time(int sec) {
        if (sec >= 3600) return (sec / 3600) + ":" + String.format("%02d:%02d", (sec / 60) % 60, sec % 60);
        return (sec / 60) + ":" + String.format("%02d", sec % 60);
    }

    private static void add(HudElement e, boolean auto, int x, int y) {
        if (x < 0) {
            int col = slot / 8, row = slot % 8;
            slot++;
            x = 134 + col * 130;
            y = 4 + row * 26;
        }
        e.at(x, y);
        e.finish();
        e.idx = HudConfig.ELEMENTS.size();
        HudConfig.ELEMENTS.add(e);
        if (auto) e.s("color").withDefault(Setting.AUTO);
    }

    private static TextElement t(String id, String name, boolean en, int iv, TextElement.Updater u, Setting... extra) {
        return new TextElement(id, name, en, iv, u, extra);
    }

    private static final String[] DIRS = {"Güney", "Güneybatı", "Batı", "Kuzeybatı", "Kuzey", "Kuzeydoğu", "Doğu", "Güneydoğu"};

    static void registerAll() {
        if (done) return;
        done = true;

        // ---- Varsayılan açık olanlar ----
        add(t("fps", "FPS", true, 2, (mc, t) -> {
            int fps = mc.getCurrentFps();
            t.autoColor = hi(fps, 100, 60, 30);
            String a = t.fmt("FPS", String.valueOf(fps));
            if (t.s("minmax").on())
                a += " (" + (int) FrameStats.minFps + "/" + (int) FrameStats.avgFps + "/" + (int) FrameStats.maxFps + ")";
            if (t.s("low").on()) t.set(a, t.fmt("1% Low", String.valueOf((int) FrameStats.low1)));
            else t.set(a);
        }, Setting.bool("minmax", "Min / Ort / Maks göster", false), Setting.bool("low", "1% Low göster", false)), true, 4, 4);

        add(t("cps", "CPS", true, 2, (mc, t) -> {
            int l = CpsTracker.left(), r = CpsTracker.right();
            int m = t.s("mode").value;
            String v = m == 0 ? l + " | " + r : m == 1 ? String.valueOf(l) : m == 2 ? String.valueOf(r) : String.valueOf(l + r);
            if (t.s("peak").on()) v += "  (maks " + CpsTracker.peakL + "|" + CpsTracker.peakR + ")";
            t.set(t.fmt("CPS", v));
        }, Setting.choice("mode", "Mod", 0, "Sol | Sağ", "Sadece sol", "Sadece sağ", "Toplam"),
                Setting.bool("peak", "En yüksek CPS göster", false)), false, 4, 22);

        add(t("ping", "Ping", true, 10, (mc, t) -> {
            int p = 0;
            if (mc.player != null && mc.getNetworkHandler() != null) {
                var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (e != null) p = e.getLatency();
            }
            t.autoColor = lo(p, 60, 120, 200);
            t.set(t.fmt("Ping", p + " ms"));
        }), true, 4, 40);

        add(t("coords", "Koordinat", true, 5, (mc, t) -> {
            var p = mc.player;
            double x = 0, y = 64, z = 0;
            if (p != null) { x = p.getX(); y = p.getY(); z = p.getZ(); }
            String fm = "%." + t.s("dec").value + "f";
            List<String> out = new ArrayList<>();
            out.add(t.fmt("XYZ", String.format(Locale.ROOT, fm + " " + fm + " " + fm, x, y, z)));
            if (t.s("nether").on() && mc.world != null) {
                String dim = mc.world.getRegistryKey().getValue().getPath();
                if (dim.equals("overworld"))
                    out.add("Nether: " + (int) Math.floor(x / 8) + " " + (int) Math.floor(z / 8));
                else if (dim.equals("the_nether"))
                    out.add("Overworld: " + (int) Math.floor(x * 8) + " " + (int) Math.floor(z * 8));
            }
            if (t.s("chunk").on())
                out.add("Chunk: " + (((int) Math.floor(x)) >> 4) + " " + (((int) Math.floor(z)) >> 4));
            t.set(out.toArray(new String[0]));
        }, Setting.num("dec", "Ondalık basamak", 0, 3, 1, 1, ""),
                Setting.bool("nether", "Nether/Overworld çevirisi", false),
                Setting.bool("chunk", "Chunk koordinatı", false)), false, 4, 58);

        add(new KeystrokesElement(true), false, 4, 100);
        add(new ArmorElement(true), false, 4, 190);

        add(t("effects", "İksir Etkileri", true, 10, (mc, t) -> {
            PlayerEntity p = mc.player;
            List<String> out = new ArrayList<>();
            if (p != null) {
                for (StatusEffectInstance e : p.getStatusEffects()) {
                    String n = e.getEffectType().value().getName().getString();
                    int amp = e.getAmplifier() + 1;
                    String dur = e.isInfinite() ? "∞" : time(e.getDuration() / 20);
                    out.add(n + (amp > 1 ? " " + amp : "") + "  " + dur);
                    if (out.size() >= 8) break;
                }
            }
            t.hidden = p != null && out.isEmpty();
            if (out.isEmpty()) out.add("Etki yok");
            t.set(out.toArray(new String[0]));
        }), false, -1, -1);

        // ---- Varsayılan kapalı olanlar ----
        add(t("dir", "Yön", false, 5, (mc, t) -> {
            float yaw = 0, pitch = 0;
            if (mc.player != null) { yaw = mc.player.getYaw(); pitch = mc.player.getPitch(); }
            float w = MathHelper.wrapDegrees(yaw);
            int i = t.s("eight").on()
                    ? Math.floorMod((int) Math.floor((w + 22.5f) / 45f), 8)
                    : Math.floorMod((int) Math.floor((w + 45f) / 90f), 4) * 2;
            String s = DIRS[i];
            if (t.s("deg").on()) s += String.format(Locale.ROOT, " (%.0f / %.0f)", w, pitch);
            t.set(t.fmt("Yön", s));
        }, Setting.bool("eight", "8 yön", true), Setting.bool("deg", "Açıyı göster", false)), false, -1, -1);

        add(t("biome", "Biyom", false, 20, (mc, t) -> {
            String b = "-";
            if (mc.world != null && mc.player != null)
                b = pretty(mc.world.getBiome(mc.player.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("?"));
            t.set(t.fmt("Biyom", b));
        }), false, -1, -1);

        final double[] last = new double[3];
        add(t("speed", "Hız", false, 2, (mc, t) -> {
            double sp = 0;
            if (mc.player != null) {
                double dt = (HudMod.tick - last[2]) / 20.0;
                if (dt > 0 && last[2] > 0) sp = Math.hypot(mc.player.getX() - last[0], mc.player.getZ() - last[1]) / dt;
                last[0] = mc.player.getX();
                last[1] = mc.player.getZ();
                last[2] = HudMod.tick;
            }
            boolean kmh = t.s("unit").value == 1;
            if (kmh) sp *= 3.6;
            t.set(t.fmt("Hız", f(sp, 2) + (kmh ? " km/sa" : " b/sn")));
        }, Setting.choice("unit", "Birim", 0, "blok/sn", "km/sa")), false, -1, -1);

        add(t("clock", "Saat", false, 5, (mc, t) -> {
            boolean h12 = t.s("h12").on();
            String pat = (h12 ? "hh" : "HH") + ":mm" + (t.s("sec").on() ? ":ss" : "") + (h12 ? " a" : "");
            String s = LocalTime.now().format(DateTimeFormatter.ofPattern(pat, Locale.ROOT));
            if (t.s("date").on())
                t.set(t.fmt("Saat", s), LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            else t.set(t.fmt("Saat", s));
        }, Setting.bool("h12", "12 saat formatı", false), Setting.bool("sec", "Saniye göster", true),
                Setting.bool("date", "Tarih göster", false)), false, -1, -1);

        add(t("gametime", "Oyun Saati", false, 10, (mc, t) -> {
            if (mc.world == null) { t.set(t.fmt("Oyun saati", "--:--")); return; }
            long tt = mc.world.getTimeOfDay();
            long tod = tt % 24000;
            int hh = (int) ((tod / 1000 + 6) % 24);
            int mm = (int) ((tod % 1000) * 60 / 1000);
            String s = String.format(Locale.ROOT, "%02d:%02d", hh, mm);
            if (t.s("day").on()) s += " (Gün " + (tt / 24000) + ")";
            t.set(t.fmt("Oyun saati", s));
        }, Setting.bool("day", "Gün sayısı", true)), false, -1, -1);

        add(t("ram", "Bellek (RAM)", false, 20, (mc, t) -> {
            Runtime r = Runtime.getRuntime();
            long used = (r.totalMemory() - r.freeMemory()) >> 20, max = r.maxMemory() >> 20;
            double pct = used * 100.0 / Math.max(1, max);
            t.autoColor = lo(pct, 65, 85, 92);
            t.set(t.fmt("RAM", t.s("pct").on() ? (int) pct + "%" : used + "/" + max + " MB"));
        }, Setting.bool("pct", "Yüzde olarak göster", false)), true, -1, -1);

        add(t("health", "Can", false, 5, (mc, t) -> {
            var p = mc.player;
            float hp = 20, mx = 20, ab = 0;
            if (p != null) { hp = p.getHealth(); mx = p.getMaxHealth(); ab = p.getAbsorptionAmount(); }
            t.autoColor = hi(hp / Math.max(1f, mx) * 100, 75, 45, 25);
            String s = f(hp, 1);
            if (t.s("abs").on() && ab > 0) s += " +" + f(ab, 1);
            if (t.s("max").on()) s += "/" + f(mx, 0);
            t.set(t.fmt("Can", s));
        }, Setting.bool("abs", "Altın kalp göster", true), Setting.bool("max", "Maks canı göster", false)), true, -1, -1);

        add(t("hunger", "Açlık", false, 10, (mc, t) -> {
            var p = mc.player;
            int food = 20;
            float sat = 5;
            if (p != null) { food = p.getHungerManager().getFoodLevel(); sat = p.getHungerManager().getSaturationLevel(); }
            t.autoColor = hi(food, 15, 8, 4);
            String s = String.valueOf(food);
            if (t.s("sat").on()) s += " (doygunluk " + f(sat, 1) + ")";
            t.set(t.fmt("Açlık", s));
        }, Setting.bool("sat", "Doygunluk göster", false)), true, -1, -1);

        add(t("xp", "XP", false, 10, (mc, t) -> {
            var p = mc.player;
            int lvl = 0;
            float prog = 0;
            if (p != null) { lvl = p.experienceLevel; prog = p.experienceProgress; }
            String s = String.valueOf(lvl);
            if (t.s("pct").on()) s += " (" + (int) (prog * 100) + "%)";
            t.set(t.fmt("Seviye", s));
        }, Setting.bool("pct", "İlerleme yüzdesi", true)), false, -1, -1);

        add(t("server", "Sunucu", false, 20, (mc, t) -> {
            String s = "-";
            var si = mc.getCurrentServerEntry();
            if (si != null) s = t.s("hide").on() ? "***" : si.address;
            else if (mc.world != null) s = "Tek oyunculu";
            t.set(t.fmt("Sunucu", s));
        }, Setting.bool("hide", "IP gizle (yayıncı modu)", false)), false, -1, -1);

        add(t("session", "Oyun Süresi", false, 20, (mc, t) -> {
            int sec = (int) ((System.currentTimeMillis() - HudMod.START) / 1000);
            t.set(t.fmt("Süre", time(sec)));
        }), false, -1, -1);

        add(t("light", "Işık Seviyesi", false, 5, (mc, t) -> {
            int l = 15;
            if (mc.world != null && mc.player != null) l = mc.world.getLightLevel(mc.player.getBlockPos());
            t.autoColor = l >= 12 ? GREEN : l >= 8 ? YELLOW : RED;
            t.set(t.fmt("Işık", String.valueOf(l)));
        }), true, -1, -1);

        add(t("lookat", "Baktığın Blok", false, 5, (mc, t) -> {
            String s = "-";
            HitResult hr = mc.crosshairTarget;
            if (mc.world != null && hr instanceof BlockHitResult b && hr.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = b.getBlockPos();
                s = mc.world.getBlockState(pos).getBlock().getName().getString();
                if (t.s("pos").on()) s += " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
            }
            t.set(t.fmt("Blok", s));
        }, Setting.bool("pos", "Koordinatı göster", false)), false, -1, -1);

        add(t("target", "Hedef", false, 5, (mc, t) -> {
            String s = "-";
            HitResult hr = mc.crosshairTarget;
            if (hr instanceof EntityHitResult eh) {
                Entity en = eh.getEntity();
                s = en.getName().getString();
                if (t.s("hp").on() && en instanceof LivingEntity le)
                    s += "  " + f(le.getHealth(), 1) + "/" + f(le.getMaxHealth(), 0);
            }
            t.set(t.fmt("Hedef", s));
        }, Setting.bool("hp", "Hedef canı", true)), false, -1, -1);

        add(t("combo", "Combo / Menzil", false, 2, (mc, t) -> {
            int cb = System.currentTimeMillis() - Combat.last < 2000 ? Combat.combo : 0;
            List<String> out = new ArrayList<>();
            out.add(t.fmt("Combo", String.valueOf(cb)));
            if (t.s("reach").on()) out.add(t.fmt("Menzil", f(Combat.reach, 2)));
            if (t.s("hits").on()) out.add(t.fmt("Toplam vuruş", String.valueOf(Combat.hits)));
            t.set(out.toArray(new String[0]));
        }, Setting.bool("reach", "Son vuruş menzili", true), Setting.bool("hits", "Toplam vuruş", false)), false, -1, -1);

        add(t("state", "Durum", false, 5, (mc, t) -> {
            var p = mc.player;
            List<String> l = new ArrayList<>();
            if (p != null) {
                if (p.isSprinting()) l.add("Koşuyor");
                if (p.isSneaking()) l.add("Eğiliyor");
                if (p.getAbilities().flying) l.add("Uçuyor");
                if (p.isSwimming()) l.add("Yüzüyor");
            }
            t.set(t.fmt("Durum", l.isEmpty() ? "Yürüyor" : String.join(", ", l)));
        }), false, -1, -1);

        add(t("counts", "Eşya Sayacı", false, 10, (mc, t) -> {
            int totem = 0, pearl = 0, gap = 0, arrow = 0, xp = 0;
            var p = mc.player;
            if (p != null) {
                var inv = p.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack st = inv.getStack(i);
                    if (st.isEmpty()) continue;
                    int n = st.getCount();
                    if (st.isOf(Items.TOTEM_OF_UNDYING)) totem += n;
                    else if (st.isOf(Items.ENDER_PEARL)) pearl += n;
                    else if (st.isOf(Items.GOLDEN_APPLE) || st.isOf(Items.ENCHANTED_GOLDEN_APPLE)) gap += n;
                    else if (st.isOf(Items.ARROW) || st.isOf(Items.SPECTRAL_ARROW)) arrow += n;
                    else if (st.isOf(Items.EXPERIENCE_BOTTLE)) xp += n;
                }
            }
            List<String> out = new ArrayList<>();
            if (t.s("totem").on()) out.add(t.fmt("Totem", String.valueOf(totem)));
            if (t.s("pearl").on()) out.add(t.fmt("Pearl", String.valueOf(pearl)));
            if (t.s("gap").on()) out.add(t.fmt("Altın elma", String.valueOf(gap)));
            if (t.s("arrow").on()) out.add(t.fmt("Ok", String.valueOf(arrow)));
            if (t.s("xp").on()) out.add(t.fmt("XP şişesi", String.valueOf(xp)));
            if (out.isEmpty()) out.add("Sayaç kapalı");
            t.set(out.toArray(new String[0]));
        }, Setting.bool("totem", "Totem", true), Setting.bool("pearl", "Ender Pearl", true),
                Setting.bool("gap", "Altın elma", true), Setting.bool("arrow", "Ok", false),
                Setting.bool("xp", "XP şişesi", false)), false, -1, -1);

        add(t("held", "Elindeki Eşya", false, 5, (mc, t) -> {
            String s = "-";
            if (mc.player != null) {
                ItemStack st = mc.player.getMainHandStack();
                if (!st.isEmpty()) {
                    s = st.getName().getString();
                    if (st.getCount() > 1) s += " x" + st.getCount();
                    if (st.isDamageable()) s += " (" + (st.getMaxDamage() - st.getDamage()) + "/" + st.getMaxDamage() + ")";
                }
            }
            t.set(t.fmt("Eşya", s));
        }), false, -1, -1);

        add(new GraphElement(false), true, -1, -1);
    }
}
