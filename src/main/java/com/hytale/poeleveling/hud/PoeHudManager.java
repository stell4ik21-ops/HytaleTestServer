package com.hytale.poeleveling.hud;

import com.hytale.poeleveling.components.PlayerLevelData;
import com.hytale.poeleveling.hud.backend.HudBackend;
import com.hytale.poeleveling.services.LevelingService;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PoeHudManager {
    private static final String HUD_KEY = "PoeLeveling_Level";
    private final LevelingService levelingService;
    private final HudBackend backend;
    private final Map<UUID, HudState> hudStates = new HashMap<>();

    public PoeHudManager(LevelingService levelingService, HudBackend backend) {
        this.levelingService = levelingService;
        this.backend = backend;
    }

    public void updateHud(Player player, PlayerRef playerRef, PlayerLevelData data) {
        if (player == null || playerRef == null || data == null) {
            return;
        }
        HudState state = hudStates.get(playerRef.getUuid());
        if (state == null) {
            PoeLevelHud hud = new PoeLevelHud(playerRef);
            state = new HudState(hud, data.getLevel(), data.getExperience());
            hudStates.put(playerRef.getUuid(), state);
            backend.show(player, playerRef, HUD_KEY, hud);
        }

        double needed = levelingService.getXpNeededForNextLevel(data.getLevel());
        float progress = (float) (needed > 0 && needed < Double.POSITIVE_INFINITY
            ? Math.min(1.0, data.getExperience() / needed)
            : 1.0);
        String text = String.format("Level %d • %.0f / %.0f XP",
            data.getLevel(),
            data.getExperience(),
            needed == Double.POSITIVE_INFINITY ? data.getExperience() : needed
        );

        if (state.lastLevel != data.getLevel() || Math.abs(state.lastXp - data.getExperience()) > 0.01) {
            state.lastLevel = data.getLevel();
            state.lastXp = data.getExperience();
            state.hud.updateLevelInfo(text, progress);
        }
    }

    public void hideHud(Player player, PlayerRef playerRef) {
        if (player == null || playerRef == null) {
            return;
        }
        backend.hide(player, playerRef, HUD_KEY);
        hudStates.remove(playerRef.getUuid());
    }

    public void clear(PlayerRef playerRef) {
        if (playerRef != null) {
            hudStates.remove(playerRef.getUuid());
        }
    }

    private static class HudState {
        private final PoeLevelHud hud;
        private int lastLevel;
        private double lastXp;

        private HudState(PoeLevelHud hud, int lastLevel, double lastXp) {
            this.hud = hud;
            this.lastLevel = lastLevel;
            this.lastXp = lastXp;
        }
    }
}
