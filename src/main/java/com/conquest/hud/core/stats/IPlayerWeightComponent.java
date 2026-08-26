package com.conquest.hud.core.stats;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IPlayerWeightComponent extends Component, AutoSyncedComponent {
    float getCurrentWeight();
    void setCurrentWeight(float weight);
}