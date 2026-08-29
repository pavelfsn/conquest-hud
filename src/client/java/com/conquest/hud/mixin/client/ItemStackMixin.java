package com.conquest.hud.mixin.client;

import com.conquest.hud.core.config.ItemWeightConfig;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void onGetTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isEmpty()) return;

        List<Text> customTooltip = new ArrayList<>();

        // 1. Название предмета (с сохранением цвета редкости)
        customTooltip.add(stack.getName());

        // 2. Количество
        customTooltip.add(Text.literal(stack.getCount() + " шт.").formatted(Formatting.GRAY));

        // 3. Вес стака
        float weight = ItemWeightConfig.getWeight(stack) * stack.getCount();
        customTooltip.add(Text.literal(String.format("%.1f кг", weight)).formatted(Formatting.YELLOW));

        // Принудительно возвращаем только наш список, отсекая ванильный мусор
        cir.setReturnValue(customTooltip);
    }
}