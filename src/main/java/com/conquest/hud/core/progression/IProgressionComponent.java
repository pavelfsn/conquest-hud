package com.conquest.hud.core.progression;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IProgressionComponent extends Component, AutoSyncedComponent {
    long getXp();
    void addXp(long amount);
    int getLevel();
    int getAvailablePoints();

    long getNextLevelXp();
    long getCurrentLevelBaseXp();

    int getStat(int index); // 0=Сила, 1=Ловкость, 2=Метаболизм, 3=Удача, 4=Восприятие
    boolean upgradeStat(int index);
    void setStatRaw(int index, int value);

    int getWeaponSkill(int index); // 0=Доставание, 1=Перезарядка, 2=Контроль
    boolean upgradeWeaponSkill(int index);
    void setWeaponSkillRaw(int index, int value);

    long getLastRespecTime();
    boolean tryRespec();

    void forceRespec();
}