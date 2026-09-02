package com.conquest.hud.core.container;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface IContainer {
    // Уникальный ID контейнера для сетевой синхронизации (0 - основной инвентарь, 1 - экипировка и т.д.)
    int getContainerId();

    // Общее количество слотов
    int getSize();

    // Лимит веса (для рюкзаков), 0 - если лимита нет
    float getWeightLimit();

    // Текущий вес всех предметов внутри
    float getCurrentWeight();

    // Список предметов
    DefaultedList<ItemStack> getItems();

    // Валидация: можно ли положить предмет в этот слот (проверка тегов)
    boolean canInsert(int slot, ItemStack stack);

    // Получить предмет из слота
    ItemStack getStack(int slot);

    // Установить предмет в слот
    void setStack(int slot, ItemStack stack);
}