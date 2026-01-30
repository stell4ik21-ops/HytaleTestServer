package com.hytale.poeleveling.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class PoeLevelHud extends CustomUIHud {
    private String levelText = "Level 1 • 0 / 100 XP";
    private float progress = 0.0f;

    public PoeLevelHud(PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(UICommandBuilder builder) {
        builder.append("PoeLevelHud.ui");
        builder.set("#LevelLabel.Text", levelText);
        builder.set("#XPBar.Value", progress);
    }

    public void updateLevelInfo(String levelText, float progress) {
        this.levelText = levelText;
        this.progress = progress;
        requestUpdate();
    }

    private void requestUpdate() {
        UICommandBuilder builder = new UICommandBuilder();
        builder.set("#LevelLabel.Text", levelText);
        builder.set("#XPBar.Value", progress);
        update(false, builder);
    }
}
