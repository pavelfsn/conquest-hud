package com.conquest.hud.core.item;

/**
 * Система редкости предметов в стиле STALZONE + Tarkov
 * Цветовая кодировка + визуальные эффекты
 */
public enum ItemRarity {
    COMMON(0x9D9D9D, "Обычное", 1.0f, 0),      // Серый
    UNCOMMON(0x55FF55, "Необычное", 1.15f, 1),   // Зеленый
    RARE(0x5555FF, "Редкое", 1.35f, 2),         // Синий
    EPIC(0xAA55FF, "Эпическое", 1.6f, 3),       // Фиолетовый
    LEGENDARY(0xFFAA00, "Легендарное", 2.0f, 4), // Оранжевый
    ARTIFACT(0xFF00AA, "Артефакт", 2.5f, 5);    // Розовый (уникально для сталкера)

    private final int color;
    private final String name;
    private final float valueMultiplier;
    private final int tier;

    ItemRarity(int color, String name, float valueMultiplier, int tier) {
        this.color = color;
        this.name = name;
        this.valueMultiplier = valueMultiplier;
        this.tier = tier;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public float getValueMultiplier() {
        return valueMultiplier;
    }

    public int getTier() {
        return tier;
    }

    /**
     * Получить редкость из строкового названия
     */
    public static ItemRarity fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }

    /**
     * Получить редкость по тиру
     */
    public static ItemRarity fromTier(int tier) {
        if (tier < 0) return COMMON;
        if (tier >= values().length) return values()[values().length - 1];
        return values()[tier];
    }

    /**
     * Получить цвет в формате ARGB для рендеринга
     */
    public int getARGBColor() {
        // Конвертируем RGB в ARGB с полной альфой
        return 0xFF000000 | (color & 0xFFFFFF);
    }

    /**
     * Получить цвет рамки слота
     */
    public int getBorderColor() {
        return 0xFF000000 | (color & 0xFFFFFF);
    }

    /**
     * Получить эффект свечения (сила от 0.0 до 1.0)
     */
    public float getGlowStrength() {
        switch (this) {
            case LEGENDARY:
            case ARTIFACT:
                return 0.8f;
            case EPIC:
                return 0.5f;
            case RARE:
                return 0.3f;
            default:
                return 0.0f;
        }
    }

    /**
     * Есть ли у предмета свечение
     */
    public boolean hasGlow() {
        return this == LEGENDARY || this == ARTIFACT || this == EPIC;
    }
}
