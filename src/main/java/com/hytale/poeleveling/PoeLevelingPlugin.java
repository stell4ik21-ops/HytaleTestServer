package com.hytale.poeleveling;

import com.hytale.poeleveling.components.DeathProcessedMarker;
import com.hytale.poeleveling.components.NpcLevelData;
import com.hytale.poeleveling.components.PlayerLevelData;
import com.hytale.poeleveling.hud.PoeHudManager;
import com.hytale.poeleveling.hud.backend.DefaultHudBackend;
import com.hytale.poeleveling.hud.backend.HudBackend;
import com.hytale.poeleveling.hud.backend.MultipleHudBackend;
import com.hytale.poeleveling.services.LevelingService;
import com.hytale.poeleveling.services.NotificationService;
import com.hytale.poeleveling.systems.DamageTrackingSystem;
import com.hytale.poeleveling.systems.DeathDetectionSystem;
import com.hytale.poeleveling.systems.NpcLevelAssignmentSystem;
import com.hytale.poeleveling.systems.PlayerHudSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PoeLevelingPlugin extends JavaPlugin {
    private static PoeLevelingPlugin instance;

    private final Map<UUID, UUID> lastAttackers = new ConcurrentHashMap<>();
    private final Map<UUID, String> entityNames = new ConcurrentHashMap<>();

    private ComponentType<EntityStore, PlayerLevelData> playerLevelDataType;
    private ComponentType<EntityStore, NpcLevelData> npcLevelDataType;
    private ComponentType<EntityStore, DeathProcessedMarker> deathProcessedMarkerType;

    private NotificationService notificationService;
    private LevelingService levelingService;
    private PoeHudManager hudManager;

    public PoeLevelingPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static PoeLevelingPlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        playerLevelDataType = getEntityStoreRegistry().registerComponent(PlayerLevelData.class, PlayerLevelData::new);
        npcLevelDataType = getEntityStoreRegistry().registerComponent(NpcLevelData.class, NpcLevelData::new);
        deathProcessedMarkerType = getEntityStoreRegistry().registerComponent(DeathProcessedMarker.class, DeathProcessedMarker::new);

        notificationService = new NotificationService();
        levelingService = new LevelingService(playerLevelDataType, notificationService);
        hudManager = new PoeHudManager(levelingService, detectHudBackend());

        getEntityStoreRegistry().registerSystem(new DamageTrackingSystem(lastAttackers, entityNames));
        getEntityStoreRegistry().registerSystem(new DeathDetectionSystem(
            lastAttackers,
            entityNames,
            levelingService,
            notificationService,
            deathProcessedMarkerType,
            npcLevelDataType
        ));
        getEntityStoreRegistry().registerSystem(new NpcLevelAssignmentSystem(npcLevelDataType));
        getEntityStoreRegistry().registerSystem(new PlayerHudSystem(hudManager, playerLevelDataType));

        getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event -> {
            Holder<EntityStore> holder = event.getHolder();
            holder.ensureComponent(playerLevelDataType);
            Player player = holder.getComponent(Player.getComponentType());
            PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
            if (player != null && playerRef != null) {
                PlayerLevelData data = holder.getComponent(playerLevelDataType);
                hudManager.updateHud(player, playerRef, data);
            }
        });

        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, event -> {
            hudManager.clear(event.getPlayerRef());
        });
    }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() {
        return playerLevelDataType;
    }

    public LevelingService getLevelingService() {
        return levelingService;
    }

    public PoeHudManager getHudManager() {
        return hudManager;
    }

    private HudBackend detectHudBackend() {
        try {
            Class<?> clazz = Class.forName("com.buuz135.mhud.MultipleHUD");
            Method getInstance = clazz.getMethod("getInstance");
            Object instance = getInstance.invoke(null);
            if (instance != null) {
                return new MultipleHudBackend(instance);
            }
        } catch (Exception ignored) {
            // fallback to default
        }
        return new DefaultHudBackend();
    }
}
