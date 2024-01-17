package fr.euphyllia.skyfolia.listeners.skyblockevents;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.configuration.WorldConfig;
import fr.euphyllia.skyfolia.api.event.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyfolia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyfolia.api.event.SkyblockDeleteEvent;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.listeners.ListenersUtils;
import fr.euphyllia.skyfolia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkyblockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(SkyblockEvent.class);

    public SkyblockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkyblockCreate(final SkyblockCreateEvent event) {
        this.api.getCacheManager().updateCacheIsland(event.getIsland(), event.getIsland().getOwnerId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkyblockDelete(final SkyblockDeleteEvent event) {
        if (event.isCancelled()) return;
        this.api.getCacheManager().deleteCacheIsland(event.getIsland());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPrepareChangeWorldSkyblock(final PlayerPrepareChangeWorldSkyblockEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (event.getPortalType() == PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER) {
            teleportOtherWorld(player, event, PermissionsIsland.USE_NETHER_PORTAL);
        } else if (event.getPortalType() == PlayerPrepareChangeWorldSkyblockEvent.PortalType.END) {
            teleportOtherWorld(player, event, PermissionsIsland.USE_END_PORTAL);
        }
    }

    private void teleportOtherWorld(Player player, PlayerPrepareChangeWorldSkyblockEvent event, PermissionsIsland permissionsIsland) {
        Island island = ListenersUtils.checkPermission(player.getChunk(), player, permissionsIsland, event);
        if (island == null) {
            return;
        }
        try {
            WorldConfig worldConfig = event.getWorldConfig();
            if (!WorldUtils.isWorldSkyblock(worldConfig.netherPortalDestination())) {
                logger.log(Level.ERROR, "The %s world is not a skyblock world!".formatted(worldConfig.netherPortalDestination()));
                return;
            }
            World world = Bukkit.getWorld(worldConfig.netherPortalDestination());
            if (world == null) {
                logger.log(Level.ERROR, "The %s world is not loaded or not exist!".formatted(worldConfig.netherPortalDestination()));
                return;
            }
            Location playerLocation = player.getLocation();
            Location futurLocation = new Location(world, playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
            Bukkit.getRegionScheduler().run(this.api.getPlugin(), futurLocation, task -> {
                if (WorldUtils.isSafeLocation(futurLocation)) {
                    player.getScheduler().run(this.api.getPlugin(), task1 -> player.teleportAsync(futurLocation), null);
                } else {
                    LanguageToml.sendMessage(this.api.getPlugin(), player, LanguageToml.messageLocationNotSafe);
                }
            });
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }


}