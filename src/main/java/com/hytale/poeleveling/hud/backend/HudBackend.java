package com.hytale.poeleveling.hud.backend;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public interface HudBackend {
    void show(Player player, PlayerRef playerRef, String key, CustomUIHud hud);

    void hide(Player player, PlayerRef playerRef, String key);
}
