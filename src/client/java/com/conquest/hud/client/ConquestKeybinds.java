package com.conquest.hud.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import com.conquest.hud.ConquestHud;
import com.conquest.hud.client.gui.ConquestInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ConquestKeybinds {
    public static KeyBinding inventoryKey;
    public static KeyBinding equipmentKey;

    public static void register() {
        inventoryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.conquest.inventory",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I, // Клавиша I
                "category.conquest.keys"

        ));


        equipmentKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.conquest.equipment",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U, // Клавиша U
                "category.conquest.keys"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (inventoryKey.wasPressed()) {
                ConquestInventoryScreen.activeTab = 0; // Включаем рендер инвентаря
                ClientPlayNetworking.send(ConquestHud.OPEN_UI_PACKET, PacketByteBufs.create());
            }

            while (equipmentKey.wasPressed()) {
                ConquestInventoryScreen.activeTab = 1; // Включаем рендер экипировки
                ClientPlayNetworking.send(ConquestHud.OPEN_UI_PACKET, PacketByteBufs.create());
            }
        });
    }
}