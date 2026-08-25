package com.conquest.hud.core.stats;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class PlayerStatsComponent implements IPlayerStats {
    private int strength = 0;
    private int agility = 0;
    private int vitality = 0;
    private int metabolism = 0;
    private int intellect = 0;
    private final PlayerEntity provider;

    public PlayerStatsComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override public int getStrength() { return strength; }
    @Override public int getAgility() { return agility; }
    @Override public int getVitality() { return vitality; }
    @Override public int getMetabolism() { return metabolism; }
    @Override public int getIntellect() { return intellect; }

    @Override public void setStrength(int value) { this.strength = value; sync(); }
    @Override public void setAgility(int value) { this.agility = value; sync(); }
    @Override public void setVitality(int value) { this.vitality = value; sync(); }
    @Override public void setMetabolism(int value) { this.metabolism = value; sync(); }
    @Override public void setIntellect(int value) { this.intellect = value; sync(); }

    private void sync() {
        StatsComponentRegistry.PLAYER_STATS.sync(this.provider);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.strength = tag.getInt("strength");
        this.agility = tag.getInt("agility");
        this.vitality = tag.getInt("vitality");
        this.metabolism = tag.getInt("metabolism");
        this.intellect = tag.getInt("intellect");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("strength", this.strength);
        tag.putInt("agility", this.agility);
        tag.putInt("vitality", this.vitality);
        tag.putInt("metabolism", this.metabolism);
        tag.putInt("intellect", this.intellect);
    }
}