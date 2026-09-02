package com.conquest.hud.core.container;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class PlayerContainersComponent implements IPlayerContainers, AutoSyncedComponent {
    private final PlayerEntity provider;
    private final ItemContainer inventory;
    private final ItemContainer equipment;
    private final ItemContainer hotbar; // Новый контейнер

    public PlayerContainersComponent(PlayerEntity provider) {
        this.provider = provider;
        this.inventory = new ItemContainer(0, 160, 0.0f);
        this.equipment = new PlayerEquipmentContainer(1, 14, 0.0f);
        this.hotbar = new ItemContainer(2, 9, 0.0f); // ID 2, 9 слотов
    }

    @Override public ItemContainer getInventory() { return inventory; }
    @Override public ItemContainer getEquipment() { return equipment; }
    @Override public ItemContainer getHotbar() { return hotbar; }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("Inventory")) inventory.readNbt(tag.getCompound("Inventory"));
        if (tag.contains("Equipment")) equipment.readNbt(tag.getCompound("Equipment"));
        if (tag.contains("Hotbar")) hotbar.readNbt(tag.getCompound("Hotbar"));
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtCompound invTag = new NbtCompound(); inventory.writeNbt(invTag); tag.put("Inventory", invTag);
        NbtCompound eqTag = new NbtCompound(); equipment.writeNbt(eqTag); tag.put("Equipment", eqTag);
        NbtCompound hbTag = new NbtCompound(); hotbar.writeNbt(hbTag); tag.put("Hotbar", hbTag);
    }
}