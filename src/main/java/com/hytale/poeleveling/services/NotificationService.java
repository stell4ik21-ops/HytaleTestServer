package com.hytale.poeleveling.services;

import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;

public class NotificationService {
    public void sendXpGain(PlayerRef playerRef, double amount) {
        if (playerRef == null || amount <= 0) {
            return;
        }
        Message message = Message.raw(String.format("You gained %.0f XP.", amount));
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), message, NotificationStyle.Default);
    }

    public void sendLevelUp(PlayerRef playerRef, int level) {
        if (playerRef == null) {
            return;
        }
        Message message = Message.raw(String.format("Level up! You are now level %d.", level));
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), message, NotificationStyle.Success);
    }

    public void sendKill(PlayerRef playerRef, String targetName, int targetLevel, double xp) {
        if (playerRef == null) {
            return;
        }
        Message message = Message.raw(
            String.format("Defeated %s (Lvl %d) and gained %.0f XP.", targetName, targetLevel, xp)
        );
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), message, NotificationStyle.Default);
    }
}
