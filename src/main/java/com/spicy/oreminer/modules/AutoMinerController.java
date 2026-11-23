package com.spicy.oreminer.modules;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.SafetyManager;
import net.minecraft.client.MinecraftClient;

public final class AutoMinerController {
    private static boolean wasActiveLastTick = false;

    private AutoMinerController() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.player == null || mc.world == null) {
            wasActiveLastTick = false;
            return;
        }

        ConfigManager cfg = ConfigManager.get();
        boolean canRun = cfg.systemEnabled && !SafetyManager.isEmergencyStopped();

        if (!canRun) {
            if (wasActiveLastTick) {
                releaseControls(mc);
                wasActiveLastTick = false;
            }
            return;
        }

        wasActiveLastTick = true;

        // Đặt logic quét quặng, di chuyển và đào block của bạn ở đây
        // khi hệ thống đã được bật (systemEnabled == true).
    }

    public static void stopAndRelease(MinecraftClient mc) {
        wasActiveLastTick = false;
        releaseControls(mc);
    }

    private static void releaseControls(MinecraftClient mc) {
        if (mc == null) return;
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
        if (mc.interactionManager != null) {
            mc.interactionManager.cancelBlockBreaking();
        }
    }
}
