package com.spicy.oreminer.modules;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.LogManager;
import com.spicy.oreminer.core.NotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

public final class InventoryManager {
    private static int emptySlots = 36;
    private static int breadCount = 0;
    private static int lowestToolDurability = -1;
    private static String toolName = "Không có công cụ";
    private static boolean inventoryFull = false;

    private InventoryManager() {}

    public static void tick(MinecraftClient mc) {
        if (!ConfigManager.get().inventoryEnabled) return;
        if (mc.player == null) return;

        PlayerInventory inv = mc.player.getInventory();
        int free = 0;
        int bread = 0;
        int minDurability = Integer.MAX_VALUE;
        String activeToolName = "Không có công cụ";

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) {
                free++;
                continue;
            }
            if (stack.isOf(Items.BREAD)) {
                bread += stack.getCount();
            }
            if (stack.isIn(ItemTags.PICKAXES) && stack.isDamageable()) {
                int remaining = stack.getMaxDamage() - stack.getDamage();
                if (remaining < minDurability) {
                    minDurability = remaining;
                    activeToolName = stack.getName().getString();
                }
            }
        }

        emptySlots = free;
        breadCount = bread;
        lowestToolDurability = (minDurability == Integer.MAX_VALUE) ? -1 : minDurability;
        toolName = activeToolName;

        boolean nowFull = (free == 0);
        if (nowFull && !inventoryFull) {
            LogManager.log(LogManager.Level.CANH_BAO, "Kho đồ", "Túi đồ đã đầy (0 ô trống).");
            NotificationManager.push("inv_full", "Kho đồ đầy", "Không còn ô trống trong túi đồ.", 0xFFFFD54F, ConfigManager.get().notifInventoryFull);
        }
        inventoryFull = nowFull;

        int durThreshold = ConfigManager.get().lowDurabilityThreshold;
        if (lowestToolDurability >= 0 && lowestToolDurability <= durThreshold) {
            LogManager.log(LogManager.Level.CANH_BAO, "Kho đồ",
                    "Độ bền công cụ thấp (" + toolName + ": còn " + lowestToolDurability + ").");
            NotificationManager.push("low_dur", "Độ bền công cụ thấp",
                    toolName + " chỉ còn " + lowestToolDurability + " độ bền.", 0xFFFF8A65, ConfigManager.get().notifLowDurability);
        }

        int breadThreshold = ConfigManager.get().lowBreadThreshold;
        if (breadCount <= breadThreshold) {
            NotificationManager.push("low_bread", "Bánh mì sắp hết",
                    "Chỉ còn " + breadCount + " bánh mì trong kho.", 0xFFFFD54F, ConfigManager.get().notifLowBread);
        }
    }

    public static int getEmptySlots() { return emptySlots; }
    public static int getBreadCount() { return breadCount; }
    public static int getLowestToolDurability() { return lowestToolDurability; }
    public static String getToolName() { return toolName; }
    public static boolean isInventoryFull() { return inventoryFull; }
}
