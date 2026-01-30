package com.hytale.poeleveling.hud.backend;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.lang.reflect.Method;

public class MultipleHudBackend implements HudBackend {
    private final Object multipleHudInstance;
    private final Method setCustomHudMethod;
    private final Method hideCustomHudMethod;

    public MultipleHudBackend(Object multipleHudInstance) throws NoSuchMethodException {
        this.multipleHudInstance = multipleHudInstance;
        Class<?> clazz = multipleHudInstance.getClass();
        this.setCustomHudMethod = clazz.getMethod(
            "setCustomHud",
            Player.class,
            PlayerRef.class,
            String.class,
            CustomUIHud.class
        );
        this.hideCustomHudMethod = clazz.getMethod(
            "hideCustomHud",
            Player.class,
            PlayerRef.class,
            String.class
        );
    }

    @Override
    public void show(Player player, PlayerRef playerRef, String key, CustomUIHud hud) {
        if (player == null || playerRef == null) {
            return;
        }
        try {
            setCustomHudMethod.invoke(multipleHudInstance, player, playerRef, key, hud);
        } catch (Exception ignored) {
            // fallback to no-op
        }
    }

    @Override
    public void hide(Player player, PlayerRef playerRef, String key) {
        if (player == null || playerRef == null) {
            return;
        }
        try {
            hideCustomHudMethod.invoke(multipleHudInstance, player, playerRef, key);
        } catch (Exception ignored) {
            // fallback to no-op
        }
    }
}
