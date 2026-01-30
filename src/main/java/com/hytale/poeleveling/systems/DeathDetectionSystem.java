package com.hytale.poeleveling.systems;

import com.hytale.poeleveling.components.DeathProcessedMarker;
import com.hytale.poeleveling.components.NpcLevelData;
import com.hytale.poeleveling.services.LevelingService;
import com.hytale.poeleveling.services.NotificationService;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;

public class DeathDetectionSystem extends EntityTickingSystem<EntityStore> {
    private static final double BASE_XP = 8.0;
    private static final double HEALTH_XP_MULTIPLIER = 0.35;
    private static final double LEVEL_XP_MULTIPLIER = 2.0;

    private final Map<UUID, UUID> lastAttackers;
    private final Map<UUID, String> entityNames;
    private final LevelingService levelingService;
    private final NotificationService notificationService;
    private final ComponentType<EntityStore, DeathProcessedMarker> deathProcessedMarkerType;
    private final ComponentType<EntityStore, NpcLevelData> npcLevelDataType;

    public DeathDetectionSystem(Map<UUID, UUID> lastAttackers,
                                Map<UUID, String> entityNames,
                                LevelingService levelingService,
                                NotificationService notificationService,
                                ComponentType<EntityStore, DeathProcessedMarker> deathProcessedMarkerType,
                                ComponentType<EntityStore, NpcLevelData> npcLevelDataType) {
        this.lastAttackers = lastAttackers;
        this.entityNames = entityNames;
        this.levelingService = levelingService;
        this.notificationService = notificationService;
        this.deathProcessedMarkerType = deathProcessedMarkerType;
        this.npcLevelDataType = npcLevelDataType;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
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

        if (store.getComponent(ref, DeathComponent.getComponentType()) == null) {
            return;
        }

        if (store.getComponent(ref, deathProcessedMarkerType) != null) {
            return;
        }

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }

        UUID victimId = uuidComponent.getUuid();
        UUID attackerId = lastAttackers.remove(victimId);
        String name = entityNames.remove(victimId);
        if (name == null) {
            name = "Unknown Entity";
        }

        if (attackerId != null) {
            PlayerRef attackerRef = Universe.get().getPlayer(attackerId);
            if (attackerRef != null) {
                int targetLevel = 1;
                NpcLevelData npcLevelData = store.getComponent(ref, npcLevelDataType);
                if (npcLevelData != null) {
                    targetLevel = npcLevelData.getLevel();
                }

                double maxHealth = getMaxHealth(store, ref);
                double xp = Math.max(0.0, BASE_XP + maxHealth * HEALTH_XP_MULTIPLIER + targetLevel * LEVEL_XP_MULTIPLIER);
                if (xp > 0.0) {
                    levelingService.addExperience(attackerRef, xp, commandBuffer);
                    notificationService.sendKill(attackerRef, name.replace('_', ' '), targetLevel, xp);
                }
            }
        }

        commandBuffer.addComponent(ref, deathProcessedMarkerType, new DeathProcessedMarker());
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
