package com.conquest.hud.client.gui;

import com.conquest.hud.client.ConquestKeybinds;
import com.conquest.hud.client.network.ClientPacketSender;
import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.EquipmentSlotRegistry;
import com.conquest.hud.core.container.IContainer;
import com.conquest.hud.core.container.IPlayerContainers;
import com.conquest.hud.core.network.ContainerActionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ConquestInventoryScreen extends Screen {
    private final List<ModularWindow> windows = new ArrayList<>();
    private final List<ModularWindow> pendingWindows = new ArrayList<>();
    private final ContextMenuWidget contextMenu = new ContextMenuWidget();

    private ItemStack cursorStack = ItemStack.EMPTY;
    private int cursorSourceContainer = -1;
    private int cursorSourceSlot = -1;

    private int currentMouseX = 0;
    private int currentMouseY = 0;

    private final ModularWindow renderHelper = new ModularWindow("helper", 0, 0, 0, 0, false);

    public ConquestInventoryScreen() {
        super(Text.literal("Conquest UI"));
    }

    @Override
    protected void init() {
        windows.clear();
        pendingWindows.clear();
        if (client == null || client.player == null) return;
        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(client.player);

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        String playerName = client.player.getName().getString();

        ContainerWindow invWindow = new ContainerWindow("Инвентарь", sw - 380, (sh - 460) / 2, 350, 430, containers.getInventory(), 10, false);
        windows.add(invWindow);

        EquipmentWindow eqWindow = new EquipmentWindow(playerName + " [U]", (sw - 360) / 2, (sh - 417) / 2, containers.getEquipment(), false);
        windows.add(eqWindow);

        ProgressionWindow progWindow = new ProgressionWindow("Прокачка персонажа", (sw - 600) / 2, (sh - 350) / 2, false);
        windows.add(progWindow);

        BuffsWindow buffsWindow = new BuffsWindow("Бафы", sw - 160, 10);
        windows.add(buffsWindow);

        StatsHudWindow statsW = new StatsHudWindow("HUD_Статусы", 20, 20);
        windows.add(statsW);
    }

    @Override
    public void renderBackground(DrawContext context) {}

    public void setInitialVisibility(boolean showInv, boolean showEq) {
        setInitialVisibility(showInv, showEq, false);
    }

    public void setInitialVisibility(boolean showInv, boolean showEq, boolean showProg) {
        ModularWindow invW = getWindow("Инвентарь");
        if (invW != null) invW.setVisible(showInv);
        ModularWindow eqW = getWindow(client.player.getName().getString() + " [U]");
        if (eqW != null) eqW.setVisible(showEq);
        ModularWindow progW = getWindow("Прокачка персонажа");
        if (progW != null) progW.setVisible(showProg);
        ModularWindow buffsW = getWindow("Бафы");
        if (buffsW != null) buffsW.setVisible(true);
    }

    private ModularWindow getWindow(String title) {
        for (ModularWindow w : windows) {
            if (w.getTitle().equals(title)) return w;
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;

        if (!pendingWindows.isEmpty()) {
            windows.addAll(pendingWindows);
            pendingWindows.clear();
        }

        windows.removeIf(w -> w.isClosed && !w.getTitle().equals("Инвентарь") && !w.getTitle().contains("[U]") && !w.getTitle().equals("Бафы") && !w.getTitle().equals("Прокачка персонажа"));

        int zOffset = 0;
        for (ModularWindow window : windows) {
            if (window instanceof ContainerWindow cw) cw.setCursorInfo(cursorStack, cursorSourceContainer, cursorSourceSlot);
            else if (window instanceof EquipmentWindow ew) ew.setCursorInfo(cursorStack, cursorSourceContainer, cursorSourceSlot);

            // 1. Отрисовка фона через NanoVG
            com.conquest.hud.client.render.nanovg.NanoVGManager.setupAndDraw(() -> window.renderNanoVGBackground(mouseX, mouseY));

            // 2. Отрисовка контента окна
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, zOffset);
            if (window instanceof ContainerWindow cw) cw.renderForeground(context, mouseX, mouseY, delta);
            else window.renderForeground(context, mouseX, mouseY, delta);
            context.getMatrices().pop();

            // КРИТИЧЕСКИЙ ФИКС Z-Index: Очищаем буфер глубины после каждого окна.
            // Теперь 3D-сущности (броня игрока) не будут протыкать окна, отрисованные поверх них.
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

            zOffset += 300;
        }

        renderInteractiveHotbar(context, mouseX, mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 1500);
        contextMenu.render(context, mouseX, mouseY);
        context.getMatrices().pop();

        if (!cursorStack.isEmpty() && client != null) {
            context.getMatrices().push();
            context.getMatrices().translate(mouseX - 8, mouseY - 8, 2000);
            context.getMatrices().scale(1.5f, 1.5f, 1.0f);
            context.drawItem(cursorStack, 0, 0);
            renderHelper.drawCustomItemOverlay(context, client.textRenderer, cursorStack, false);
            context.getMatrices().pop();
        } else if (client != null && client.player != null && !contextMenu.isVisible()) {
            drawTooltips(context, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderInteractiveHotbar(DrawContext context, int mouseX, int mouseY) {
        if (client == null || client.player == null) return;
        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(client.player);
        IContainer hotbar = containers.getHotbar();
        int slotSize = 32; int gap = 2; int totalWidth = (9 * slotSize) + (8 * gap);
        int startX = this.width - totalWidth - 15; int startY = this.height - slotSize - 15;

        for (int i = 0; i < 9; i++) {
            int x = startX + i * (slotSize + gap);
            context.fill(x, startY, x + slotSize, startY + slotSize, 0x99000000);
            context.drawBorder(x, startY, slotSize, slotSize, 0xFF555555);

            ItemStack stack = hotbar.getStack(i);
            boolean isCursorSource = (cursorSourceContainer == 2 && cursorSourceSlot == i && !cursorStack.isEmpty());
            ItemStack stackToDraw = isCursorSource ? cursorStack : stack;

            if (!stackToDraw.isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(x + 4, startY + 4, 0);
                context.getMatrices().scale(1.5f, 1.5f, 1.0f);

                if (isCursorSource) RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);

                context.drawItem(stackToDraw, 0, 0);
                renderHelper.drawCustomItemOverlay(context, client.textRenderer, stackToDraw, false);

                if (isCursorSource) RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                context.getMatrices().pop();
            }

            String keyName = ConquestKeybinds.actionKeys[i].getBoundKeyLocalizedText().getString();
            context.getMatrices().push();
            context.getMatrices().translate(x + 2, startY + slotSize - 10, 250);
            if (client.textRenderer.getWidth(keyName) > 12) context.getMatrices().scale(0.6f, 0.6f, 1.0f);
            context.drawTextWithShadow(client.textRenderer, keyName, 0, 0, 0xFFFFFF);
            context.getMatrices().pop();

            if (mouseX >= x && mouseX < x + slotSize && mouseY >= startY && mouseY < startY + slotSize) {
                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 200);
                context.fill(x, startY, x + slotSize, startY + slotSize, 0x44FFFFFF);
                context.getMatrices().pop();
            }
        }
    }

    private void drawTooltips(DrawContext context, int mouseX, int mouseY) {
        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(client.player);
        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int i = windows.size() - 1; i >= 0; i--) {
            ModularWindow window = windows.get(i);
            int slot = -1;
            if (window instanceof ContainerWindow cw) slot = cw.getHoveredSlot(mouseX, mouseY);
            else if (window instanceof EquipmentWindow ew) slot = ew.getHoveredSlot(mouseX, mouseY);

            if (slot != -1) {
                IContainer container = (window instanceof ContainerWindow cw) ? cw.getContainer() : ((EquipmentWindow) window).getContainer();
                hoveredStack = container.getStack(slot);
                break;
            }
        }

        if (hoveredStack.isEmpty()) {
            int slotSize = 32; int gap = 2; int totalWidth = (9 * slotSize) + (8 * gap);
            int startX = this.width - totalWidth - 15; int startY = this.height - slotSize - 15;
            if (mouseY >= startY && mouseY < startY + slotSize) {
                for (int i = 0; i < 9; i++) {
                    int x = startX + i * (slotSize + gap);
                    if (mouseX >= x && mouseX < x + slotSize) {
                        hoveredStack = containers.getHotbar().getStack(i);
                        break;
                    }
                }
            }
        }

        if (!hoveredStack.isEmpty()) {
            List<net.minecraft.text.Text> lines = hoveredStack.getTooltip(client.player, client.options.advancedItemTooltips ? net.minecraft.client.item.TooltipContext.Default.ADVANCED : net.minecraft.client.item.TooltipContext.Default.BASIC);
            ModularWindow.drawConquestTooltip(context, client.textRenderer, lines, mouseX, mouseY, this.width, this.height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (contextMenu.isVisible()) {
            if (contextMenu.mouseClicked(mouseX, mouseY, button)) return true;
            contextMenu.close();
        }

        boolean clickedOnWindow = false;

        // Фикс перетаскивания: цикл идет с конца (от самого верхнего окна к нижнему)
        for (int i = windows.size() - 1; i >= 0; i--) {
            ModularWindow window = windows.get(i);

            // Обработка удаления предмета (корзина) в инвентаре
            if (window instanceof ContainerWindow cw && cw.deleteMode) {
                if (cw.isHoveredTrash(mouseX, mouseY)) {
                    cw.deleteMode = false;
                    return true;
                }
                int visualSlot = cw.getHoveredVisualSlot(mouseX, mouseY);
                if (visualSlot != -1) {
                    int realSlot = cw.getRealSlot(visualSlot);
                    ItemStack stackToDel = cw.getContainer().getStack(realSlot);
                    if (!stackToDel.isEmpty()) {
                        String msg = String.format("Вы точно хотите удалить предмет:\n[%s] x%d?", stackToDel.getName().getString(), stackToDel.getCount());
                        pendingWindows.add(new ConfirmWindow(msg, (int)mouseX - 110, (int)mouseY - 40, () -> {
                            ClientPacketSender.sendAction(ContainerActionType.DELETE, cw.getContainer().getContainerId(), realSlot, -1, -1);
                        }, null));
                        cw.deleteMode = false;
                        return true;
                    }
                }
            }

            int slot = -1;
            IContainer container = null;
            if (window instanceof ContainerWindow cw) {
                slot = cw.getHoveredSlot(mouseX, mouseY);
                container = cw.getContainer();
            } else if (window instanceof EquipmentWindow ew) {
                slot = ew.getHoveredSlot(mouseX, mouseY);
                container = ew.getContainer();
            }

            if (slot != -1 && container != null) {
                windows.remove(i);
                windows.add(window); // Выводим на передний план
                handleAction(container, slot, button, (int) mouseX, (int) mouseY);
                return true;
            }

            // Обрабатываем клики самого окна (перетаскивание, кнопки)
            if (window.mouseClicked(mouseX, mouseY, button)) {
                clickedOnWindow = true;
                windows.remove(i);
                windows.add(window); // Выводим на передний план
                break; // Не прокликиваем окна сквозь друг друга
            }
        }

        // Логика курсора и хотбара, если не попали в окно
        int slotSize = 32; int gap = 2; int totalWidth = (9 * slotSize) + (8 * gap);
        int startX = this.width - totalWidth - 15; int startY = this.height - slotSize - 15;

        if (!clickedOnWindow && mouseY >= startY && mouseY < startY + slotSize) {
            for (int i = 0; i < 9; i++) {
                int x = startX + i * (slotSize + gap);
                if (mouseX >= x && mouseX < x + slotSize) {
                    IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(client.player);
                    handleAction(containers.getHotbar(), i, button, (int)mouseX, (int)mouseY);
                    return true;
                }
            }
        }

        if (!clickedOnWindow && button == 0 && !cursorStack.isEmpty()) {
            final int fSrcCont = cursorSourceContainer;
            final int fSrcSlot = cursorSourceSlot;
            pendingWindows.add(new ConfirmWindow("Вы точно хотите выбросить предмет?", (int)mouseX - 110, (int)mouseY - 40, () -> {
                ClientPacketSender.sendAction(ContainerActionType.DROP, fSrcCont, fSrcSlot, -1, -1);
                cursorStack = ItemStack.EMPTY;
                cursorSourceContainer = -1;
                cursorSourceSlot = -1;
            }, () -> {
                restoreCursorStackLocally();
                cursorStack = ItemStack.EMPTY;
                cursorSourceContainer = -1;
                cursorSourceSlot = -1;
            }));
            return true;
        }

        return clickedOnWindow || super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleAction(IContainer currentContainer, int slot, int button, int mouseX, int mouseY) {
        if (currentContainer == null) return;
        ItemStack stack = currentContainer.getStack(slot);
        int containerId = currentContainer.getContainerId();

        if (button == 0) {
            if (cursorStack.isEmpty()) {
                if (!stack.isEmpty()) {
                    cursorStack = stack.copy();
                    cursorSourceContainer = containerId;
                    cursorSourceSlot = slot;
                    currentContainer.setStack(slot, ItemStack.EMPTY);
                }
            } else {
                if (containerId == 1) {
                    if (!EquipmentSlotRegistry.canEquip(cursorStack, slot)) {
                        restoreCursorStackLocally();
                        cursorStack = ItemStack.EMPTY;
                        cursorSourceContainer = -1;
                        cursorSourceSlot = -1;
                        return;
                    }
                }
                restoreCursorStackLocally();
                ClientPacketSender.sendAction(ContainerActionType.MOVE, cursorSourceContainer, cursorSourceSlot, containerId, slot);
                cursorStack = ItemStack.EMPTY;
                cursorSourceContainer = -1;
                cursorSourceSlot = -1;
            }
        } else if (button == 1 && !stack.isEmpty() && cursorStack.isEmpty()) {
            List<ContextMenuWidget.ActionOption> options = new ArrayList<>();

            if (stack.isFood()) {
                options.add(new ContextMenuWidget.ActionOption("Использовать", () ->
                        ClientPacketSender.sendAction(ContainerActionType.USE, containerId, slot, -1, -1)
                ));
            }

            if (!stack.isFood() && (containerId == 0 || containerId == 2)) {
                options.add(new ContextMenuWidget.ActionOption("Экипировать", () ->
                        ClientPacketSender.sendAction(ContainerActionType.EQUIP, containerId, slot, -1, -1)
                ));
            }
            if (containerId == 1) {
                options.add(new ContextMenuWidget.ActionOption("Снять", () ->
                        ClientPacketSender.sendAction(ContainerActionType.UNEQUIP, containerId, slot, -1, -1)
                ));
            }

            options.add(new ContextMenuWidget.ActionOption("Информация", () -> {
                ItemInfoWindow infoW = new ItemInfoWindow("Информация", currentMouseX, currentMouseY);
                infoW.showItem(stack);

                pendingWindows.removeIf(w -> w.getTitle().equals("Информация"));
                windows.removeIf(w -> w.getTitle().equals("Информация"));

                pendingWindows.add(infoW);
            }));

            options.add(new ContextMenuWidget.ActionOption("Выбросить", () ->
                    pendingWindows.add(new ConfirmWindow("Вы точно хотите выбросить предмет?", currentMouseX - 110, currentMouseY - 40, () -> {
                        ClientPacketSender.sendAction(ContainerActionType.DROP, containerId, slot, -1, -1);
                    }, null))
            ));

            options.add(new ContextMenuWidget.ActionOption("Удалить", () -> {
                String msg = String.format("Вы точно хотите удалить предмет:\n[%s] x%d?", stack.getName().getString(), stack.getCount());
                pendingWindows.add(new ConfirmWindow(msg, currentMouseX - 110, currentMouseY - 40, () -> {
                    ClientPacketSender.sendAction(ContainerActionType.DELETE, containerId, slot, -1, -1);
                }, null));
            }));

            contextMenu.open(mouseX, mouseY, options);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ConquestKeybinds.openInvKey.matchesKey(keyCode, scanCode)) { toggleWindow("Инвентарь"); return true; }
        if (ConquestKeybinds.openEqKey.matchesKey(keyCode, scanCode)) { toggleWindow(client.player.getName().getString() + " [U]"); return true; }
        if (ConquestKeybinds.freeCursorKey.matchesKey(keyCode, scanCode)) { this.close(); return true; }
        if (ConquestKeybinds.openProgressionKey.matchesKey(keyCode, scanCode)) { toggleWindow("Прокачка персонажа"); return true; }

        if (keyCode == GLFW.GLFW_KEY_G) {
            for (ModularWindow window : windows) {
                int slot = -1;
                if (window instanceof ContainerWindow cw) slot = cw.getHoveredSlot(currentMouseX, currentMouseY);
                else if (window instanceof EquipmentWindow ew) slot = ew.getHoveredSlot(currentMouseX, currentMouseY);

                if (slot != -1) {
                    IContainer container = (window instanceof ContainerWindow cw) ? cw.getContainer() : ((EquipmentWindow) window).getContainer();
                    ItemStack stackToDrop = container.getStack(slot);
                    if (!stackToDrop.isEmpty()) {
                        final int finalSlot = slot;
                        final int finalContainerId = container.getContainerId();
                        String msg = String.format("Вы точно хотите выбросить предмет:\n[%s] x%d?", stackToDrop.getName().getString(), stackToDrop.getCount());

                        pendingWindows.add(new ConfirmWindow(msg, currentMouseX - 110, currentMouseY - 40, () -> {
                            ClientPacketSender.sendAction(ContainerActionType.DROP, finalContainerId, finalSlot, -1, -1);
                        }, null));
                        return true;
                    }
                }
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { this.close(); return true; }
        handleMovementKey(keyCode, scanCode, true);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        handleMovementKey(keyCode, scanCode, false);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void handleMovementKey(int keyCode, int scanCode, boolean pressed) {
        if (client == null) return;
        GameOptions options = client.options;
        KeyBinding[] movementKeys = { options.forwardKey, options.backKey, options.leftKey, options.rightKey, options.jumpKey, options.sprintKey, options.sneakKey };
        for (KeyBinding key : movementKeys) if (key.matchesKey(keyCode, scanCode)) key.setPressed(pressed);
    }

    private void toggleWindow(String title) {
        ModularWindow w = getWindow(title);
        if (w != null) {
            w.toggle();
            if (w.isVisible()) {
                windows.remove(w);
                windows.add(w);
            }
        }
        boolean anyVisible = false;
        for (ModularWindow win : windows) if (win.isVisible()) anyVisible = true;
        if (!anyVisible) this.close();
    }

    private void restoreCursorStackLocally() {
        if (cursorSourceContainer == 2) {
            ContainerComponentRegistry.CONTAINERS.get(client.player).getHotbar().setStack(cursorSourceSlot, cursorStack);
        } else {
            for (ModularWindow w : windows) {
                if (w instanceof ContainerWindow cw && cw.getContainer().getContainerId() == cursorSourceContainer) {
                    cw.getContainer().setStack(cursorSourceSlot, cursorStack);
                    break;
                } else if (w instanceof EquipmentWindow ew && ew.getContainer().getContainerId() == cursorSourceContainer) {
                    ew.getContainer().setStack(cursorSourceSlot, cursorStack);
                    break;
                }
            }
        }
    }

    @Override
    public void close() {
        if (!cursorStack.isEmpty()) restoreCursorStackLocally();
        if (client != null) KeyBinding.updateKeysByCode();
        super.close();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (windows.get(i).mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }
}