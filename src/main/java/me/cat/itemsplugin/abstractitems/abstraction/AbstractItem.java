package me.cat.itemsplugin.abstractitems.abstraction;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import me.cat.itemsplugin.ItemsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public abstract class AbstractItem {

    public static final NamespacedKey IS_CUSTOM_ITEM_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "is_custom_item",
            ItemsPlugin.getInstance()
    ));
    public static final NamespacedKey CUSTOM_ITEM_ID_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "custom_item_id",
            ItemsPlugin.getInstance()
    ));
    private final AbstractItemBuilder builder;

    public AbstractItem(AbstractItemBuilder builder) {
        this.builder = builder;
    }

    public static class AbstractItemBuilder {

        private ItemStack itemStack;
        private ItemMeta itemMeta;
        private PersistentDataContainer persistentDataContainer;
        private List<Action> useActions = Lists.newArrayList();
        private String itemId = UUID.randomUUID().toString().replace("-", "");
        private Material material = Material.STONE;
        private Component displayName = Component.text(UUID.randomUUID().toString().replace("-", ""));
        private List<Component> lore = Lists.newArrayList();
        private boolean unbreakableItem = true;

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
            this.itemStack = new ItemStack(material);
            this.itemMeta = itemStack.getItemMeta();
            this.persistentDataContainer = itemMeta.getPersistentDataContainer();
            return this;
        }

        public <T, Z> AbstractItemBuilder addData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
            this.persistentDataContainer.set(key, type, value);
            return this;
        }

        public AbstractItemBuilder setDisplayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public AbstractItemBuilder setLore(List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public AbstractItemBuilder setUnbreakableItem(boolean unbreakableItem) {
            this.unbreakableItem = unbreakableItem;
            return this;
        }

        public PersistentDataContainer getPersistentDataContainer() {
            return persistentDataContainer;
        }

        public ItemStack toItemStack() {
            itemMeta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
            itemMeta.lore(lore.stream()
                    .map(component -> component.decoration(TextDecoration.ITALIC, false))
                    .toList());

            itemMeta.getPersistentDataContainer()
                    .set(IS_CUSTOM_ITEM_TAG, PersistentDataType.BOOLEAN, true);
            itemMeta.getPersistentDataContainer()
                    .set(CUSTOM_ITEM_ID_TAG, PersistentDataType.STRING, itemId);

            itemMeta.setUnbreakable(unbreakableItem);

            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
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

        public Component getDisplayName() {
            return displayName;
        }

        public List<Component> getLore() {
            return lore;
        }

        public boolean isUnbreakableItem() {
            return unbreakableItem;
        }
    }

    public abstract void useItemInteraction(PlayerInteractEvent event);

    public AbstractItemBuilder getBuilder() {
        return builder;
    }
}
