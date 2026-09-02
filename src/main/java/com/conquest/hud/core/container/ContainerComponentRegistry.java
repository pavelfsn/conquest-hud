package com.conquest.hud.core.container;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class ContainerComponentRegistry implements EntityComponentInitializer {
    public static final ComponentKey<IPlayerContainers> CONTAINERS =
            ComponentRegistry.getOrCreate(new Identifier("conquest", "player_containers"), IPlayerContainers.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // ALWAYS_COPY гарантирует, что наши контейнеры не исчезнут при возрождении
        registry.registerForPlayers(CONTAINERS, PlayerContainersComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}