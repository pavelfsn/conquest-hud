package com.conquest.hud.core.stats;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IStaminaComponent extends Component, AutoSyncedComponent {
    float getStamina();
    float getMaxStamina();
    void setStamina(float value);
}