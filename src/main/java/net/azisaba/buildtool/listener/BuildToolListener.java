package net.azisaba.buildtool.listener;

import net.azisaba.buildtool.BuildTool;
import net.azisaba.buildtool.item.BuildToolItem;
import net.azisaba.buildtool.menu.OptionsMenu;
import net.azisaba.buildtool.place.Placement;
import net.azisaba.buildtool.supply.MaterialSupply;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One listener for everything. Every handler returns on its cheapest check first, because
 * PlayerInteractEvent and InventoryClickEvent fire for every player on the server.
 */
public final class BuildToolListener implements Listener {

    private static final int COOLDOWN_TICKS = 10;

    private final BuildTool plugin;
    // main thread only
    private final Map<UUID, OptionsMenu> menus = new HashMap<>();
    private final Map<UUID, Integer> lastUseTick = new HashMap<>();

    public BuildToolListener(BuildTool plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !BuildToolItem.isBuildTool(event.getItem())) return;

        Action action = event.getAction();
        Player player = event.getPlayer();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                event.setCancelled(true);
                player.openInventory(menu(player).getInventory());
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            use(event, player);
        }
    }

    private void use(PlayerInteractEvent event, Player player) {
        Block clicked = event.getClickedBlock();
        if (clicked == null || !plugin.isWorldEnabled(player.getWorld().getName())) return;

        int now = Bukkit.getCurrentTick();
        Integer last = lastUseTick.get(player.getUniqueId());
        if (last != null && now - last < COOLDOWN_TICKS) return;

        Material material = clicked.getType();
        if (!material.isBlock() || material.isAir()) return;

        MaterialSupply supply = new MaterialSupply(player, material, plugin.getStorageBoxHook());
        if (supply.available() <= 0) return;
        lastUseTick.put(player.getUniqueId(), now);

        OptionsMenu menu = menu(player);
        int placed = Placement.place(player, clicked, event.getBlockFace(), material, menu.getMode(), menu.getAmount(), supply.available());
        supply.take(placed);

        if (placed > 0) {
            plugin.getLogger().info(player.getName() + "はBuildToolを使用した (" + material + " x" + placed + ", "
                    + clicked.getWorld().getName() + " " + clicked.getX() + "," + clicked.getY() + "," + clicked.getZ() + ")");
        }
    }

    private OptionsMenu menu(Player player) {
        return menus.computeIfAbsent(player.getUniqueId(), uuid -> new OptionsMenu());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // getHolder(false): no block state snapshot for chests etc. (getHolder() makes one on every click)
        if (!(event.getInventory().getHolder(false) instanceof OptionsMenu menu)) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == event.getInventory()) {
            menu.click(event.getSlot(), event.isRightClick(), event.isShiftClick());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof OptionsMenu) event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        menus.remove(uuid);
        lastUseTick.remove(uuid);
    }
}
