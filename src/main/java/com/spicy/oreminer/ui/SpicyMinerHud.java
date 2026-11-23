package com.spicy.oreminer.ui;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.HudVisibilityManager;
import com.spicy.oreminer.core.NotificationManager;
import com.spicy.oreminer.core.SafetyManager;
import com.spicy.oreminer.modules.OreDisplayTracker;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public final class SpicyMinerHud {
    private SpicyMinerHud() {}

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.options.hudHidden) {
            return;
        }

        ConfigManager cfg = ConfigManager.get();
        boolean showMainHud = HudVisibilityManager.isVisible() || (mc.currentScreen instanceof SpicyMinerScreen);
        TextRenderer tr = mc.textRenderer;

        if (showMainHud) {
            int x = Math.max(4, Math.round(cfg.hudX));
            int y = Math.max(4, Math.round(cfg.hudY));
            int width = cfg.compactHudLayout ? 175 : 210;
            int height = cfg.compactHudLayout ? 50 : 66;

            int alphaByte = Math.min(255, Math.max(40, Math.round(cfg.hudAlpha * 255.0f)));
            int bgColor = (alphaByte << 24) | 0x0D0B14;
            int borderColor = 0xFF9C27B0;

            context.fill(x, y, x + width, y + height, bgColor);
            drawBorderLines(context, x, y, width, height, borderColor);

            context.drawTextWithShadow(tr, Text.literal("SPICY ORE MINER v1"), x + 7, y + 6, 0xFFE1BEE7);

            boolean stopped = SafetyManager.isEmergencyStopped();
            String statusText = stopped
                    ? "Trạng thái: DỪNG KHẨN CẤP"
                    : (cfg.systemEnabled ? "Trạng thái: Đang bật" : "Trạng thái: Đang tắt");
            int statusColor = stopped
                    ? 0xFFFF5252
                    : (cfg.systemEnabled ? 0xFF69F0AE : 0xFFFFD54F);
            context.drawTextWithShadow(tr, Text.literal(statusText), x + 7, y + 19, statusColor);

            if (!cfg.compactHudLayout) {
                Map<BlockPos, Block> ores = OreDisplayTracker.getActiveDisplayOres();
                String oreText = (!cfg.systemEnabled || ores.isEmpty())
                        ? "Quặng theo dõi: 0"
                        : "Quặng theo dõi: " + ores.size() + " mục tiêu";
                context.drawTextWithShadow(tr, Text.literal(oreText), x + 7, y + 32, 0xFFB3E5FC);

                context.drawTextWithShadow(
                        tr,
                        Text.literal("Hồ sơ: " + cfg.activeProfile),
                        x + 7,
                        y + 45,
                        0xFFCCCCCC
                );
            }
        }

        if (NotificationManager.isHudNotificationEnabled()) {
            renderNotifications(context, tr, mc.getWindow().getScaledWidth());
        }
    }

    private static void renderNotifications(DrawContext context, TextRenderer tr, int screenWidth) {
        List<NotificationManager.Entry> entries = NotificationManager.getActiveEntries();
        if (entries.isEmpty()) {
            return;
        }

        int boxWidth = 210;
        int boxHeight = 24;
        int x = Math.max(8, screenWidth - boxWidth - 10);
        int y = 10;

        int maxShow = Math.min(3, entries.size());
        for (int i = 0; i < maxShow; i++) {
            NotificationManager.Entry item = entries.get(i);
            int curY = y + i * (boxHeight + 4);

            context.fill(x, curY, x + boxWidth, curY + boxHeight, 0xCC120E1C);
            context.fill(x, curY, x + 3, curY + boxHeight, item.color);

            context.drawTextWithShadow(tr, Text.literal(item.title), x + 8, curY + 3, item.color);
            context.drawTextWithShadow(tr, Text.literal(item.message), x + 8, curY + 13, 0xFFF5F5F5);
        }
    }

    private static void drawBorderLines(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
}
