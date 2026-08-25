package com.conquest.hud.core.stats;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IPlayerStats extends ComponentV3, AutoSyncedComponent {
    int getStrength();
    int getAgility();
    int getVitality();
    int getMetabolism();
    int getIntellect();

    void setStrength(int value);
    void setAgility(int value);
    void setVitality(int value);
    void setMetabolism(int value);
    void setIntellect(int value);
}