package com.conquest.hud.core.stats;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class PlayerWeightComponent implements IPlayerWeightComponent {
    private final PlayerEntity player;
    private float currentWeight = 0.0f;

    public PlayerWeightComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public float getCurrentWeight() {
        return this.currentWeight;
    }

    @Override
    public void setCurrentWeight(float weight) {
        this.currentWeight = weight;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.currentWeight = tag.getFloat("CurrentWeight");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putFloat("CurrentWeight", this.currentWeight);
    }
}