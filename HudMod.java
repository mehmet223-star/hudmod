package com.example.hudmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HudMod implements ClientModInitializer {
    private static KeyBinding openEditor;

    @Override
    public void onInitializeClient() {
        MinecraftClient mc = MinecraftClient.getInstance();

        add("fps", "FPS", () -> "FPS: " + mc.getCurrentFps());
        add("cps", "CPS", () -> "CPS: " + CpsTracker.left() + " | " + CpsTracker.right());
        add("xyz", "Koordinat", () -> mc.player == null ? "XYZ: -" :
                String.format(Locale.ROOT, "XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        add("dir", "Yon", () -> mc.player == null ? "Yon: -" :
                "Yon: " + mc.player.getHorizontalFacing().asString());
        add("ping", "Ping", () -> {
            if (mc.player == null || mc.getNetworkHandler() == null) return "Ping: -";
            var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return "Ping: " + (e == null ? "-" : e.getLatency() + " ms");
        });
        add("speed", "Hiz", () -> mc.player == null ? "Hiz: -" :
                String.format(Locale.ROOT, "Hiz: %.2f b/s", mc.player.getVelocity().horizontalLength() * 20));
        add("time", "Saat", () -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        HudConfig.load();

        openEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hudmod.editor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of("hudmod", "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CpsTracker.poll();
            while (openEditor.wasPressed()) client.setScreen(new HudEditorScreen());
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.of("hudmod", "hud"), (context, tickCounter) -> {
                    if (mc.options.hudHidden || mc.currentScreen instanceof HudEditorScreen) return;
                    CpsTracker.poll();
                    for (HudElement e : HudConfig.ELEMENTS) {
                        if (e.enabled) e.render(context, mc.textRenderer, false);
                    }
                });
    }

    private static void add(String id, String name, java.util.function.Supplier<String> s) {
        HudElement e = new HudElement(id, name, s);
        e.x = 4;
        e.y = 4 + HudConfig.ELEMENTS.size() * 18;
        HudConfig.ELEMENTS.add(e);
    }
}
