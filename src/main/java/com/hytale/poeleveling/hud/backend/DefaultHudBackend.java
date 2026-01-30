package com.hytale.poeleveling.hud.backend;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class DefaultHudBackend implements HudBackend {
    @Override
    public void show(Player player, PlayerRef playerRef, String key, CustomUIHud hud) {
        if (player == null || playerRef == null) {
            return;
        }
        player.getHudManager().setCustomHud(playerRef, hud);
        hud.show();
    }

    @Override
    public void hide(Player player, PlayerRef playerRef, String key) {
        if (player == null || playerRef == null) {
            return;
        }
        player.getHudManager().resetHud(playerRef);
    }
}
