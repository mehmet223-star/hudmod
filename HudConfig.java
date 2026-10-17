package com.example.hudmod;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class HudConfig {
    public static final List<HudElement> ELEMENTS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("hudmod.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            for (HudElement e : ELEMENTS) {
                if (!root.has(e.id)) continue;
                JsonObject o = root.getAsJsonObject(e.id);
                e.enabled = o.get("enabled").getAsBoolean();
                e.x = o.get("x").getAsInt();
                e.y = o.get("y").getAsInt();
                e.scale = o.get("scale").getAsFloat();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (HudElement e : ELEMENTS) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", e.enabled);
                o.addProperty("x", e.x);
                o.addProperty("y", e.y);
                o.addProperty("scale", e.scale);
                root.add(e.id, o);
            }
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
