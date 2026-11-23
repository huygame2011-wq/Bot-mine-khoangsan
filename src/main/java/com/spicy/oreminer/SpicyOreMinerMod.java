package com.spicy.oreminer;

import com.mojang.brigadier.CommandDispatcher;
import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.LogManager;
import com.spicy.oreminer.core.ModuleManager;
import com.spicy.oreminer.modules.OreSimIntegration;
import com.spicy.oreminer.ui.SpicyMinerHud;
import com.spicy.oreminer.ui.SpicyMinerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SpicyOreMinerMod implements ClientModInitializer {
    public static final String MOD_ID = "spicyoreminer";

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        ConfigManager.get();
        OreSimIntegration.checkCompatibility();
        LogManager.log(LogManager.Level.THONG_TIN, "Khởi tạo", "Spicy Ore Miner v1 đã sẵn sàng.");

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spicyoreminer.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                category
        ));

        ClientCommandRegistrationCallback.EVENT.register(SpicyOreMinerMod::registerCommands);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        HudRenderCallback.EVENT.register(SpicyMinerHud::render);
    }

    private static void registerCommands(CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("spicygui")
                .executes(ctx -> {
                    MinecraftClient mc = ctx.getSource().getClient();
                    mc.send(() -> mc.setScreen(new SpicyMinerScreen()));
                    ctx.getSource().sendFeedback(Text.literal("§d[Spicy Ore Miner] §fĐang mở bảng điều khiển..."));
                    return 1;
                }));
    }

    private void onEndClientTick(MinecraftClient mc) {
        while (openGuiKey.wasPressed()) {
            if (mc.currentScreen == null) {
                mc.setScreen(new SpicyMinerScreen());
            }
        }
        ModuleManager.onClientTick(mc);
    }
}
