package net.azisaba.buildtool.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class BuildToolItem {

    /** Kept as is so that tools given on 1.15.2 are still recognised. */
    public static final NamespacedKey KEY = NamespacedKey.minecraft("build_tool_item");
    public static final Material MATERIAL = Material.BLAZE_ROD;

    private static final Component NAME = legacy("&e&lBuild&f&l-&b&lTool");
    private static final List<Component> LORE = List.of(
            legacy("&fプレイヤー用建築補助ツール"),
            legacy("&f手持ちのブロックを消費して、ブロック設置します。"),
            legacy("&fシフト左で設定を開きます。")
    );

    private BuildToolItem() {}

    public static ItemStack create(int modelData) {
        ItemStack item = new ItemStack(MATERIAL);
        item.editMeta(meta -> apply(meta, modelData));
        return item;
    }

    @SuppressWarnings("deprecation") // the resource pack still selects the model by the legacy custom model data
    private static void apply(ItemMeta meta, int modelData) {
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, 1);
        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        meta.setCustomModelData(modelData);
        meta.displayName(NAME);
        meta.lore(LORE);
    }

    /**
     * Called for every interaction on the server, so the material is checked first and the PDC is read
     * through the read-only view (no ItemMeta copy). The key is accepted with any value type: tools moved
     * through other plugins' storage may have had their int turned into another type.
     */
    public static boolean isBuildTool(ItemStack item) {
        return item != null && item.getType() == MATERIAL && item.getPersistentDataContainer().has(KEY);
    }

    public static Component legacy(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }
}
