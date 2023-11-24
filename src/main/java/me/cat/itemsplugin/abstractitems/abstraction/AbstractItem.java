package me.cat.itemsplugin.abstractitems.abstraction;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.cat.itemsplugin.Helper;
import me.cat.itemsplugin.ItemsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        private Map<Enchantment, Integer> enchants = Maps.newHashMap();
        private Component displayName = Component.text(UUID.randomUUID().toString().replace("-", ""));
        private List<Component> lore = Lists.newArrayList();
        private boolean unbreakableItem = true;
        private List<ItemFlag> itemFlags = Lists.newArrayList();
        private Duration useCooldown = Duration.ofSeconds(1L);
        private boolean showCooldownLoreLine = true;

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

        public <T, Z> AbstractItemBuilder addData(NamespacedKey key, PersistentDataType<T, Z> dataType, Z value) {
            this.persistentDataContainer.set(key, dataType, value);
            return this;
        }

        public AbstractItemBuilder setEnchants(Map<Enchantment, Integer> enchants) {
            this.enchants = enchants;
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

        public AbstractItemBuilder setItemFlags(List<ItemFlag> itemFlags) {
            this.itemFlags = itemFlags;
            return this;
        }

        public AbstractItemBuilder setUseCooldown(Duration useCooldown) {
            this.useCooldown = useCooldown;
            return this;
        }

        public AbstractItemBuilder setShowCooldownLoreLine(boolean showCooldownLoreLine) {
            this.showCooldownLoreLine = showCooldownLoreLine;
            return this;
        }

        public PersistentDataContainer getPersistentDataContainer() {
            return persistentDataContainer;
        }

        public ItemStack toItemStack() {
            enchants.forEach((enchant, level) -> itemMeta.addEnchant(enchant, level, true));

            itemMeta.displayName(displayName.decoration(TextDecoration.ITALIC, false));

            List<Component> additionalLore = Lists.newArrayList(lore);
            if (showCooldownLoreLine) {
                additionalLore.add(Component.empty());
                additionalLore.add(Component.text("Cooldown -> ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(Helper.formatDuration(useCooldown), NamedTextColor.YELLOW)));
            }
            additionalLore.add(Component.empty());

            itemMeta.lore(additionalLore.stream()
                    .map(component -> {
                        if (component.hasDecoration(TextDecoration.BOLD)) {
                            return component.decoration(TextDecoration.ITALIC, false);
                        }
                        if (component.hasDecoration(TextDecoration.ITALIC)) {
                            return component;
                        }
                        return component.decoration(TextDecoration.ITALIC, false);
                    })
                    .collect(Collectors.toList()));

            itemMeta.getPersistentDataContainer()
                    .set(IS_CUSTOM_ITEM_TAG, PersistentDataType.BOOLEAN, true);
            itemMeta.getPersistentDataContainer()
                    .set(CUSTOM_ITEM_ID_TAG, PersistentDataType.STRING, itemId);

            itemMeta.setUnbreakable(unbreakableItem);

            List<ItemFlag> alreadyProvidedItemFlags = Lists.newArrayList(itemFlags);
            alreadyProvidedItemFlags.add(ItemFlag.HIDE_UNBREAKABLE);
            alreadyProvidedItemFlags.forEach(itemFlag -> itemMeta.addItemFlags(itemFlag));

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        public ItemMeta getItemMeta() {
            return itemMeta;
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

        public Map<Enchantment, Integer> getEnchants() {
            return enchants;
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

        public List<ItemFlag> getItemFlags() {
            return itemFlags;
        }

        public Duration getUseCooldown() {
            return useCooldown;
        }

        public boolean showCooldownLoreLine() {
            return showCooldownLoreLine;
        }
    }

    public abstract void useItemInteraction(PlayerInteractEvent event);

    public AbstractItemBuilder getBuilder() {
        return builder;
    }
}
