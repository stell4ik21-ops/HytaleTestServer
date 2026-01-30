package com.hytale.poeleveling.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Map;
import java.util.UUID;

public class DamageTrackingSystem extends DamageEventSystem {
    private final Map<UUID, UUID> lastAttackers;
    private final Map<UUID, String> entityNames;

    public DamageTrackingSystem(Map<UUID, UUID> lastAttackers, Map<UUID, String> entityNames) {
        this.lastAttackers = lastAttackers;
        this.entityNames = entityNames;
    }

    @Override
    public void handle(int index,
                       ArchetypeChunk<EntityStore> chunk,
                       Store<EntityStore> store,
                       CommandBuffer<EntityStore> commandBuffer,
                       Damage damage) {
        Ref<EntityStore> victimRef = chunk.getReferenceTo(index);
        if (victimRef == null || !victimRef.isValid()) {
            return;
        }

        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource)) {
            return;
        }

        Ref<EntityStore> attackerRef = ((Damage.EntitySource) source).getRef();
        if (attackerRef == null || !attackerRef.isValid()) {
            return;
        }

        Player attacker = store.getComponent(attackerRef, Player.getComponentType());
        if (attacker == null) {
            return;
        }

        UUIDComponent attackerUuidComponent = store.getComponent(attackerRef, UUIDComponent.getComponentType());
        UUIDComponent victimUuidComponent = store.getComponent(victimRef, UUIDComponent.getComponentType());
        if (attackerUuidComponent == null || victimUuidComponent == null) {
            return;
        }

        UUID attackerId = attackerUuidComponent.getUuid();
        UUID victimId = victimUuidComponent.getUuid();
        String name = "Unknown Entity";

        NPCEntity npcEntity = store.getComponent(victimRef, NPCEntity.getComponentType());
        if (npcEntity != null) {
            name = npcEntity.getNPCTypeId();
        }

        entityNames.put(victimId, name);
        lastAttackers.put(victimId, attackerId);
    }
}
