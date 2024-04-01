package me.cat.toybox.impl.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.helpers.Helper;
import me.cat.toybox.impl.abstraction.item.ToyboxItem;
import me.cat.toybox.impl.abstraction.item.ToyboxItemBuilder;
import me.cat.toybox.impl.abstraction.sharedlisteners.ArmorStandManipulateListener;
import me.cat.toybox.impl.abstraction.sharedlisteners.FireworkDamageListener;
import me.cat.toybox.items.*;
import me.cat.toybox.items.explosivetoybow.ExplosiveToyBowItem;
import me.cat.toybox.items.portal.PortalItem;
import me.cat.toybox.items.undeadbow.UndeadBowItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ToyboxItemManager {

    private final ToyboxPlugin plugin;
    private final List<ToyboxItem> registeredItems;
    private final Map<String, ItemStack> mappedItemIdAndStack;

    public ToyboxItemManager(ToyboxPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;

        this.registeredItems = Lists.newArrayList();
        this.mappedItemIdAndStack = Maps.newHashMap();

        registerItems();
        mapItemIdAndStack();
        registerSharedListeners();

        plugin.registerEvents(new ItemUseListener(this, cooldownManager));
    }

    private void registerItems() {
        addItem(new ExplosiveToyBowItem());
        addItem(new PortalItem());
        addItem(new UndeadBowItem());
        addItem(new ColorStaffItem());
        addItem(new CorrupterItem());
        addItem(new MyPreciousItem());
        addItem(new PassengerEnderPearlItem());
        addItem(new TestificateSpawnerItem());
        addItem(new TestItem());
        addItem(new TimeShifterItem());
    }

    private void mapItemIdAndStack() {
        registeredItems.forEach(item -> {
            ToyboxItemBuilder builder = item.builder();

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

    public void giveAllItems(Player player) {
        mappedItemIdAndStack.forEach((itemId, itemStack) -> {
            player.sendMessage(Helper.getGiveItemMessageComponent(itemId, player.getName()));
            player.getInventory().addItem(itemStack);
        });
    }

    public void addItem(ToyboxItem item) {
        if (!isItemRegistered(item)) {
            registeredItems.add(item);

            item.loadAdditionalItemData();
            item.registerHookedSelfListeners(plugin);

            plugin.getLogger().info("-> '" + item.builder().itemId() + "' registered!");
        }
    }

    public void removeItem(ToyboxItem item) {
        registeredItems.remove(item);
        mappedItemIdAndStack.remove(item.builder().itemId());
    }

    public Optional<ToyboxItem> getItemByMaterial(Material material) {
        return registeredItems.stream()
                .filter(item -> item.builder().material() == material)
                .findFirst();
    }

    public Optional<ToyboxItem> getItemById(String itemId) {
        return registeredItems.stream()
                .filter(item -> Objects.equals(item.builder().itemId(), itemId))
                .findFirst();
    }

    public boolean isItemRegistered(ToyboxItem otherItem) {
        return registeredItems.stream()
                .anyMatch(thisItem -> {
                    String thisItemId = thisItem.builder().itemId();
                    String otherItemId = otherItem.builder().itemId();

                    return Objects.equals(thisItemId, otherItemId);
                });
    }

    public List<ToyboxItem> getRegisteredItems() {
        return registeredItems;
    }

    public Map<String, ItemStack> getMappedItemIdAndStack() {
        return mappedItemIdAndStack;
    }
}
