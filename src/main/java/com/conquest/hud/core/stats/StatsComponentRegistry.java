package com.conquest.hud.core.stats;

import com.conquest.hud.ConquestHud;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class StatsComponentRegistry implements EntityComponentInitializer {
    public static final ComponentKey<IPlayerStats> PLAYER_STATS =
            ComponentRegistry.getOrCreate(new Identifier(ConquestHud.MOD_ID, "player_stats"), IPlayerStats.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_STATS, PlayerStatsComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}