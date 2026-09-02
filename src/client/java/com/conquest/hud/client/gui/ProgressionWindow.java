package com.conquest.hud.client.gui;

import com.conquest.hud.client.render.nanovg.NanoVGManager;
import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ProgressionWindow extends ModularWindow {

    private final int cardWidth = 125;
    private final int cardHeight = 85;
    private final int gapX = 8;
    private final int gapY = 8;
    private final int pad = 14;

    private final long[] upgradeAnim = new long[8];

    private static final Identifier[] STAT_ICONS = {
            new Identifier("minecraft", "textures/mob_effect/strength.png"),
            new Identifier("minecraft", "textures/mob_effect/speed.png"),
            new Identifier("minecraft", "textures/mob_effect/regeneration.png"),
            new Identifier("minecraft", "textures/mob_effect/luck.png"),
            new Identifier("minecraft", "textures/mob_effect/haste.png")
    };

    private static final String[] STAT_NAMES = {"Сила", "Ловкость", "Метаболизм", "Удача", "Восприятие"};
    private static final String[] WEAPON_NAMES = {"Доставание", "Перезарядка", "Контроль"};

    private List<Text> tooltipToDraw = null;

    public ProgressionWindow(String title, int defaultX, int defaultY, boolean defaultVisible) {
        super(title, defaultX, defaultY, 560, 425, defaultVisible); // Уменьшена высота
    }

    @Override
    public void renderNanoVGBackground(int mouseX, int mouseY) {
        if (!isVisible()) return;
        if (isDragging) {
            this.x = mouseX - dragOffsetX;
            this.y = mouseY - dragOffsetY;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        this.x = Math.max(0, Math.min(this.x, client.getWindow().getScaledWidth() - this.width));
        this.y = Math.max(0, Math.min(this.y, client.getWindow().getScaledHeight() - 20));

        NanoVGManager.drawRoundedRect(x, y, width, height, 0.0f, 0xF20B0A09);
        NanoVGManager.drawRoundedRect(x, y, width, 18, 0.0f, 0xF2242424);
        NanoVGManager.drawRoundedRectStroke(x, y, width, height, 0.0f, 1.0f, 0xFF464646);
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderForeground(context, mouseX, mouseY, delta);
        if (!isVisible()) return;

        tooltipToDraw = null;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(client.player);
        if (prog == null) return;

        TextRenderer tr = client.textRenderer;

        int headerY = y + pad + 18;
        int rightEdge = x + width - pad;

        int respecW = 24;
        int pointsW = 80;

        int respecX = rightEdge - respecW;
        int pointsX = respecX - gapX - pointsW;

        int barX = x + pad;
        int barW = pointsX - gapX - barX; // Прогресс-бар упирается прямо перед кнопкой ОЧКИ
        int barY = headerY + 14;

        context.fill(pointsX, headerY - 4, pointsX + pointsW, headerY + 10, 0xFF484848);
        context.drawTextWithShadow(tr, "ОЧКИ: " + prog.getAvailablePoints(), pointsX + 8, headerY - 1, 0xFFFFFF);

        boolean canRespec = (System.currentTimeMillis() - prog.getLastRespecTime() >= 86_400_000L);
        context.fill(respecX, headerY - 4, respecX + respecW, headerY + 10, canRespec ? 0xFF484848 : 0xFF222222);
        context.drawTextWithShadow(tr, "R", respecX + 8, headerY - 1, canRespec ? 0xFFFFFF : 0xFF555555);

        context.drawTextWithShadow(tr, "УРОВЕНЬ", barX, headerY, 0xAAAAAA);
        context.getMatrices().push();
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        context.drawTextWithShadow(tr, String.valueOf(prog.getLevel()), (int) ((barX + 55) / 1.5f), (int) ((headerY - 4) / 1.5f), 0xF89000);
        context.getMatrices().pop();

        long currentXp = prog.getXp();
        long nextLvlXp = prog.getNextLevelXp();
        long currentLvlBase = prog.getCurrentLevelBaseXp();

        String xpText = String.format("%,d / %,d XP", currentXp, nextLvlXp).replace(',', '\'');
        int xpTextWidth = tr.getWidth(xpText);
        int xpTextX = barX + barW - xpTextWidth;
        context.drawTextWithShadow(tr, xpText, xpTextX, headerY, 0xAAAAAA);

        context.fill(barX, barY, barX + barW, barY + 4, 0xFF333333);
        if (nextLvlXp > currentLvlBase) {
            float progress = (float)(currentXp - currentLvlBase) / (nextLvlXp - currentLvlBase);
            progress = Math.max(0.0f, Math.min(1.0f, progress));
            if (progress > 0) {
                context.fill(barX, barY, barX + (int)(barW * progress), barY + 4, 0xFFF89000);
            }
        }

        int gridStartX = x + pad;
        int gridStartY = barY + 16;

        for (int i = 0; i < 8; i++) {
            int col = i % 4;
            int row = i / 4;
            int cardX = gridStartX + col * (cardWidth + gapX);
            int cardY = gridStartY + row * (cardHeight + gapY);
            drawCard(context, tr, cardX, cardY, mouseX, mouseY, i, prog);
        }

        int statY = gridStartY + 2 * (cardHeight + gapY) + 6;
        context.drawTextWithShadow(tr, "ХАРАКТЕРИСТИКИ", x + pad, statY, 0xAAAAAA);
        context.fill(x + pad, statY + 10, rightEdge, statY + 11, 0xFF444444);

        int col1 = x + pad;
        int col2 = x + pad + 270;
        int step = 15;
        int sY = statY + 16;

        float hpBonus = prog.getStat(2) * 10.0f;
        float hpRegen = prog.getStat(2) * 0.4f;
        float stamBonus = prog.getStat(1) * 10.0f;
        float stamRegen = prog.getStat(1) * 1.5f;

        drawStatBlock(context, tr, "Здоровье", 200f, hpBonus, col1, sY, mouseX, mouseY, "Метаболизм " + prog.getStat(2) + " ур.");
        drawStatBlock(context, tr, "Реген. здоровья", 0f, hpRegen, col1, sY + step, mouseX, mouseY, "Метаболизм " + prog.getStat(2) + " ур.");
        drawStatBlock(context, tr, "Выносливость", 100f, stamBonus, col1, sY + step*2, mouseX, mouseY, "Ловкость " + prog.getStat(1) + " ур.");
        drawStatBlock(context, tr, "Реген. стамины", 15f, stamRegen, col1, sY + step*3, mouseX, mouseY, "Ловкость " + prog.getStat(1) + " ур.");

        float wghtBonus = prog.getStat(0) * 3.0f;
        float runSpeed = 5.6f * (1.0f + (prog.getStat(1) * 0.03f));
        drawStatBlockStr(context, tr, "Скорость бега", String.format(java.util.Locale.US, "%.1f бл/с", runSpeed), "", col2, sY, mouseX, mouseY, "");
        drawStatBlock(context, tr, "Грузоподъемность", 40f, wghtBonus, col2, sY + step, mouseX, mouseY, "Сила " + prog.getStat(0) + " ур.");
        drawStatBlockStr(context, tr, "Шанс добычи", "x1.0", "", col2, sY + step*2, mouseX, mouseY, "");
        drawStatBlockStr(context, tr, "Кол-во добычи", "x1.0", "", col2, sY + step*3, mouseX, mouseY, "");

        if (tooltipToDraw != null) {
            ModularWindow.drawConquestTooltip(context, tr, tooltipToDraw, mouseX, mouseY, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        }
    }

    private void drawStatBlock(DrawContext context, TextRenderer tr, String label, float base, float bonus, int sx, int sy, int mx, int my, String source) {
        context.drawTextWithShadow(tr, label + ":", sx, sy, 0xFFDDDD);
        String baseStr = String.format(java.util.Locale.US, "%.1f", base).replace(".0", "");
        context.drawTextWithShadow(tr, baseStr, sx + tr.getWidth(label + ": "), sy, 0xFFFFFF);

        if (bonus > 0) {
            String bonusStr = String.format(java.util.Locale.US, "[+%.1f]", bonus).replace(".0", "");
            int bX = sx + tr.getWidth(label + ": " + baseStr + " ");
            context.drawTextWithShadow(tr, bonusStr, bX, sy, 0x55FF55);

            if (mx >= bX && mx <= bX + tr.getWidth(bonusStr) && my >= sy && my <= sy + 10) {
                List<Text> lines = new ArrayList<>();
                lines.add(Text.literal("§7Источник бонуса:"));
                lines.add(Text.literal("§a- " + source));
                lines.add(Text.literal("§a- Бафы: +0 (пока в разработке)"));
                tooltipToDraw = lines;
            }
        }
    }

    private void drawStatBlockStr(DrawContext context, TextRenderer tr, String label, String base, String bonus, int sx, int sy, int mx, int my, String source) {
        context.drawTextWithShadow(tr, label + ":", sx, sy, 0xFFDDDD);
        context.drawTextWithShadow(tr, base, sx + tr.getWidth(label + ": "), sy, 0xFFFFFF);
    }

    private void drawCard(DrawContext context, TextRenderer tr, int cx, int cy, int mouseX, int mouseY, int index, IProgressionComponent prog) {
        context.fill(cx, cy, cx + cardWidth, cy + cardHeight, 0xFF202020);
        context.drawBorder(cx, cy, cardWidth, cardHeight, 0xFF2C2C2C);

        long animTime = System.currentTimeMillis() - upgradeAnim[index];
        if (animTime < 400) {
            float alpha = 1.0f - (animTime / 400f);
            int alphaInt = (int)(alpha * 255);
            int color = (alphaInt << 24) | 0x55FF55;
            RenderSystem.enableBlend();
            context.drawBorder(cx - 1, cy - 1, cardWidth + 2, cardHeight + 2, color);
            RenderSystem.disableBlend();
        }

        boolean isStat = index < 5;
        String name = isStat ? STAT_NAMES[index] : WEAPON_NAMES[index - 5];
        int level = isStat ? prog.getStat(index) : prog.getWeaponSkill(index - 5);
        boolean canUpgrade = prog.getAvailablePoints() > 0 && level < 10;

        context.fill(cx + 4, cy + 5, cx + 28, cy + 29, 0xFF252525);
        context.drawBorder(cx + 4, cy + 5, 24, 24, 0xFF000000);
        if (isStat) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.drawTexture(STAT_ICONS[index], cx + 8, cy + 9, 0, 0, 16, 16, 16, 16);
        }

        context.getMatrices().push();
        if (tr.getWidth(name) > 60) context.getMatrices().scale(0.85f, 0.85f, 1.0f);
        context.drawTextWithShadow(tr, name, (int)((cx + 32) / (tr.getWidth(name) > 60 ? 0.85f : 1.0f)), (int)((cy + 8) / (tr.getWidth(name) > 60 ? 0.85f : 1.0f)), 0xFFFFFF);
        context.getMatrices().pop();

        int plusX = cx + cardWidth - 20;
        int plusY = cy + 5;
        boolean hoverPlus = canUpgrade && mouseX >= plusX && mouseX <= plusX + 16 && mouseY >= plusY && mouseY <= plusY + 16;

        int btnColor = canUpgrade ? (hoverPlus ? 0xFFFFAA00 : 0xFFFFA600) : 0xFF333333;
        int plusColor = canUpgrade ? 0xFFFFFFFF : 0xFF777777;

        context.fill(plusX, plusY, plusX + 16, plusY + 16, btnColor);
        context.drawTextWithShadow(tr, "+", plusX + 5, plusY + 4, plusColor);

        int barY = cy + 34;
        for (int b = 0; b < 10; b++) {
            int bx = cx + 8 + (b * 11);
            int color = (b < level) ? 0xFFFFFFFF : 0xFF555555;
            context.fill(bx, barY, bx + 9, barY + 6, color);
        }

        int modY = cy + 50;
        List<String> buffs = getBuffDescriptions(index, level);

        for (int m = 0; m < 3; m++) {
            int mx = cx + 8 + (m * 20);
            context.fill(mx, modY, mx + 16, modY + 16, 0xFF252525);
            context.drawBorder(mx, modY, 16, 16, 0xFF000000);

            if (m < buffs.size() && level > 0) {
                context.drawTextWithShadow(tr, "v", mx + 5, modY + 4, 0xFF55FF55);
                if (mouseX >= mx && mouseX <= mx + 16 && mouseY >= modY && mouseY <= modY + 16) {
                    List<Text> tLines = new ArrayList<>();
                    tLines.add(Text.literal("§6Бонус навыка:"));
                    tLines.add(Text.literal("§f" + buffs.get(m)));
                    tooltipToDraw = tLines;
                }
            }
        }
    }

    private List<String> getBuffDescriptions(int index, int level) {
        List<String> list = new ArrayList<>();
        if (level == 0) level = 1;
        switch (index) {
            case 0 -> list.add(String.format("Макс. вес: +%.1f кг", level * 3.0f));
            case 1 -> { list.add("Макс. выносливость: +" + (level * 10)); list.add(String.format("Скорость бега: +%d%%", level * 3)); }
            case 2 -> { list.add("Здоровье: +" + (level * 10)); list.add("Лечение: +" + (level * 3) + "%"); }
            case 3 -> list.add("Сохранность лута: +" + (level * 2) + "%");
            case 4 -> { list.add("Скорость лута: +" + (level * 5) + "%"); list.add("Эффект медицины: +" + (level * 3) + "%"); }
            case 5 -> list.add("Смена оружия: +" + (level * 5) + "%");
            case 6 -> list.add("Перезарядка: +" + (level * 4) + "%");
            case 7 -> list.add("Снижение отдачи: -" + (level * 3) + "%");
        }
        return list;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(client.player);
            if (prog != null) {
                int headerY = y + pad + 18;
                int rightEdge = x + width - pad;
                int respecW = 24;
                int pointsW = 80;
                int respecX = rightEdge - respecW;
                int pointsX = respecX - gapX - pointsW;

                if (mouseX >= respecX && mouseX <= respecX + respecW && mouseY >= headerY - 4 && mouseY <= headerY + 10) {
                    if (System.currentTimeMillis() - prog.getLastRespecTime() >= 86_400_000L) sendAction(2, 0);
                    return true;
                }

                int barY = headerY + 14;
                int gridStartX = x + pad;
                int gridStartY = barY + 16;

                for (int i = 0; i < 8; i++) {
                    int col = i % 4;
                    int row = i / 4;
                    int cx = gridStartX + col * (cardWidth + gapX);
                    int cy = gridStartY + row * (cardHeight + gapY);
                    int plusX = cx + cardWidth - 20;
                    int plusY = cy + 5;

                    if (mouseX >= plusX && mouseX <= plusX + 16 && mouseY >= plusY && mouseY <= plusY + 16) {
                        boolean isStat = i < 5;
                        int level = isStat ? prog.getStat(i) : prog.getWeaponSkill(i - 5);
                        if (prog.getAvailablePoints() > 0 && level < 10) {
                            sendAction(isStat ? 0 : 1, isStat ? i : i - 5);
                            upgradeAnim[i] = System.currentTimeMillis();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void sendAction(int actionType, int index) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(actionType);
        buf.writeInt(index);
        ClientPlayNetworking.send(new Identifier("conquest", "progression_action"), buf);
    }
}