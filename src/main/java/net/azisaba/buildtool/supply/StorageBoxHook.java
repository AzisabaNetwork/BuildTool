package net.azisaba.buildtool.supply;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import xyz.acrylicstyle.storageBox.utils.StorageBox;
import xyz.acrylicstyle.storageBox.utils.StorageBoxUtils;

import java.util.Map;

/** All StorageBox API usage lives here, so BuildTool loads without StorageBox installed. */
public final class StorageBoxHook {

    /** A StorageBox in the player's inventory that holds plain items of the material. */
    public static final class Source {
        private final int slot;
        private final StorageBox box;

        private Source(int slot, StorageBox box) {
            this.slot = slot;
            this.box = box;
        }

        public long amount() {
            return Math.max(0L, box.getAmount());
        }
    }

    /** @return the auto-collecting box for plain items of the material, or null */
    public Source find(PlayerInventory inventory, Material material) {
        Map.Entry<Integer, StorageBox> entry = StorageBoxUtils.getStorageBoxForType(inventory, new ItemStack(material));
        if (entry == null) return null;
        StorageBox box = entry.getValue();
        ItemStack component = box.getComponentItemStack();
        // only boxes of plain blocks: a box of renamed/custom blocks must not be spent as normal blocks
        if (box.isEmpty() || component == null || component.hasItemMeta()) return null;
        return new Source(entry.getKey(), box);
    }

    /** Takes the amount out of the box and writes the box item back once. */
    public void take(PlayerInventory inventory, Source source, long amount) {
        if (amount <= 0) return;
        source.box.setAmount(source.box.getAmount() - amount);
        inventory.setItem(source.slot, source.box.getItemStack());
    }
}
