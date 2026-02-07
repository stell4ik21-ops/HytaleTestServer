package com.hytale.poeleveling.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DeathProcessedMarker implements Component<EntityStore> {
    @Override
    public Component<EntityStore> clone() {
        return new DeathProcessedMarker();
    }
}
