package com.spicy.oreminer.ui;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.NotificationManager;
import com.spicy.oreminer.core.SafetyManager;
import com.spicy.oreminer.modules.InventoryManager;
import com.spicy.oreminer.modules.OreSimIntegration;
import com.spicy.oreminer.modules.SurvivalManager;
import com.spicy.oreminer.modules.TargetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class SpicyMinerHud {
    private SpicyMinerHud() {}

    private static int withAlpha(int argb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(((argb >>> 24) & 0xFF) * alpha)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.options.hudHidden) return;

        ConfigManager cfg = ConfigManager.get();
        TextRenderer tr = mc.textRenderer;

        if (cfg.hudEnabled) {
            List<String[]> rows = new ArrayList<>();
            String sysLabel = SafetyManager.isEmergencyStopped() ? "DỪNG KHẨN CẤP"
                    : (SafetyManager.isPaused() ? "Tạm dừng" : (cfg.systemEnabled ? "Đang bật" : "Đã tắt"));
            rows.add(new String[]{"Hệ thống", sysLabel});
            rows.add(new String[]{"Hồ sơ", cfg.activeProfile});

            if (!cfg.compactHudLayout) {
                rows.add(new String[]{"Ore Sim", OreSimIntegration.getDataStatus()});
            }

            BlockPos tPos = TargetManager.getTargetPos();
            String targetText = (tPos == null)
                    ? "Chưa có"
                    : TargetManager.getActiveTargetOre() + " (" + tPos.getX() + ", " + tPos.getY() + ", " + tPos.getZ() + ") [" + TargetManager.getDistanceText(mc) + "]";
            rows.add(new String[]{"Mục tiêu", targetText});
            rows.add(new String[]{"Trạng thái", TargetManager.getTargetState()});
            rows.add(new String[]{"Sinh tồn", SurvivalManager.getStatusText()});

            String durText = InventoryManager.getLowestToolDurability() < 0
                    ? "Không có"
                    : String.valueOf(InventoryManager.getLowestToolDurability());
            rows.add(new String[]{"Kho đồ", "Trống: " + InventoryManager.getEmptySlots()
                    + " | Bánh mì: " + InventoryManager.getBreadCount()
                    + " | Độ bền: " + durText});
            rows.add(new String[]{"Cảnh báo", SafetyManager.getCurrentWarning()});

            int width = 225;
            for (String[] r : rows) {
                int w = tr.getWidth(r[0] + ": " + r[1]) + 18;
                if (w > width) width = Math.min(w, 340);
            }
            int lineHeight = 11;
            int headerHeight = 16;
            int height = headerHeight + rows.size() * lineHeight + 6;

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(cfg.hudX, cfg.hudY);
            context.getMatrices().scale(cfg.hudScale, cfg.hudScale);

            int bgColor = withAlpha(0xFF0B0A12, cfg.hudAlpha);
            int borderColor = withAlpha(0xFF7C3AED, Math.min(1.0f, cfg.hudAlpha + 0.1f));
            int headerBg = withAlpha(0xFF1A102E, cfg.hudAlpha);

            context.fill(0, 0, width, height, bgColor);
            context.fill(0, 0, width, headerHeight, headerBg);
            context.fill(0, 0, width, 1, borderColor);
            context.fill(0, height - 1, width, height, borderColor);
            context.fill(0, 0, 1, height, borderColor);
            context.fill(width - 1, 0, width, height, borderColor);

            context.drawTextWithShadow(tr, "SPICY ORE MINER", 6, 4, 0xFFC084FC);

            int y = headerHeight + 3;
            for (String[] r : rows) {
                String key = r[0] + ": ";
                String val = tr.trimToWidth(r[1], Math.max(20, width - tr.getWidth(key) - 12));
                context.drawTextWithShadow(tr, key, 6, y, 0xFFA78BFA);
                int valColor = "Cảnh báo".equals(r[0]) && !"Không có cảnh báo".equals(r[1]) ? 0xFFFF8A65 : 0xFFF3E8FF;
                context.drawTextWithShadow(tr, val, 6 + tr.getWidth(key), y, valColor);
                y += lineHeight;
            }

            context.getMatrices().popMatrix();
        }

        if (cfg.notificationsEnabled && cfg.notifHudEnabled) {
            List<NotificationManager.Toast> toasts = NotificationManager.getActiveToasts();
            int screenW = mc.getWindow().getScaledWidth();
            int ty = 12;
            for (NotificationManager.Toast t : toasts) {
                int boxW = Math.min(250, Math.max(150, tr.getWidth(t.detail()) + 16));
                int tx = screenW - boxW - 12;
                int boxH = 26;
                context.fill(tx, ty, tx + boxW, ty + boxH, withAlpha(0xFF110D1D, cfg.hudAlpha));
                context.fill(tx, ty, tx + 3, ty + boxH, t.color());
                context.drawTextWithShadow(tr, tr.trimToWidth(t.title(), boxW - 10), tx + 7, ty + 4, t.color());
                context.drawTextWithShadow(tr, tr.trimToWidth(t.detail(), boxW - 10), tx + 7, ty + 14, 0xFFE9D5FF);
                ty += boxH + 5;
            }
        }
    }
}
