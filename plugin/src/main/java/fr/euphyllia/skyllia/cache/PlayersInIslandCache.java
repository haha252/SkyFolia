package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayersInIslandCache {

    private static final Logger logger = LogManager.getLogger(PlayersInIslandCache.class);
    private static final ConcurrentHashMap<UUID, CopyOnWriteArrayList<Players>> listPlayersInIsland = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, UUID> islandIdByPlayerId = new ConcurrentHashMap<>();

    public static Players getPlayers(UUID islandId, UUID playerId) {
        List<Players> playersInIsland = listPlayersInIsland.getOrDefault(islandId, new CopyOnWriteArrayList<>());
        if (playersInIsland.isEmpty()) {
            return new Players(playerId, null, islandId, RoleType.VISITOR);
        }
        for (Players players : playersInIsland) {
            if (players.getMojangId().equals(playerId)) {
                return players;
            }
        }
        return new Players(playerId, null, islandId, RoleType.VISITOR);
    }

    public static ConcurrentMap<UUID, UUID> getIslandIdByPlayerId() {
        return islandIdByPlayerId;
    }

    public static void delete(UUID islandId) {
        listPlayersInIsland.remove(islandId);
    }

    public static void add(UUID islandId, CopyOnWriteArrayList<Players> members) {
        listPlayersInIsland.put(islandId, members);
    }
}
