package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.annotation.Experimental;
import fr.euphyllia.skyllia.api.annotation.Information;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.Executors;

@Experimental
@Information("Je n'ai pas tester encore cette fonctionnalité")
public class CacheManager {

    private final Logger logger = LogManager.getLogger(CacheManager.class);
    private final SkyblockManager skyblockManager;

    public CacheManager(SkyblockManager skyblockManager) {
        this.skyblockManager = skyblockManager;
    }

    public void updateCache(SkyblockManager skyblockManager, Player bPlayer) {
        Island pIsland = skyblockManager.getIslandByOwner(bPlayer.getUniqueId()).join();
        if (pIsland == null) {
            // ========= remove player
            PlayersInIslandCache.getIslandIdByPlayerId().remove(bPlayer.getUniqueId());
            return;
        }
        this.updateCacheIsland(pIsland, bPlayer.getUniqueId());
    }

    public void deleteCacheIsland(Island island) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            // ============= player cache
            for (Players players : island.getMembers()) {
                PlayersInIslandCache.getIslandIdByPlayerId().remove(players.getMojangId(), island.getId());
            }

            PlayersInIslandCache.delete(island.getId());
            // ============= position island cache
            PositionIslandCache.delete(island.getPosition());
            // ============= permission role cache
            for (RoleType roleType : RoleType.values()) {
                for (PermissionsType permissionsType : PermissionsType.values()) {
                    PermissionRoleInIslandCache.deletePermissionInIsland(island.getId(), roleType, permissionsType);
                }
            }
        });
    }

    public void updateCacheIsland(Island island, UUID playerId) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            // ============= player cache
            PlayersInIslandCache.getIslandIdByPlayerId().put(playerId, island.getId());
            PlayersInIslandCache.add(island.getId(), island.getMembers());
            // ============= position island cache
            PositionIslandCache.add(island.getPosition(), island);
            // ============= permission role cache
            this.updatePermissionCacheIsland(island);
        });
    }

    public void updatePermissionCacheIsland(Island island) {
        for (RoleType roleType : RoleType.values()) {
            for (PermissionsType permissionsType : PermissionsType.values()) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), permissionsType, roleType).join();
                PermissionRoleInIslandCache.addPermissionInIsland(island.getId(), roleType, permissionsType, permissionRoleIsland);
            }
        }
    }
}
