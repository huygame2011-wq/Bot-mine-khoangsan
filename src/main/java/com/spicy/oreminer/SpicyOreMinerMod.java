package com.spicy.oreminer;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.HudVisibilityManager;
import com.spicy.oreminer.core.LogManager;
import com.spicy.oreminer.core.ModuleManager;
import com.spicy.oreminer.core.NotificationManager;
import com.spicy.oreminer.core.SafetyManager;
import com.spicy.oreminer.ui.SpicyMinerHud;
import com.spicy.oreminer.ui.SpicyMinerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SpicyOreMinerMod implements ClientModInitializer {
    public static final String MOD_ID = "spicyoreminer";

    public static KeyBinding openMenuKey;
    public static KeyBinding toggleHudKey;
    public static KeyBinding toggleSystemKey;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        HudVisibilityManager.initFromConfig();

        KeyBinding.Category mainCategory = KeyBinding.Category.create(
                Identifier.of(MOD_ID, "main")
        );

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spicyoreminer.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                mainCategory
        ));

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spicyoreminer.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                mainCategory
        ));

        toggleSystemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spicyoreminer.toggle_system",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                mainCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register(SpicyMinerHud::render);

        LogManager.log(LogManager.Level.THONG_TIN, "INIT", "Spicy Ore Miner v1 đã khởi tạo.");
    }

    private void onClientTick(MinecraftClient mc) {
        if (mc == null) {
            return;
        }

        while (openMenuKey.wasPressed()) {
            if (mc.player != null) {
                mc.setScreen(new SpicyMinerScreen(mc.currentScreen));
            }
        }

        while (toggleHudKey.wasPressed()) {
            HudVisibilityManager.toggle();
            boolean visible = HudVisibilityManager.isVisible();
            NotificationManager.push(
                    "hud_toggle",
                    "HUD thông tin",
                    visible ? "Đã bật hiển thị HUD" : "Đã ẩn HUD",
                    visible ? 0xFF69F0AE : 0xFFFFD54F,
                    true
            );
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("[Spicy Miner] HUD: " + (visible ? "BẬT" : "TẮT")),
                        true
                );
            }
        }

        while (toggleSystemKey.wasPressed()) {
            ConfigManager cfg = ConfigManager.get();
            boolean nextState = !cfg.systemEnabled;
            if (nextState && SafetyManager.isEmergencyStopped()) {
                SafetyManager.clearEmergencyStop();
            }
            ModuleManager.setSystemEnabled(mc, nextState);
            NotificationManager.push(
                    "system_toggle",
                    "Hệ thống đào",
                    nextState ? "Hệ thống đang BẬT" : "Hệ thống đã TẮT — nhả điều khiển",
                    nextState ? 0xFF69F0AE : 0xFFFF5252,
                    true
            );
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("[Spicy Miner] Hệ thống: " + (nextState ? "BẬT" : "TẮT")),
                        true
                );
            }
        }

        HudVisibilityManager.tick(mc);
        ModuleManager.onClientTick(mc);
    }
}
