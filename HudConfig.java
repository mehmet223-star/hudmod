package com.example.hudmod;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HudConfig {
    public static final List<HudElement> ELEMENTS = new ArrayList<>();
    public static final List<Setting> GLOBAL = new ArrayList<>();
    private static final Map<String, Setting> GMAP = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String[] THEMES = {"Koyu", "Cam", "Neon", "Minimal"};
    private static final Setting G_SCALE;

    static {
        G_SCALE = Setting.num("gscale", "Genel HUD boyutu", 50, 200, 10, 100, "%");
        g(Setting.choice("theme", "Tema (tıkla = hepsine uygula)", 0, THEMES));
        g(G_SCALE);
        g(Setting.bool("perf", "Performans modu (daha seyrek güncelle)", false));
        g(Setting.bool("pause", "Pencere odakta değilken güncellemeyi durdur", true));
        g(Setting.bool("snap", "Editörde ızgaraya yapış", true));
        g(Setting.num("grid", "Izgara boyutu", 1, 20, 1, 4, " px"));
        g(Setting.bool("guides", "Hizalama çizgileri", true));
    }

    private static void g(Setting s) {
        GLOBAL.add(s);
        GMAP.put(s.key, s);
    }

    public static Setting g(String key) { return GMAP.get(key); }
    public static float gscale() { return G_SCALE.value / 100f; }

    private static void set(HudElement e, String k, int v) {
        Setting s = e.s(k);
        if (s != null) s.value = v;
    }

    public static void applyTheme(int t) {
        for (HudElement e : ELEMENTS) {
            switch (t) {
                case 0:
                    set(e, "bg", 1); set(e, "bgAlpha", 144); set(e, "accent", 1); set(e, "accentColor", 5); set(e, "round", 1);
                    break;
                case 1:
                    set(e, "bg", 1); set(e, "bgAlpha", 70); set(e, "accent", 0); set(e, "round", 1);
                    break;
                case 2:
                    set(e, "bg", 1); set(e, "bgAlpha", 190); set(e, "accent", 1); set(e, "accentColor", Setting.RAINBOW); set(e, "round", 0);
                    break;
                default:
                    set(e, "bg", 0); set(e, "accent", 0); set(e, "round", 0);
            }
        }
    }

    public static void resetAll() {
        for (HudElement e : ELEMENTS) {
            e.enabled = e.defEnabled;
            e.x = e.defX;
            e.y = e.defY;
            for (Setting s : e.settings) s.reset();
        }
        for (Setting s : GLOBAL) s.reset();
    }

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("hudmod.json"); }

    public static void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            if (root.has("global")) {
                JsonObject go = root.getAsJsonObject("global");
                for (Setting s : GLOBAL)
                    if (go.has(s.key)) { s.value = go.get(s.key).getAsInt(); s.clamp(); }
            }
            JsonObject eo = root.has("elements") ? root.getAsJsonObject("elements") : root;
            for (HudElement e : ELEMENTS) {
                if (!eo.has(e.id) || !eo.get(e.id).isJsonObject()) continue;
                JsonObject o = eo.getAsJsonObject(e.id);
                if (o.has("enabled")) e.enabled = o.get("enabled").getAsBoolean();
                if (o.has("x")) e.x = o.get("x").getAsInt();
                if (o.has("y")) e.y = o.get("y").getAsInt();
                if (o.has("settings")) {
                    JsonObject so = o.getAsJsonObject("settings");
                    for (Setting s : e.settings)
                        if (so.has(s.key)) { s.value = so.get(s.key).getAsInt(); s.clamp(); }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            JsonObject go = new JsonObject();
            for (Setting s : GLOBAL) go.addProperty(s.key, s.value);
            root.add("global", go);
            JsonObject eo = new JsonObject();
            for (HudElement e : ELEMENTS) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", e.enabled);
                o.addProperty("x", e.x);
                o.addProperty("y", e.y);
                JsonObject so = new JsonObject();
                for (Setting s : e.settings) so.addProperty(s.key, s.value);
                o.add("settings", so);
                eo.add(e.id, o);
            }
            root.add("elements", eo);
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
