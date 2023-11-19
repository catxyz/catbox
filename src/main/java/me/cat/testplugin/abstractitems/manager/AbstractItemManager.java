package me.cat.testplugin.abstractitems.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.cat.testplugin.TestPlugin;
import me.cat.testplugin.abstractitems.abstraction.AbstractItem;
import me.cat.testplugin.abstractitems.items.TestItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AbstractItemManager {

    private final List<AbstractItem> registeredItems;
    private final Map<String, ItemStack> mappedItemIdAndStack;

    public AbstractItemManager(TestPlugin plugin) {
        this.registeredItems = Lists.newArrayList();
        this.mappedItemIdAndStack = Maps.newHashMap();

        registerAbstractItems();
        mapItemIdAndStack();

        plugin.getServer()
                .getPluginManager()
                .registerEvents(new ItemUseListener(this), plugin);
    }

    private void registerAbstractItems() {
        addAbstractItem(new TestItem());
    }

    private void mapItemIdAndStack() {
        registeredItems.forEach(item -> {
            AbstractItem.AbstractItemBuilder builder = item.getBuilder();

            if (!mappedItemIdAndStack.containsKey(builder.getItemId())) {
                mappedItemIdAndStack.put(builder.getItemId(), item.toItemStack());
            }
        });
    }

    public void addAbstractItem(AbstractItem abstractItem) {
        if (!isItemRegistered(abstractItem)) {
            registeredItems.add(abstractItem);
            Bukkit.getServer().getLogger().info("Abstract item '" + abstractItem.getBuilder().getItemId() + "' registered!");
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
                .anyMatch(item ->
                        Objects.equals(
                                item.getBuilder().getItemId(),
                                otherItem.getBuilder().getItemId()
                        )
                );
    }

    public List<AbstractItem> getRegisteredItems() {
        return registeredItems;
    }

    public Map<String, ItemStack> getMappedItemIdAndStack() {
        return mappedItemIdAndStack;
    }
}
