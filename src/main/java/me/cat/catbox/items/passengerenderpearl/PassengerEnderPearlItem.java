package me.cat.catbox.items.passengerenderpearl;

import me.cat.catbox.helpers.NamespaceHelper;
import me.cat.catbox.impl.abstraction.item.CatboxItem;
import me.cat.catbox.impl.abstraction.item.CatboxItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PassengerEnderPearlItem extends CatboxItem implements Listener {

    protected static final String PASSENGER_ENDER_PEARL_ITEM_ID = "passenger_ender_pearl";
    protected static final NamespacedKey IS_PASSENGER_ENDER_PEARL_TAG = NamespaceHelper.newSelfPluginTag("is_passenger_ender_pearl");
    protected static final int DESPAWN_SECONDS = 30;
    private final PassengerEnderPearlListener passengerEnderPearlListener;

    public PassengerEnderPearlItem() {
        super(
                new CatboxItemBuilder()
                        .useActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK
                        ))
                        .itemId(PASSENGER_ENDER_PEARL_ITEM_ID)
                        .material(Material.ENDER_PEARL)
                        .insertData(IS_PASSENGER_ENDER_PEARL_TAG, PersistentDataType.BOOLEAN, true)
                        .displayName(Component.text("Passenger Ender Pearl", NamedTextColor.YELLOW))
                        .lore(List.of(
                                Component.empty(),
                                Component.text("???", NamedTextColor.GRAY)
                        ))
        );

        this.passengerEnderPearlListener = new PassengerEnderPearlListener(this);
    }

    @Override
    public void loadAdditionalItemData() {
        hookSelfListener(passengerEnderPearlListener);
        passengerEnderPearlListener.runFallbackOffHandEnderPearlChecker();
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
    }
}
