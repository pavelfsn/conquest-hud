package com.conquest.hud.core.stats;

import com.conquest.hud.core.progression.IProgressionComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class StaminaComponent implements IStaminaComponent {
    private float stamina = 100f;
    private final PlayerEntity provider;

    public StaminaComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override public float getStamina() { return this.stamina; }

    @Override
    public float getMaxStamina() {
        IProgressionComponent progression = StatsComponentRegistry.PROGRESSION.getNullable(this.provider);
        int agility = progression != null ? progression.getStat(1) : 0;
        return 100.0f + (agility * 10.0f); // 100 - 200
    }

    @Override
    public void setStamina(float value) {
        float max = getMaxStamina();
        float clamped = Math.max(0, Math.min(value, max));
        if (Math.abs(this.stamina - clamped) > 0.001f) {
            int oldInt = (int) this.stamina;
            this.stamina = clamped;
            if (oldInt != (int) this.stamina) StatsComponentRegistry.STAMINA.sync(this.provider);
        }
    }

    public void setStaminaRaw(float value) {
        this.stamina = Math.max(0, Math.min(value, getMaxStamina()));
    }

    @Override public void readFromNbt(NbtCompound tag) { this.stamina = tag.getFloat("stamina"); }
    @Override public void writeToNbt(NbtCompound tag) { tag.putFloat("stamina", this.stamina); }
}