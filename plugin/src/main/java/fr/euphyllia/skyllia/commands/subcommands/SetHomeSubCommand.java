package fr.euphyllia.skyllia.commands.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.RegionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SetHomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetHomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.sethome")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        Location playerLocation = player.getLocation();
        int regionLocX = playerLocation.getChunk().getX();
        int regionLocZ = playerLocation.getChunk().getZ();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                try {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
                    if (island == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
                        return;
                    }
                    Position islandPosition = island.getPosition();
                    Position playerRegionPosition = RegionUtils.getRegionInChunk(regionLocX, regionLocZ);

                    if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotInIsland);
                        return;
                    }

                    Players executorPlayer = island.getMember(player.getUniqueId());

                    if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                        PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();
                        PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                        if (!permissionManager.hasPermission(PermissionsCommandIsland.SET_HOME)) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                            return;
                        }
                    }

                    boolean updateOrCreateHome = island.addWarps("home", playerLocation, false);
                    if (updateOrCreateHome) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageHomeCreateSuccess);
                    } else {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
                    }
                } catch (Exception e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
                }
            });
        } finally {
            executor.shutdown();
        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
