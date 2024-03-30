package me.cat.toybox.impl.abstraction.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import me.cat.toybox.ToyboxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public abstract class ToyboxItem implements Listener {

    public static final NamespacedKey IS_CUSTOM_ITEM_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "is_custom_item",
            ToyboxPlugin.get()
    ));
    public static final NamespacedKey CUSTOM_ITEM_ID_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "custom_item_id",
            ToyboxPlugin.get()
    ));
    private final ToyboxItemBuilder builder;
    private final Set<Listener> hookedSelfListeners;

    public ToyboxItem(ToyboxItemBuilder builder) {
        this.builder = builder;
        this.hookedSelfListeners = Sets.newHashSet();

        hookSelfListener(this);
    }

    public abstract void onUse(PlayerInteractEvent event);

    public void loadAdditionalItemData() {
    }

    public void hookSelfListener(Listener listener) {
        hookedSelfListeners.add(listener);
    }

    public void registerHookedSelfListeners(ToyboxPlugin plugin) {
        plugin.registerEvents(hookedSelfListeners.toArray(new Listener[0]));
    }

    public ItemStack getSelfItemStack() {
        return builder().toItemStack();
    }

    public ToyboxItemBuilder builder() {
        return builder;
    }

    public Set<Listener> getHookedSelfListeners() {
        return hookedSelfListeners;
    }
}
