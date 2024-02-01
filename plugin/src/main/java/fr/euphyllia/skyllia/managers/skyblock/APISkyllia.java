package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaImplementation;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.cache.PositionIslandCache;
import fr.euphyllia.skyllia.utils.RegionUtils;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class APISkyllia implements SkylliaImplementation {


    private final InterneAPI interneAPI;

    public APISkyllia(InterneAPI interneAPI) {
        this.interneAPI = interneAPI;
    }

    @Override
    public CompletableFuture<@NotNull Island> getIslandByPlayerId(UUID playerUniqueId) {
        return this.interneAPI.getSkyblockManager().getIslandByPlayerId(playerUniqueId);
    }

    @Override
    public CompletableFuture<@NotNull Island> getIslandByIslandId(UUID islandId) {
        return this.interneAPI.getSkyblockManager().getIslandByIslandId(islandId);
    }

    @Override
    public @NotNull Island getIslandByPosition(Position position) {
        return PositionIslandCache.getIsland(position);
    }

    @Override
    public @NotNull Island getIslandByChunk(Chunk chunk) {
        Position position = RegionUtils.getRegionInChunk(chunk.getX(), chunk.getZ());
        return PositionIslandCache.getIsland(position);
    }
}