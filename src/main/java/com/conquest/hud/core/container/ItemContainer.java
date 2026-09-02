package com.conquest.hud.core.container;

import com.conquest.hud.core.stats.WeightManager;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class ItemContainer implements IContainer {
    private final int containerId;
    private final DefaultedList<ItemStack> items;
    private final float weightLimit;

    public ItemContainer(int containerId, int size, float weightLimit) {
        this.containerId = containerId;
        this.items = DefaultedList.ofSize(size, ItemStack.EMPTY);
        this.weightLimit = weightLimit;
    }

    @Override
    public int getContainerId() {
        return this.containerId;
    }

    @Override
    public int getSize() {
        return this.items.size();
    }

    @Override
    public float getWeightLimit() {
        return this.weightLimit;
    }

    @Override
    public float getCurrentWeight() {
        float weight = 0.0f;
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                weight += WeightManager.getItemWeight(stack) * stack.getCount();
            }
        }
        return weight;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 0 && slot < this.items.size()) {
            return this.items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.items.size() && canInsert(slot, stack)) {
            this.items.set(slot, stack);
        }
    }

    // Сдвигает все предметы к началу, убирая промежутки
    public void compactAndSort() {
        List<ItemStack> compacted = new ArrayList<>();
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                compacted.add(stack);
            }
        }
        for (int i = 0; i < this.items.size(); i++) {
            if (i < compacted.size()) {
                this.items.set(i, compacted.get(i));
            } else {
                this.items.set(i, ItemStack.EMPTY);
            }
        }
    }

    public void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, this.items);
    }

    public void readNbt(NbtCompound nbt) {
        this.items.clear();
        Inventories.readNbt(nbt, this.items);
    }
}