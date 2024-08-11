package me.cat.catbox.impl.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.cat.catbox.CatboxPlugin;
import me.cat.catbox.helpers.MiscHelper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import me.cat.catbox.impl.abstraction.sharedlisteners.ArmorStandManipulateListener;
import me.cat.catbox.impl.abstraction.sharedlisteners.FireworkDamageListener;
import me.cat.catbox.items.beta.CorrupterItem;
import me.cat.catbox.items.beta.MyPreciousItem;
import me.cat.catbox.items.beta.TimeShifterItem;
import me.cat.catbox.items.beta.portal.PortalItem;
import me.cat.catbox.items.stable.EverythingPotionItem;
import me.cat.catbox.items.stable.TestItem;
import me.cat.catbox.items.stable.TestificateSpawnerItem;
import me.cat.catbox.items.stable.amaceing.AmaceingItem;
import me.cat.catbox.items.stable.colorstaff.ColorStaffItem;
import me.cat.catbox.items.stable.explosivetoybow.ExplosiveToyBowItem;
import me.cat.catbox.items.stable.passengerenderpearl.PassengerEnderPearlItem;
import me.cat.catbox.items.stable.undeadbow.UndeadBowItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CatboxItemManager {

    private final CatboxPlugin plugin;
    private final List<CatboxItem> registeredItems;
    private final Map<String, ItemStack> mappedItemIdAndStack;

    public CatboxItemManager(CatboxPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;

        this.registeredItems = Lists.newArrayList();
        this.mappedItemIdAndStack = Maps.newHashMap();

        registerItems();
        mapItemIdAndStack();
        registerSharedListeners();

        plugin.registerEvents(new ItemUseListener(this, cooldownManager));
    }

    private void registerItems() {
        addItem(new AmaceingItem());
        addItem(new ColorStaffItem());
        addItem(new ExplosiveToyBowItem());
        addItem(new PassengerEnderPearlItem());
        addItem(new PortalItem());
        addItem(new UndeadBowItem());
        addItem(new CorrupterItem());
        addItem(new EverythingPotionItem());
        addItem(new MyPreciousItem());
        addItem(new TestificateSpawnerItem());
        addItem(new TestItem());
        addItem(new TimeShifterItem());
    }

    private void mapItemIdAndStack() {
        registeredItems.forEach(item -> {
            CatboxItemBuilder builder = item.builder();

            String itemId = builder.itemId();
            if (!mappedItemIdAndStack.containsKey(itemId)) {
                mappedItemIdAndStack.put(itemId, builder.toItemStack());
            }
        });
    }

    private void registerSharedListeners() {
        plugin.registerEvents(
                new ArmorStandManipulateListener(),
                new FireworkDamageListener()
        );
    }

    public ItemStack getItemStackById(String itemId) {
        if (mappedItemIdAndStack.containsKey(itemId)) {
            return mappedItemIdAndStack.get(itemId);
        }
        return null;
    }

    public void giveAllStableItems(Player player) {
        registeredItems.stream()
                .filter(item -> !item.builder().markedAsBeta())
                .forEach(item -> giveStableOrBeta(player, item.builder()));
    }

    public void giveAllBetaItems(Player player) {
        registeredItems.stream()
                .filter(item -> item.builder().markedAsBeta())
                .forEach(item -> giveStableOrBeta(player, item.builder()));
    }

    private void giveStableOrBeta(Player player, CatboxItemBuilder builder) {
        player.sendMessage(MiscHelper.getGiveItemMessageComponent(builder.itemId(), player.getName()));
        player.getInventory().addItem(builder.toItemStack());
    }

    public void addItem(CatboxItem item) {
        CatboxItemBuilder builder = item.builder();

        if (!isItemRegistered(item)) {
            registeredItems.add(item);

            item.loadAdditionalItemData();
            item.registerHookedSelfListeners(plugin);

            plugin.getLogger().info("-> '" + builder.itemId() + '\''
                    + (builder.markedAsBeta() ? " (beta)" : "")
                    + " registered!");
        }
    }

    public void removeItem(CatboxItem item) {
        registeredItems.remove(item);
        mappedItemIdAndStack.remove(item.builder().itemId());
    }

    public Optional<CatboxItem> getItemByMaterial(Material material) {
        return registeredItems.stream()
                .filter(item -> item.builder().material() == material)
                .findFirst();
    }

    public Optional<CatboxItem> getItemById(String itemId) {
        return registeredItems.stream()
                .filter(item -> Objects.equals(item.builder().itemId(), itemId))
                .findFirst();
    }

    public boolean isItemRegistered(CatboxItem otherItem) {
        return registeredItems.stream()
                .anyMatch(thisItem -> {
                    String thisItemId = thisItem.builder().itemId();
                    String otherItemId = otherItem.builder().itemId();

                    return Objects.equals(thisItemId, otherItemId);
                });
    }

    public List<CatboxItem> getRegisteredItems() {
        return registeredItems;
    }

    public Map<String, ItemStack> getMappedItemIdAndStack() {
        return mappedItemIdAndStack;
    }
}
