package com.conquest.hud.mixin.client.pointblank;

import com.conquest.hud.client.render.nanovg.NanoVGManager;
import com.conquest.hud.mixin.SlotAccessor;
import com.vicmatskiv.pointblank.client.gui.AttachmentManagerScreen;
import com.vicmatskiv.pointblank.client.gui.MouseInteractionHandler;
import com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu;
import com.vicmatskiv.pointblank.inventory.SimpleAttachmentContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AttachmentManagerScreen.class, remap = false)
public abstract class AttachmentScreenMixin extends HandledScreen<AttachmentContainerMenu> {

    @Shadow private MouseInteractionHandler mouseInteractionHandler;

    public AttachmentScreenMixin(AttachmentContainerMenu handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void onInitCustom(CallbackInfo ci) {
        this.x = 0;
        this.y = 0;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Скрываем ванильный инвентарь игрока (убираем за экран), но данные сохраняем
        for (Slot slot : this.handler.slots) {
            if (slot.inventory instanceof PlayerInventory) {
                ((SlotAccessor) slot).setX(-9999);
                ((SlotAccessor) slot).setY(-9999);
            }
        }

        // Красиво расставляем слоты обвесов по бокам от 3D-оружия в стиле Tarkov
        SimpleAttachmentContainer[] containers = this.handler.getAttachmentContainers();
        for (int i = 0; i < containers.length; i++) {
            SimpleAttachmentContainer container = containers[i];
            if (container == null) continue;

            int startSlotIndex = SimpleAttachmentContainer.getContainerStartIndex(containers, i);
            int colX, startY = centerY - 120;

            if (i < 5) {
                colX = centerX - 280 + (i * 26);
            } else {
                colX = centerX + 160 + ((i - 5) * 26);
            }

            Slot headerSlot = this.handler.slots.get(startSlotIndex);
            ((SlotAccessor) headerSlot).setX(colX);
            ((SlotAccessor) headerSlot).setY(startY);

            for (int j = 1; j < container.size(); j++) {
                Slot attSlot = this.handler.slots.get(startSlotIndex + j);
                ((SlotAccessor) attSlot).setX(colX);
                ((SlotAccessor) attSlot).setY(startY + (j * 20));
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = true)
    private void onRenderCustom(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.renderBackground(context);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Рисуем наш фирменный темный фон в стиле Таркова на весь экран
        NanoVGManager.setupAndDraw(() -> {
            NanoVGManager.drawRoundedRect(0, 0, this.width, this.height, 0.0f, 0xEE0B0A09);

            // Рамка вокруг центральной зоны модели
            NanoVGManager.drawRoundedRect(centerX - 160, centerY - 130, 320, 260, 8.0f, 0xFF141312);
            NanoVGManager.drawRoundedRectStroke(centerX - 160, centerY - 130, 320, 260, 8.0f, 1.0f, 0xFF333230);
        });

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 300);
        context.drawTextWithShadow(this.textRenderer, "МОДИФИКАЦИЯ ОРУЖИЯ", 20, 20, 0xFFD700);
        context.drawTextWithShadow(this.textRenderer, "Нажмите ESC для выхода", 20, 35, 0x888888);
        context.getMatrices().pop();

        // Отрисовка слотов обвесов с нашими рамками
        SimpleAttachmentContainer[] containers = this.handler.getAttachmentContainers();
        for (int i = 0; i < containers.length; i++) {
            SimpleAttachmentContainer container = containers[i];
            if (container == null) continue;
            int startSlotIndex = SimpleAttachmentContainer.getContainerStartIndex(containers, i);

            Slot headerSlot = this.handler.slots.get(startSlotIndex);
            if (headerSlot.isEnabled()) {
                context.fill(headerSlot.x, headerSlot.y, headerSlot.x + 18, headerSlot.y + 18, 0x88000000);
                context.drawBorder(headerSlot.x, headerSlot.y, 18, 18, 0xFF555555);
            }

            for (int j = 1; j < container.size(); j++) {
                Slot attSlot = this.handler.slots.get(startSlotIndex + j);
                if (attSlot.isEnabled()) {
                    context.fill(attSlot.x, attSlot.y, attSlot.x + 18, attSlot.y + 18, 0x88000000);
                    context.drawBorder(attSlot.x, attSlot.y, 18, 18, 0xFF444444);
                }
            }
        }

        // Рендерим только слоты, отсекая ванильный дубликат иконки
        for (Slot slot : this.handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory) && slot.isEnabled()) {
                if (!slot.hasStack()) {
                    // Рисуем пустой слот
                    context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x44000000);
                }
            }
        }

        // Вызываем Foreground для прорисовки предметов в слотах
        super.render(context, mouseX, mouseY, delta);

        // Рендерим крупную 3D-модель оружия по центру без пиксельных дубликатов
        this.renderItemInHandCustom(context, mouseX, mouseY);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        ci.cancel();
    }

    private void renderItemInHandCustom(DrawContext guiGraphics, int mouseX, int mouseY) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        MatrixStack poseStack = guiGraphics.getMatrices();
        poseStack.push();

        poseStack.translate(this.width / 2.0f, this.height / 2.0f, 250.0F);

        float interactionOffsetX = (float)this.mouseInteractionHandler.getX() + (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isTranslating() ? (float)mouseX - this.mouseInteractionHandler.getMouseClickedX() : 0.0F);
        float interactionOffsetY = (float)this.mouseInteractionHandler.getY() + (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isTranslating() ? (float)mouseY - this.mouseInteractionHandler.getMouseClickedY() : 0.0F);
        poseStack.translate(interactionOffsetX, interactionOffsetY, 0.0F);

        float interactionPitch = this.mouseInteractionHandler.getRotationPitch() - (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isRotating() ? (float)mouseY - this.mouseInteractionHandler.getMouseClickedY() : 0.0F) - 30.0F;
        float interactionYaw = this.mouseInteractionHandler.getRotationYaw() + (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isRotating() ? (float)mouseX - this.mouseInteractionHandler.getMouseClickedX() : 0.0F) + 150.0F;
        poseStack.multiply((new Quaternionf()).rotationXYZ(interactionPitch * ((float)Math.PI / 180F), interactionYaw * ((float)Math.PI / 180F), 0.0F));

        // Увеличиваем модель, чтобы она была крупной и детализированной
        float zoom = this.mouseInteractionHandler.getZoom() * 3.5f;
        poseStack.scale(zoom, zoom, zoom);
        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(100.0F, 100.0F, 100.0F);

        com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
        VertexConsumerProvider.Immediate buffer = minecraft.getBufferBuilders().getEntityVertexConsumers();
        ItemStack itemStack = minecraft.player.getMainHandStack();

        BakedModel model = minecraft.getItemRenderer().getModel(itemStack, minecraft.world, minecraft.player, minecraft.player.getId());
        minecraft.getItemRenderer().renderItem(itemStack, ModelTransformationMode.GUI, false, poseStack, buffer, 15728880, OverlayTexture.DEFAULT_UV, model);
        buffer.draw();

        poseStack.pop();
        com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
    }
}