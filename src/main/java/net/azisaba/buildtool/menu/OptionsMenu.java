package net.azisaba.buildtool.menu;

import net.azisaba.buildtool.item.BuildToolItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * The settings of one player and their options inventory. The holder identifies the menu, so
 * click listeners can tell it apart with a single instanceof check (no title comparison).
 */
public final class OptionsMenu implements InventoryHolder {

    private final Map<PlaceMode, Integer> amounts = new EnumMap<>(PlaceMode.class);
    private final Inventory inventory;
    private PlaceMode mode = PlaceMode.LINE;

    public OptionsMenu() {
        for (PlaceMode m : PlaceMode.values()) amounts.put(m, 1);
        inventory = Bukkit.createInventory(this, 9, BuildToolItem.legacy("Build Tool Options"));
        render();
    }

    public PlaceMode getMode() {
        return mode;
    }

    public int getAmount() {
        return amounts.get(mode);
    }

    /** Left click +1, right click -1, with shift x5; wraps around at both ends. Selects the mode. */
    public void click(int slot, boolean rightClick, boolean shiftClick) {
        PlaceMode clicked = PlaceMode.fromSlot(slot);
        if (clicked == null) return;
        int delta = (rightClick ? -1 : 1) * (shiftClick ? 5 : 1);
        int amount = amounts.get(clicked) + delta;
        if (amount > clicked.maxAmount()) amount = 1;
        if (amount < 1) amount = clicked.maxAmount();
        amounts.put(clicked, amount);
        mode = clicked;
        render();
    }

    private void render() {
        for (PlaceMode m : PlaceMode.values()) {
            ItemStack icon = new ItemStack(m.icon(), amounts.get(m));
            boolean selected = m == mode;
            icon.editMeta(meta -> {
                meta.displayName(BuildToolItem.legacy(m.displayName()));
                meta.setEnchantmentGlintOverride(selected);
            });
            inventory.setItem(m.slot(), icon);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
