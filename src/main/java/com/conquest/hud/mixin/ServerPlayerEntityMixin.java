package com.conquest.hud.mixin;

import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void onRespawn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (!alive) { // Отрабатывает только при респавне после смерти
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(player);
            if (prog != null) {
                int metabolism = prog.getStat(2);
                EntityAttributeInstance hpAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (hpAttr != null) {
                    hpAttr.setBaseValue(200.0 + (metabolism * 10.0));
                }
                player.setHealth(player.getMaxHealth()); // Выдаем 200+ ХП
            }
        }
    }
}