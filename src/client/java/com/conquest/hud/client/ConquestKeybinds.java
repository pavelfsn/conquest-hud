package com.conquest.hud.client;

import com.conquest.hud.client.gui.ConquestInventoryScreen;
import com.conquest.hud.client.network.ClientPacketSender;
import com.conquest.hud.core.container.EquipmentSlot;
import com.conquest.hud.core.network.ContainerActionType;
import com.vicmatskiv.pointblank.client.GunClientState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ConquestKeybinds {
    public static KeyBinding openInvKey;
    public static KeyBinding openEqKey;
    public static KeyBinding freeCursorKey;
    public static KeyBinding holsterKey;
    public static KeyBinding primaryWeaponKey;
    public static KeyBinding secondaryWeaponKey;
    public static KeyBinding openProgressionKey;
    public static KeyBinding[] actionKeys = new KeyBinding[9];

    private static boolean wasForcedFirstPerson = false;
    private static boolean wasAimingLastTick = false;
    private static int aimTransitionTicks = 0;

    public static void register() {
        openInvKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.open_inv", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.conquest.keys"));
        openEqKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.open_eq", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.conquest.keys"));
        freeCursorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.free_cursor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_TAB, "category.conquest.keys"));
        holsterKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.holster", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.conquest.keys"));
        primaryWeaponKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.primary", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.conquest.keys"));
        secondaryWeaponKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.secondary", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.conquest.keys"));
        openProgressionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.open_progression", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.conquest.keys"));

        int[] defaultKeys = {GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_9};
        for (int i = 0; i < 9; i++) {
            actionKeys[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.conquest.slot_" + (i + 1), InputUtil.Type.KEYSYM, defaultKeys[i], "category.conquest.keys"));
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean invPressed = openInvKey.wasPressed();
            boolean eqPressed = openEqKey.wasPressed();
            boolean cursorPressed = freeCursorKey.wasPressed();
            boolean progPressed = openProgressionKey.wasPressed();

            if ((invPressed || eqPressed || cursorPressed || progPressed) && client.currentScreen == null) {
                ConquestInventoryScreen screen = new ConquestInventoryScreen();
                client.setScreen(screen);
                screen.setInitialVisibility(invPressed, eqPressed, progPressed);
            }

            if (client.player != null && client.currentScreen == null) {
                client.player.getInventory().selectedSlot = 0;
                while (client.options.dropKey.wasPressed()) {}

                // ПЛАВНАЯ КАМЕРА (PUBG STYLE)
                GunClientState state = GunClientState.getMainHeldState();
                boolean isAiming = state != null && state.isAiming();
                Perspective currentPerspective = client.options.getPerspective();

                if (isAiming && !wasAimingLastTick) {
                    aimTransitionTicks = 4; // Задержка перед входом в прицел
                }

                if (isAiming) {
                    if (aimTransitionTicks > 0) {
                        aimTransitionTicks--;
                    } else if (currentPerspective != Perspective.FIRST_PERSON) {
                        client.options.setPerspective(Perspective.FIRST_PERSON);
                        wasForcedFirstPerson = true;
                    }
                } else {
                    aimTransitionTicks = 0;
                    if (wasForcedFirstPerson && currentPerspective == Perspective.FIRST_PERSON) {
                        client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                        wasForcedFirstPerson = false;
                    }
                }
                wasAimingLastTick = isAiming;

                if (holsterKey.wasPressed()) ClientPacketSender.sendAction(ContainerActionType.USE, 1, -1, -1, -1);
                if (primaryWeaponKey.wasPressed()) ClientPacketSender.sendAction(ContainerActionType.USE, 1, EquipmentSlot.PRIMARY_WEAPON.getIndex(), -1, -1);
                if (secondaryWeaponKey.wasPressed()) ClientPacketSender.sendAction(ContainerActionType.USE, 1, EquipmentSlot.SECONDARY_WEAPON.getIndex(), -1, -1);
                for (int i = 0; i < 9; i++) {
                    while (actionKeys[i].wasPressed()) ClientPacketSender.sendAction(ContainerActionType.USE, 2, i, -1, -1);
                }
            }
        });
    }
}