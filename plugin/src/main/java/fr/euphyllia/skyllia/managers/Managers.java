package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.managers.world.WorldsManager;
import org.bukkit.Bukkit;

public class Managers {

    private final WorldsManager worldsManager;
    private final InterneAPI api;

    public Managers(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.worldsManager = new WorldsManager(this.api);
    }

    public void init() {
        if (this.worldsManager != null) {
            Bukkit.getGlobalRegionScheduler().run(this.api.getPlugin(), task -> this.worldsManager.initWorld());
        }
    }
}
