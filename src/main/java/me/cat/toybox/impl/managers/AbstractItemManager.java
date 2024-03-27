package me.cat.toybox.impl.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.cat.toybox.ToyboxPlugin;
import me.cat.toybox.impl.abstraction.AbstractItem;
import me.cat.toybox.impl.items.*;
import me.cat.toybox.helpers.Helper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AbstractItemManager {

    private final ToyboxPlugin plugin;
    private final List<AbstractItem> registeredItems;
    private final Map<String, ItemStack> mappedItemIdAndStack;

    public AbstractItemManager(ToyboxPlugin plugin) {
        this.plugin = plugin;

        this.registeredItems = Lists.newArrayList();
        this.mappedItemIdAndStack = Maps.newHashMap();

        registerAbstractItems();
        mapItemIdAndStack();

        plugin.getServer()
                .getPluginManager()
                .registerEvents(new ItemUseListener(this), plugin);
    }

    private void registerAbstractItems() {
        addAbstractItem(new ColorStaffItem());
        addAbstractItem(new CorrupterItem());
        addAbstractItem(new ExplosiveToyBowItem());
        addAbstractItem(new MyPreciousItem());
        addAbstractItem(new PassengerEnderPearlItem());
        addAbstractItem(new TestificateSpawnerItem());
        addAbstractItem(new TimeShifterItem());
        addAbstractItem(new UndeadBowItem());
    }

    private void mapItemIdAndStack() {
        registeredItems.forEach(item -> {
            AbstractItem.AbstractItemBuilder builder = item.getBuilder();

            if (!mappedItemIdAndStack.containsKey(builder.getItemId())) {
                mappedItemIdAndStack.put(builder.getItemId(), builder.toItemStack());
            }
        });
    }

    public ItemStack getItemStackById(String itemId) {
        if (mappedItemIdAndStack.containsKey(itemId)) {
            return mappedItemIdAndStack.get(itemId);
        }
        return new ItemStack(Material.STONE);
    }

    public void giveAllItems(Player player) {
        mappedItemIdAndStack.forEach((itemId, itemStack) -> {
            player.sendMessage(Helper.getPlayGiveItemMessageComponent(itemId, player.getName()));
            player.getInventory().addItem(itemStack);
        });
    }

    public void addAbstractItem(AbstractItem abstractItem) {
        if (!isItemRegistered(abstractItem)) {
            registeredItems.add(abstractItem);
            if (abstractItem instanceof Listener itemSelfListener) {
                plugin.registerEvents(itemSelfListener);
            }

            String itemId = abstractItem.getBuilder().getItemId();
            plugin.getLogger().info("-> '" + itemId + "' registered!");
        }
    }

    public void removeItem(AbstractItem item) {
        registeredItems.remove(item);
        mappedItemIdAndStack.remove(item.getBuilder().getItemId());
    }

    public Optional<AbstractItem> getItemByMaterial(Material material) {
        return registeredItems.stream()
                .filter(item -> item.getBuilder().getMaterial() == material)
                .findFirst();
    }

    public Optional<AbstractItem> getItemById(String itemId) {
        return registeredItems.stream()
                .filter(item -> Objects.equals(item.getBuilder().getItemId(), itemId))
                .findFirst();
    }

    public boolean isItemRegistered(AbstractItem otherItem) {
        return registeredItems.stream()
                .anyMatch(thisItem -> {
                    String thisItemId = thisItem.getBuilder().getItemId();
                    String otherItemId = otherItem.getBuilder().getItemId();

                    return Objects.equals(thisItemId, otherItemId);
                });
    }

    public List<AbstractItem> getRegisteredItems() {
        return registeredItems;
    }

    public Map<String, ItemStack> getMappedItemIdAndStack() {
        return mappedItemIdAndStack;
    }
}
