package net.azisaba.buildtool.command;

import net.azisaba.buildtool.BuildTool;
import net.azisaba.buildtool.item.BuildToolItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveBuildToolCommand implements CommandExecutor {

    private final BuildTool plugin;

    public GiveBuildToolCommand(BuildTool plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("コンソールからは実行できません");
            return true;
        }

        ItemStack tool = BuildToolItem.create(plugin.getBuildToolModelData());
        // drop it at the player's feet when the inventory is full instead of losing it
        player.getInventory().addItem(tool).values()
                .forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        player.sendMessage(Component.text("BuildToolを入手しました", NamedTextColor.GREEN));
        return true;
    }
}
