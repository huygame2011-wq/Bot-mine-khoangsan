package com.spicy.oreminer;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.HudVisibilityManager;
import com.spicy.oreminer.core.ModuleManager;
import com.spicy.oreminer.core.SafetyManager;
import com.spicy.oreminer.modules.OreDisplayTracker;
import com.spicy.oreminer.ui.SpicyMinerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class SpicyOreMinerMod implements ClientModInitializer {
    public static final String MOD_ID = "spicyoreminer";

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onEndClientTick(MinecraftClient mc) {
        ModuleManager.onClientTick(mc);
        HudVisibilityManager.tick(mc);
    }

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.options.hudHidden) {
            return;
        }

        if (!HudVisibilityManager.isVisible() && !(mc.currentScreen instanceof SpicyMinerScreen)) {
            return;
        }

        ConfigManager cfg = ConfigManager.get();
        TextRenderer tr = mc.textRenderer;
        int x = 10;
        int y = 10;
        int width = 195;
        int height = 64;

        context.fill(x, y, x + width, y + height, 0xAA0D0B14);
        drawBoxBorder(context, x, y, width, height, 0xFF9C27B0);

        context.drawTextWithShadow(tr, Text.literal("SPICY ORE MINER v1"), x + 8, y + 6, 0xFFE1BEE7);

        String sysStatus = SafetyManager.isEmergencyStopped()
                ? "Trạng thái: DỪNG KHẨN CẤP"
                : (cfg.systemEnabled ? "Trạng thái: Đang bật" : "Trạng thái: Đang tắt");
        int sysColor = SafetyManager.isEmergencyStopped()
                ? 0xFFFF5252
                : (cfg.systemEnabled ? 0xFF69F0AE : 0xFFFFD54F);
        context.drawTextWithShadow(tr, Text.literal(sysStatus), x + 8, y + 20, sysColor);

        Map<BlockPos, Block> activeOres = OreDisplayTracker.getActiveDisplayOres();
        String oreSummary = activeOres.isEmpty()
                ? "Quặng hiển thị: 0 (Chờ mục tiêu)"
                : "Quặng hiển thị: " + activeOres.size() + " vị trí";
        context.drawTextWithShadow(tr, Text.literal(oreSummary), x + 8, y + 34, 0xFFB3E5FC);

        context.drawTextWithShadow(
                tr,
                Text.literal("HUD: " + (HudVisibilityManager.isVisible() ? "Đang hiện" : "Đang ẩn")),
                x + 8,
                y + 48,
                0xFFCCCCCC
        );
    }

    private static void drawBoxBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
}
