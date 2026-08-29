package com.conquest.hud.client;

import com.conquest.hud.ConquestHud;
import com.conquest.hud.client.gui.ConquestInventoryScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ConquestKeybinds {
    public static KeyBinding inventoryKey;
    public static KeyBinding equipmentKey;

    private static long lastToggleTime = 0;

    public static void register() {
        inventoryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.conquest.inventory", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.conquest.keys"));
        equipmentKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.conquest.equipment", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.conquest.keys"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen instanceof ConquestInventoryScreen) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastToggleTime < 250) return; // Задержка 250 мс

            if (inventoryKey.wasPressed()) {
                lastToggleTime = currentTime;
                ConquestInventoryScreen.activeFlags = ConquestInventoryScreen.FLAG_INVENTORY;
                ClientPlayNetworking.send(ConquestHud.OPEN_UI_PACKET, PacketByteBufs.create());
            } else if (equipmentKey.wasPressed()) {
                lastToggleTime = currentTime;
                ConquestInventoryScreen.activeFlags = ConquestInventoryScreen.FLAG_EQUIPMENT;
                ClientPlayNetworking.send(ConquestHud.OPEN_UI_PACKET, PacketByteBufs.create());
            }
        });
    }
}