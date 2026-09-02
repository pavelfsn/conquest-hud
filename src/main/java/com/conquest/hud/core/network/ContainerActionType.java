package com.conquest.hud.core.network;

public enum ContainerActionType {
    MOVE,
    EQUIP,
    UNEQUIP,
    DROP,
    USE,
    DELETE,
    VANILLA_TO_CUSTOM, // Из ванильного сундука (курсора) в наш инвентарь
    CUSTOM_TO_VANILLA  // Из нашего инвентаря в ванильный курсор
}