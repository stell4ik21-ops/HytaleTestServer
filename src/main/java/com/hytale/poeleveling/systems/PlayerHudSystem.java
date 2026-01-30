package com.hytale.poeleveling.systems;

import com.hytale.poeleveling.components.PlayerLevelData;
import com.hytale.poeleveling.hud.PoeHudManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHudSystem extends EntityTickingSystem<EntityStore> {
    private static final long HUD_UPDATE_INTERVAL_NANOS = 100_000_000L;

    private final PoeHudManager hudManager;
    private final ComponentType<EntityStore, PlayerLevelData> playerLevelDataType;
    private final Map<UUID, Long> lastHudPollNanos = new HashMap<>();
    private final Query<EntityStore> query;

    public PlayerHudSystem(PoeHudManager hudManager, ComponentType<EntityStore, PlayerLevelData> playerLevelDataType) {
        this.hudManager = hudManager;
        this.playerLevelDataType = playerLevelDataType;
        this.query = Query.and(Player.getComponentType(), PlayerRef.getComponentType());
    }

    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void tick(float delta,
                     int index,
                     ArchetypeChunk<EntityStore> chunk,
                     Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        Holder<EntityStore> holder = EntityUtils.toHolder(index, chunk);
        Player player = holder.getComponent(Player.getComponentType());
        PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (player == null || playerRef == null) {
            return;
        }

        PlayerLevelData data = holder.getComponent(playerLevelDataType);
        if (data == null) {
            return;
        }

        long now = System.nanoTime();
        Long last = lastHudPollNanos.get(playerRef.getUuid());
        if (last != null && now - last < HUD_UPDATE_INTERVAL_NANOS) {
            return;
        }
        lastHudPollNanos.put(playerRef.getUuid(), now);

        hudManager.updateHud(player, playerRef, data);
    }
}
