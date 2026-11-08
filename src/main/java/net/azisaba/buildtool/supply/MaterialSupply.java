package net.azisaba.buildtool.supply;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * The blocks a player can spend: plain (no meta) stacks of the material in the inventory, plus a StorageBox
 * of the material. Counted once before placing and taken once after placing, instead of per block.
 */
public final class MaterialSupply {

    private final Player player;
    private final Material material;
    private final boolean creative;
    private final StorageBoxHook hook;
    private final StorageBoxHook.Source box;
    private final long available;

    public MaterialSupply(Player player, Material material, StorageBoxHook hook) {
        this.player = player;
        this.material = material;
        this.creative = player.getGameMode() == GameMode.CREATIVE;
        this.hook = hook;
        if (creative) {
            this.box = null;
            this.available = Long.MAX_VALUE;
            return;
        }
        this.box = hook == null ? null : hook.find(player.getInventory(), material);
        long amount = box == null ? 0L : box.amount();
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (isPlain(item)) amount += item.getAmount();
        }
        this.available = amount;
    }

    public long available() {
        return available;
    }

    /** Takes the placed blocks: from the StorageBox first, then from inventory stacks. */
    public void take(long amount) {
        if (creative || amount <= 0) return;
        PlayerInventory inventory = player.getInventory();
        if (box != null) {
            long fromBox = Math.min(amount, box.amount());
            hook.take(inventory, box, fromBox);
            amount -= fromBox;
        }
        ItemStack[] contents = inventory.getStorageContents();
        for (int slot = 0; slot < contents.length && amount > 0; slot++) {
            ItemStack item = contents[slot];
            if (!isPlain(item)) continue;
            int taken = (int) Math.min(amount, item.getAmount());
            item.setAmount(item.getAmount() - taken);
            inventory.setItem(slot, item.getAmount() <= 0 ? null : item);
            amount -= taken;
        }
    }

    private boolean isPlain(ItemStack item) {
        return item != null && item.getType() == material && !item.hasItemMeta();
    }
}
