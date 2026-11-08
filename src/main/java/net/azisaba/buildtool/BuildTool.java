package net.azisaba.buildtool;

import net.azisaba.buildtool.command.GiveBuildToolCommand;
import net.azisaba.buildtool.listener.BuildToolListener;
import net.azisaba.buildtool.supply.StorageBoxHook;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class BuildTool extends JavaPlugin {

    private Set<String> enabledWorlds = Set.of();
    private int buildToolModelData;
    private StorageBoxHook storageBoxHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigData();

        // StorageBox classes are only touched when the plugin is present (softdepend)
        if (getServer().getPluginManager().isPluginEnabled("StorageBox")) {
            storageBoxHook = new StorageBoxHook();
            getLogger().info("StorageBox連携を有効化しました");
        }

        getServer().getPluginManager().registerEvents(new BuildToolListener(this), this);
        getCommand("giveBuildTool").setExecutor(new GiveBuildToolCommand(this));
    }

    public void loadConfigData() {
        reloadConfig();
        enabledWorlds = new HashSet<>(getConfig().getStringList("enableWorlds"));
        buildToolModelData = getConfig().getInt("model", 15);
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains(worldName);
    }

    public int getBuildToolModelData() {
        return buildToolModelData;
    }

    /** @return the StorageBox hook, or null when StorageBox is not installed */
    public StorageBoxHook getStorageBoxHook() {
        return storageBoxHook;
    }
}
