package com.conquest.hud.core.progression;

import com.conquest.hud.core.stats.StatsComponentRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ProgressionComponent implements IProgressionComponent {
    private final PlayerEntity provider;
    private long xp = 0;
    private int level = 1;
    private int availablePoints = 0;
    private long lastRespecTime = 0;

    private final int[] stats = new int[5];
    private final int[] weaponSkills = new int[3];

    private static final long[] XP_MILESTONES = {
            0L, 3_000_000L, 25_000_000L, 120_000_000L, 400_000_000L,
            900_000_000L, 1_700_000_000L, 2_200_000_000L, 2_500_000_000L
    };
    private static final int[] LVL_MILESTONES = { 1, 10, 20, 30, 40, 50, 60, 70, 75 };

    public ProgressionComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override
    public long getXp() { return xp; }

    @Override
    public void addXp(long amount) {
        if (level >= 75) return;
        this.xp += amount;
        if (this.xp > 2_500_000_000L) this.xp = 2_500_000_000L;

        int newLevel = calculateLevelFromXp(this.xp);
        if (newLevel > this.level) {
            int oldLevel = this.level;
            int pointsEarned = calculatePointsForLevelRange(this.level + 1, newLevel);
            this.level = newLevel;
            this.availablePoints += pointsEarned;

            if (this.provider instanceof ServerPlayerEntity serverPlayer) {
                com.conquest.hud.core.network.LevelUpPacket.send(serverPlayer, oldLevel, newLevel);
                // Оповещение в чат
                serverPlayer.sendMessage(net.minecraft.text.Text.literal("§e[Conquest] §fВаш уровень повышен до §6" + newLevel + "§f! Получены новые очки характеристик."), false);
            }
        }
        StatsComponentRegistry.PROGRESSION.sync(provider);
    }

    public long getRequiredXpForLevel(int targetLevel) {
        if (targetLevel <= 1) return 0L;
        if (targetLevel >= 75) return 2_500_000_000L;
        for (int i = 0; i < XP_MILESTONES.length - 1; i++) {
            if (targetLevel >= LVL_MILESTONES[i] && targetLevel <= LVL_MILESTONES[i+1]) {
                long xpRange = XP_MILESTONES[i+1] - XP_MILESTONES[i];
                int lvlRange = LVL_MILESTONES[i+1] - LVL_MILESTONES[i];
                return XP_MILESTONES[i] + ((long)(targetLevel - LVL_MILESTONES[i]) * xpRange / lvlRange);
            }
        }
        return 0L;
    }

    @Override public long getNextLevelXp() { return getRequiredXpForLevel(this.level + 1); }
    @Override public long getCurrentLevelBaseXp() { return getRequiredXpForLevel(this.level); }

    private int calculateLevelFromXp(long currentXp) {
        if (currentXp >= XP_MILESTONES[XP_MILESTONES.length - 1]) return 75;
        for (int i = 0; i < XP_MILESTONES.length - 1; i++) {
            if (currentXp >= XP_MILESTONES[i] && currentXp < XP_MILESTONES[i + 1]) {
                long xpRange = XP_MILESTONES[i + 1] - XP_MILESTONES[i];
                int lvlRange = LVL_MILESTONES[i + 1] - LVL_MILESTONES[i];
                long xpIntoRange = currentXp - XP_MILESTONES[i];
                return LVL_MILESTONES[i] + (int)((xpIntoRange * lvlRange) / xpRange);
            }
        }
        return 1;
    }

    private int calculatePointsForLevelRange(int startLvl, int endLvl) {
        int points = 0;
        for (int i = startLvl; i <= endLvl; i++) {
            if (i >= 2 && i <= 55) points += 1;
            else if (i >= 56 && i <= 60) points += 2;
            else if (i >= 61 && i <= 74) points += 1;
            else if (i == 75) points += 2;
        }
        return points;
    }

    @Override public int getLevel() { return level; }
    @Override public int getAvailablePoints() { return availablePoints; }
    @Override public int getStat(int index) { return stats[index]; }

    @Override
    public boolean upgradeStat(int index) {
        if (availablePoints > 0 && stats[index] < 10) {
            stats[index]++;
            availablePoints--;
            StatsComponentRegistry.PROGRESSION.sync(provider);
            return true;
        }
        return false;
    }

    @Override
    public void setStatRaw(int index, int value) {
        if (index >= 0 && index < 5) {
            this.stats[index] = Math.max(0, value); // Сняли лимит 10 для оверкапа админом
            StatsComponentRegistry.PROGRESSION.sync(provider);
        }
    }

    @Override public int getWeaponSkill(int index) { return weaponSkills[index]; }

    @Override
    public boolean upgradeWeaponSkill(int index) {
        if (availablePoints > 0 && weaponSkills[index] < 10) {
            weaponSkills[index]++;
            availablePoints--;
            StatsComponentRegistry.PROGRESSION.sync(provider);
            return true;
        }
        return false;
    }

    @Override
    public void setWeaponSkillRaw(int index, int value) {
        if (index >= 0 && index < 3) {
            this.weaponSkills[index] = Math.max(0, value); // Сняли лимит
            StatsComponentRegistry.PROGRESSION.sync(provider);
        }
    }

    @Override public long getLastRespecTime() { return lastRespecTime; }

    @Override
    public boolean tryRespec() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRespecTime >= 86_400_000L) {
            int spentPoints = 0;
            for (int i = 0; i < stats.length; i++) { spentPoints += stats[i]; stats[i] = 0; }
            for (int i = 0; i < weaponSkills.length; i++) { spentPoints += weaponSkills[i]; weaponSkills[i] = 0; }

            availablePoints += spentPoints;
            lastRespecTime = currentTime;
            StatsComponentRegistry.PROGRESSION.sync(provider);
            return true;
        }
        return false;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.xp = tag.getLong("XP");
        this.level = tag.getInt("Level");
        this.availablePoints = tag.getInt("Points");
        this.lastRespecTime = tag.getLong("LastRespec");
        int[] savedStats = tag.getIntArray("Stats");
        if (savedStats.length == 5) System.arraycopy(savedStats, 0, this.stats, 0, 5);
        int[] savedSkills = tag.getIntArray("WeaponSkills");
        if (savedSkills.length == 3) System.arraycopy(savedSkills, 0, this.weaponSkills, 0, 3);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("XP", xp);
        tag.putInt("Level", level);
        tag.putInt("Points", availablePoints);
        tag.putLong("LastRespec", lastRespecTime);
        tag.putIntArray("Stats", stats);
        tag.putIntArray("WeaponSkills", weaponSkills);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.xp = buf.readLong();
        this.level = buf.readInt();
        this.availablePoints = buf.readInt();
        this.lastRespecTime = buf.readLong();
        for (int i = 0; i < 5; i++) this.stats[i] = buf.readInt();
        for (int i = 0; i < 3; i++) this.weaponSkills[i] = buf.readInt();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeLong(xp);
        buf.writeInt(level);
        buf.writeInt(availablePoints);
        buf.writeLong(lastRespecTime);
        for (int stat : stats) buf.writeInt(stat);
        for (int skill : weaponSkills) buf.writeInt(skill);
    }
}