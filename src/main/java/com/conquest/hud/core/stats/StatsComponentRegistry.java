package com.conquest.hud.core.stats;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class StatsComponentRegistry implements EntityComponentInitializer {
    public static final ComponentKey<IPlayerStats> PLAYER_STATS =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("conquest", "player_stats"), IPlayerStats.class);

    public static final ComponentKey<IStaminaComponent> STAMINA =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("conquest", "stamina"), IStaminaComponent.class);

    public static final ComponentKey<IPlayerWeightComponent> WEIGHT =
            ComponentRegistry.getOrCreate(new Identifier("conquest", "player_weight"), IPlayerWeightComponent.class);
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_STATS, PlayerStatsComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(STAMINA, StaminaComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(WEIGHT, PlayerWeightComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
    
}