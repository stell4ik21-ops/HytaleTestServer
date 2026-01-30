package com.hytale.poeleveling.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NpcLevelData implements Component<EntityStore> {
    private int level;

    public NpcLevelData() {
        this.level = 1;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    @Override
    public Component<EntityStore> clone() {
        NpcLevelData copy = new NpcLevelData();
        copy.level = this.level;
        return copy;
    }
}
