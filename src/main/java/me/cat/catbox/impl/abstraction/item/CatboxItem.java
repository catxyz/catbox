package me.cat.catbox.impl.abstraction.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import me.cat.catbox.CatboxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public abstract class CatboxItem implements Listener {

    public static final NamespacedKey IS_CUSTOM_ITEM_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "is_custom_item",
            CatboxPlugin.get()
    ));
    public static final NamespacedKey CUSTOM_ITEM_ID_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "custom_item_id",
            CatboxPlugin.get()
    ));
    private final CatboxItemBuilder builder;
    private final Set<Listener> hookedSelfListeners;

    public CatboxItem(CatboxItemBuilder builder) {
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

    public void registerHookedSelfListeners(CatboxPlugin plugin) {
        plugin.registerEvents(hookedSelfListeners.toArray(new Listener[0]));
    }

    public ItemStack getSelfItemStack() {
        return builder().toItemStack();
    }

    public CatboxItemBuilder builder() {
        return builder;
    }

    public Set<Listener> getHookedSelfListeners() {
        return hookedSelfListeners;
    }
}
