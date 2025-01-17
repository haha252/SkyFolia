package fr.euphyllia.skyllia.listeners.bukkitevents;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.concurrent.Executors;

public class PortailAlternativeFoliaEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(PortailAlternativeFoliaEvent.class);

    public PortailAlternativeFoliaEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalCreate(final PortalCreateEvent event) {
        // Todo : Jamais on ne crée de portail pour le monde Nether et End tant que Folia n'a pas implémenter les portails.
        //  De toute façon, le code va rester, car je ne pense pas qu'ils mettront les anciennes versions à jour.
        if (event.isCancelled()) return;
        World world = event.getWorld();
        if (world.getEnvironment().equals(World.Environment.NORMAL)) return;
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(world.getName()))) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTouchPortal(final PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        if (!this.api.isFolia()) {
            return;
        }
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Material blockType = location.getBlock().getType();
        if (blockType != Material.NETHER_PORTAL && blockType != Material.END_PORTAL_FRAME) {
            return;
        }
        World world = location.getWorld();
        if (!location.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            // Todo BUG avec les portails, vu que les listeners ne sont pas lu,
            //  impossible d'annuler, donc on fait uniquement via le monde normal.
            //  Ce sera possible quand Folia fixera cela.
            return;
        }
        // Obtenez le bloc sur lequel le joueur se tient
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            if (blockType == Material.NETHER_PORTAL) {
                callPlayerPrepareChangeWorldSkyblockEvent(player, PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER, world.getName());
            }
            if (blockType == Material.END_PORTAL_FRAME) {
                callPlayerPrepareChangeWorldSkyblockEvent(player, PlayerPrepareChangeWorldSkyblockEvent.PortalType.END, world.getName());
            }
        });

    }

    private void callPlayerPrepareChangeWorldSkyblockEvent(Player player, PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType, String worldName) {
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(worldName))) {
            return;
        }
        WorldConfig worldConfig = WorldUtils.getWorldConfig(worldName);
        if (worldConfig == null) return;
        Bukkit.getPluginManager().callEvent(new PlayerPrepareChangeWorldSkyblockEvent(player, worldConfig, portalType));
    }
}
