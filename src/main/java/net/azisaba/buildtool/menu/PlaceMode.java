package net.azisaba.buildtool.menu;

import org.bukkit.Material;

public enum PlaceMode {
    /** A line of blocks away from the clicked face. */
    LINE(0, Material.OAK_LOG, "&aライン型設置", 64),
    /** A square on the clicked face (amount = side length). */
    SQUARE(2, Material.STONE, "&a正方形設置", 4);

    private final int slot;
    private final Material icon;
    private final String name;
    private final int maxAmount;

    PlaceMode(int slot, Material icon, String name, int maxAmount) {
        this.slot = slot;
        this.icon = icon;
        this.name = name;
        this.maxAmount = maxAmount;
    }

    public int slot() { return slot; }
    public Material icon() { return icon; }
    public String displayName() { return name; }
    public int maxAmount() { return maxAmount; }

    public static PlaceMode fromSlot(int slot) {
        for (PlaceMode mode : values()) {
            if (mode.slot == slot) return mode;
        }
        return null;
    }
}
