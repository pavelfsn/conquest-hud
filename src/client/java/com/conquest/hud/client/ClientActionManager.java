package com.conquest.hud.client;

public class ClientActionManager {
    private static int maxTicks = 0;
    private static int currentTicks = 0;
    private static String actionText = "";
    private static boolean active = false;

    // TODO: В будущем добавить интеграцию с сервером. Сервер должен присылать пакет на старт/отмену действия
    // для всех механик: "Использование" (медикаменты, еда, бинты) и "Экипировка".
    public static void startAction(String text, int ticks) {
        actionText = text;
        maxTicks = ticks;
        currentTicks = 0;
        active = true;
    }

    public static void stopAction() {
        active = false;
    }

    public static void tick() {
        if (active) {
            currentTicks++;
            if (currentTicks >= maxTicks) {
                active = false;
            }
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static float getProgress() {
        return maxTicks == 0 ? 0 : (float) currentTicks / maxTicks;
    }

    public static String getActionText() {
        return actionText;
    }
}