package net.azisaba.buildtool.place;

import net.azisaba.buildtool.menu.PlaceMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** Places the blocks of one BuildTool use. */
public final class Placement {

    private Placement() {}

    /**
     * @param limit the most blocks that may be placed (what the player can pay for)
     * @return the number of blocks placed
     */
    public static int place(Player player, Block clicked, BlockFace face, Material material, PlaceMode mode, int amount, long limit) {
        if (amount <= 0 || limit <= 0) return 0;
        ItemStack hand = player.getInventory().getItemInMainHand();
        return switch (mode) {
            case LINE -> placeLine(player, hand, clicked, face, material, (int) Math.min(amount, limit));
            case SQUARE -> placeSquare(player, hand, clicked, face, material, amount, limit);
        };
    }

    /** A line away from the clicked face; stops at the first occupied or protected block. */
    private static int placeLine(Player player, ItemStack hand, Block clicked, BlockFace face, Material material, int count) {
        int placed = 0;
        Block against = clicked;
        for (int i = 0; i < count; i++) {
            Block target = against.getRelative(face);
            if (!target.getType().isAir() || !placeOne(player, hand, target, against, material)) break;
            placed++;
            against = target;
        }
        return placed;
    }

    /** A size x size square in front of the clicked face; occupied or protected blocks are skipped. */
    private static int placeSquare(Player player, ItemStack hand, Block clicked, BlockFace face, Material material, int size, long limit) {
        BlockFace[] axes = orthogonal(face);
        if (axes == null) return 0;
        Block origin = clicked.getRelative(face);
        int placed = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (placed >= limit) return placed;
                Block target = origin.getRelative(axes[0], i).getRelative(axes[1], j);
                if (target.getType().isAir() && placeOne(player, hand, target, clicked, material)) placed++;
            }
        }
        return placed;
    }

    /**
     * Places the block like vanilla does: set it first, then ask other plugins (protection, logging) through
     * BlockPlaceEvent, and restore the previous state if they cancel it.
     */
    private static boolean placeOne(Player player, ItemStack hand, Block target, Block against, Material material) {
        BlockState before = target.getState(); // snapshot for the event and for the rollback
        target.setType(material, true);
        BlockPlaceEvent event = new BlockPlaceEvent(target, before, against, hand, player, true, EquipmentSlot.HAND);
        event.callEvent();
        if (event.isCancelled() || !event.canBuild()) {
            before.update(true, false);
            return false;
        }
        return true;
    }

    private static BlockFace[] orthogonal(BlockFace face) {
        return switch (face) {
            case UP, DOWN -> new BlockFace[]{BlockFace.NORTH, BlockFace.EAST};
            case NORTH, SOUTH -> new BlockFace[]{BlockFace.UP, BlockFace.EAST};
            case EAST, WEST -> new BlockFace[]{BlockFace.UP, BlockFace.NORTH};
            default -> null;
        };
    }
}
