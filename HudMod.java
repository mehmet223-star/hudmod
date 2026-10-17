package com.example.hudmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class HudMod implements ClientModInitializer {
    public static long tick = 1;
    public static final long START = System.currentTimeMillis();
    private static KeyBinding openEditor;

    @Override
    public void onInitializeClient() {
        Elements.registerAll();
        HudConfig.load();

        openEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hudmod.editor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of("hudmod", "main"))));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && player == MinecraftClient.getInstance().player) Combat.onAttack(entity);
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(HudMod::onTick);

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.of("hudmod", "hud"), HudMod::renderHud);
    }

    private static boolean editing(MinecraftClient mc) {
        return mc.currentScreen instanceof HudEditorScreen || mc.currentScreen instanceof SettingsScreen;
    }

    private static void onTick(MinecraftClient mc) {
        tick++;
        CpsTracker.poll(mc);
        while (openEditor.wasPressed()) mc.setScreen(new HudEditorScreen());

        if (mc.player != null && mc.player.hurtTime > 0) Combat.reset();

        // Optimizasyon: odak dışındayken güncelleme yok
        if (HudConfig.g("pause").on() && !mc.isWindowFocused()) return;

        boolean perf = HudConfig.g("perf").on();
        boolean edit = editing(mc);
        if (tick % 10 == 0) FrameStats.compute();

        for (HudElement e : HudConfig.ELEMENTS) {
            // Kapalı elemanlar sadece editörde güncellenir
            if (!e.enabled && !edit) continue;
            int iv = e.interval * (perf ? 3 : 1);
            // Elemanlar farklı tick'lere dağıtılır (aynı anda hepsi güncellenmesin)
            if ((tick + e.idx) % iv == 0) e.update(mc);
        }
    }

    private static void renderHud(DrawContext c, RenderTickCounter counter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        FrameStats.frame();
        CpsTracker.poll(mc);
        if (mc.options.hudHidden || editing(mc)) return;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        for (HudElement e : HudConfig.ELEMENTS) {
            if (!e.enabled) continue;
            if (!e.ready) e.update(mc);
            e.render(c, mc.textRenderer, false, false, sw, sh);
        }
    }
}
