package com.hytale.poeleveling.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerLevelData implements Component<EntityStore> {
    private int level;
    private double experience;

    public PlayerLevelData() {
        this.level = 1;
        this.experience = 0.0;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = Math.max(0.0, experience);
    }

    public void addExperience(double amount) {
        if (amount <= 0) {
            return;
        }
        this.experience += amount;
    }

    @Override
    public Component<EntityStore> clone() {
        PlayerLevelData copy = new PlayerLevelData();
        copy.level = this.level;
        copy.experience = this.experience;
        return copy;
    }
}
