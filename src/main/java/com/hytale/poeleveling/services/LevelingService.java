package com.hytale.poeleveling.services;

import com.hytale.poeleveling.components.PlayerLevelData;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class LevelingService {
    private static final int MAX_LEVEL = 100;
    private static final double BASE_XP = 60.0;
    private static final double XP_GROWTH = 1.35;

    private final ComponentType<EntityStore, PlayerLevelData> playerLevelDataType;
    private final NotificationService notificationService;

    public LevelingService(ComponentType<EntityStore, PlayerLevelData> playerLevelDataType,
                           NotificationService notificationService) {
        this.playerLevelDataType = playerLevelDataType;
        this.notificationService = notificationService;
    }

    public double getXpNeededForNextLevel(int level) {
        if (level >= MAX_LEVEL) {
            return Double.POSITIVE_INFINITY;
        }
        return BASE_XP * Math.pow(level, XP_GROWTH);
    }

    public void addExperience(PlayerRef playerRef, double amount, CommandBuffer<EntityStore> commandBuffer) {
        if (playerRef == null || amount <= 0) {
            return;
        }
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }

        PlayerLevelData data = commandBuffer.ensureAndGetComponent(ref, playerLevelDataType);
        int levelBefore = data.getLevel();
        data.addExperience(amount);

        int currentLevel = data.getLevel();
        double xp = data.getExperience();
        while (currentLevel < MAX_LEVEL) {
            double needed = getXpNeededForNextLevel(currentLevel);
            if (xp < needed) {
                break;
            }
            xp -= needed;
            currentLevel++;
        }

        if (currentLevel != data.getLevel() || xp != data.getExperience()) {
            data.setLevel(currentLevel);
            data.setExperience(xp);
        }

        if (currentLevel > levelBefore) {
            notificationService.sendLevelUp(playerRef, currentLevel);
        }
    }
}
