package me.cat.catbox.impl.abstraction.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.cat.catbox.helpers.MiscHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
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

public class CatboxItemBuilder {

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
    private Duration useCooldown = Duration.ZERO;
    private boolean showCooldownLoreLine = true;
    private boolean cancelUseInteraction = false;

    public CatboxItemBuilder useActions(List<Action> useActions) {
        this.useActions = useActions;
        return this;
    }

    public CatboxItemBuilder itemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public CatboxItemBuilder material(Material material) {
        this.material = material;
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        this.persistentDataContainer = itemMeta.getPersistentDataContainer();
        return this;
    }

    public <T, V> CatboxItemBuilder insertData(NamespacedKey key, PersistentDataType<T, V> dataType, V value) {
        this.persistentDataContainer.set(key, dataType, value);
        return this;
    }

    public CatboxItemBuilder enchants(Map<Enchantment, Integer> enchants) {
        this.enchants = enchants;
        return this;
    }

    public CatboxItemBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public CatboxItemBuilder lore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    public CatboxItemBuilder unbreakable(boolean unbreakableItem) {
        this.unbreakableItem = unbreakableItem;
        return this;
    }

    public CatboxItemBuilder itemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags;
        return this;
    }

    public CatboxItemBuilder useCooldown(Duration useCooldown) {
        this.useCooldown = useCooldown;
        return this;
    }

    public CatboxItemBuilder showCooldownLoreLine(boolean showCooldownLoreLine) {
        this.showCooldownLoreLine = showCooldownLoreLine;
        return this;
    }

    public CatboxItemBuilder cancelUseInteraction(boolean cancelUseInteraction) {
        this.cancelUseInteraction = cancelUseInteraction;
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
                    .append(Component.text(MiscHelper.formatDuration(useCooldown), NamedTextColor.YELLOW)));
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
                .set(CatboxItem.IS_CUSTOM_ITEM_TAG, PersistentDataType.BOOLEAN, true);
        itemMeta.getPersistentDataContainer()
                .set(CatboxItem.CUSTOM_ITEM_ID_TAG, PersistentDataType.STRING, itemId);

        itemMeta.setUnbreakable(unbreakableItem);

        List<ItemFlag> alreadyProvidedItemFlags = Lists.newArrayList(itemFlags);
        alreadyProvidedItemFlags.add(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(alreadyProvidedItemFlags.toArray(new ItemFlag[0]));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemMeta itemMeta() {
        return itemMeta;
    }

    public List<Action> storedUseActions() {
        return useActions;
    }

    public String itemId() {
        return itemId;
    }

    public Material material() {
        return material;
    }

    public Map<Enchantment, Integer> storedEnchants() {
        return enchants;
    }

    public Component displayName() {
        return displayName;
    }

    public List<Component> storedLore() {
        return lore;
    }

    public boolean unbreakable() {
        return unbreakableItem;
    }

    public List<ItemFlag> storedItemFlags() {
        return itemFlags;
    }

    public Duration useCooldown() {
        return useCooldown;
    }

    public boolean showCooldownLoreLine() {
        return showCooldownLoreLine;
    }

    public boolean shouldCancelUseInteraction() {
        return cancelUseInteraction;
    }
}
