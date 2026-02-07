package com.hytale.poeleveling.systems;

import com.hytale.poeleveling.components.NpcLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.concurrent.ThreadLocalRandom;

public class NpcLevelAssignmentSystem extends EntityTickingSystem<EntityStore> {
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 40;
    private static final double HEALTH_PER_LEVEL = 10.0;

    private final ComponentType<EntityStore, NpcLevelData> npcLevelDataType;
    private final Query<EntityStore> query;

    public NpcLevelAssignmentSystem(ComponentType<EntityStore, NpcLevelData> npcLevelDataType) {
        this.npcLevelDataType = npcLevelDataType;
        this.query = Query.and(
            NPCEntity.getComponentType(),
            NewSpawnComponent.getComponentType()
        );
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
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) {
            return;
        }

        if (store.getComponent(ref, npcLevelDataType) != null) {
            return;
        }

        int level = calculateLevel(store, ref);
        NpcLevelData data = new NpcLevelData();
        data.setLevel(level);
        commandBuffer.addComponent(ref, npcLevelDataType, data);
    }

    private int calculateLevel(Store<EntityStore> store, Ref<EntityStore> ref) {
        double maxHealth = getMaxHealth(store, ref);
        if (maxHealth <= 0.0) {
            return ThreadLocalRandom.current().nextInt(MIN_LEVEL, MAX_LEVEL + 1);
        }
        int level = (int) Math.ceil(maxHealth / HEALTH_PER_LEVEL);
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }

    private double getMaxHealth(Store<EntityStore> store, Ref<EntityStore> ref) {
        EntityStatsModule statsModule = EntityStatsModule.get();
        if (statsModule == null) {
            return 0.0;
        }
        ComponentType<EntityStore, EntityStatMap> statMapType = statsModule.getEntityStatMapComponentType();
        EntityStatMap statMap = store.getComponent(ref, statMapType);
        if (statMap == null) {
            return 0.0;
        }
        int healthId = DefaultEntityStatTypes.getHealth();
        EntityStatValue health = statMap.get(healthId);
        if (health == null) {
            return 0.0;
        }
        return health.getMax();
    }
}
