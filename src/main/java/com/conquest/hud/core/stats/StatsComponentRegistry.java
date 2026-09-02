package com.conquest.hud.core.stats;

import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.progression.ProgressionComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class StatsComponentRegistry implements EntityComponentInitializer {

    public static final ComponentKey<IStaminaComponent> STAMINA =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("conquest", "stamina"), IStaminaComponent.class);

    public static final ComponentKey<IPlayerWeightComponent> WEIGHT =
            ComponentRegistry.getOrCreate(new Identifier("conquest", "player_weight"), IPlayerWeightComponent.class);

    public static final ComponentKey<IProgressionComponent> PROGRESSION =
            ComponentRegistry.getOrCreate(new Identifier("conquest", "progression"), IProgressionComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(STAMINA, StaminaComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(WEIGHT, PlayerWeightComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PROGRESSION, ProgressionComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}