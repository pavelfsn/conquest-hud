package com.conquest.hud.core.stats;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class StaminaComponent implements IStaminaComponent {
    private float stamina = 100f;
    private final float maxStamina = 100f;
    private final PlayerEntity provider;

    public StaminaComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override public float getStamina() { return this.stamina; }
    @Override public float getMaxStamina() { return this.maxStamina; }

    @Override
    public void setStamina(float value) {
        this.stamina = Math.max(0, Math.min(value, this.maxStamina));
        StatsComponentRegistry.STAMINA.sync(this.provider);
    }

    public void setStaminaRaw(float value) {
        this.stamina = Math.max(0, Math.min(value, this.maxStamina));
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.stamina = tag.getFloat("stamina");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putFloat("stamina", this.stamina);
    }
}