package me.cat.testplugin.abstractitems.abstraction;

import com.google.common.base.Preconditions;
import me.cat.testplugin.TestPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public abstract class AbstractItem {

    public static final NamespacedKey IS_CUSTOM_ITEM_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "is_custom_item",
            TestPlugin.getInstance()
    ));
    public static final NamespacedKey CUSTOM_ITEM_ID_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "custom_item_id",
            TestPlugin.getInstance()
    ));
    private final AbstractItemBuilder builder;

    public AbstractItem(AbstractItemBuilder builder) {
        this.builder = builder;
    }

    public static class AbstractItemBuilder {

        private List<Action> useActions;
        private String itemId;
        private Material material;

        public AbstractItemBuilder setUseActions(List<Action> useActions) {
            this.useActions = useActions;
            return this;
        }

        public AbstractItemBuilder setItemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public AbstractItemBuilder setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public List<Action> getUseActions() {
            return useActions;
        }

        public String getItemId() {
            return itemId;
        }

        public Material getMaterial() {
            return material;
        }
    }

    public abstract void useItemInteraction(PlayerInteractEvent event);

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(builder.getMaterial());

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer()
                .set(IS_CUSTOM_ITEM_TAG, PersistentDataType.BOOLEAN, true);
        itemMeta.getPersistentDataContainer()
                .set(CUSTOM_ITEM_ID_TAG, PersistentDataType.STRING, builder.getItemId());

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public AbstractItemBuilder getBuilder() {
        return builder;
    }
}
