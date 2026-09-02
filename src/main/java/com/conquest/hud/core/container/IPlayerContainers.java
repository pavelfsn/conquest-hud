package com.conquest.hud.core.container;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface IPlayerContainers extends Component {
    ItemContainer getInventory();
    ItemContainer getEquipment();
    ItemContainer getHotbar();
}