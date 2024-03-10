package me.cat.itemsplugin.abstractitems.items;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.common.base.Preconditions;
import me.cat.itemsplugin.ItemsPlugin;
import me.cat.itemsplugin.abstractitems.abstraction.AbstractItem;
import me.cat.itemsplugin.helpers.Helper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CorrupterItem extends AbstractItem implements Listener {

    private static final List<Material> CORRUPTED_MATERIALS;
    private static final NamespacedKey CORRUPTER_TRIDENT_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "corrupter_trident",
            ItemsPlugin.getInstance()
    ));
    private static int currentCorruptionRadius = 3;

    static {
        CORRUPTED_MATERIALS = Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(material -> MaterialTags.STAINED_GLASS.isTagged(material) || MaterialSetTag.ICE.isTagged(material))
                .toList();
    }

    public CorrupterItem() {
        super(
                new AbstractItemBuilder()
                        .setUseActions(List.of(
                                Action.RIGHT_CLICK_AIR,
                                Action.RIGHT_CLICK_BLOCK,
                                Action.LEFT_CLICK_AIR,
                                Action.LEFT_CLICK_BLOCK
                        ))
                        .setItemId("corrupter")
                        .setUseCooldown(Duration.ZERO)
                        .setMaterial(Material.TRIDENT)
                        .setEnchants(Map.of(
                                Enchantment.LOYALTY, 5
                        ))
                        .addData(CORRUPTER_TRIDENT_TAG, PersistentDataType.BOOLEAN, true)
                        .setItemFlags(List.of(
                                ItemFlag.HIDE_ENCHANTS,
                                ItemFlag.HIDE_ATTRIBUTES
                        ))
                        .setDisplayName(Component.text("Corrupter", NamedTextColor.DARK_PURPLE))
                        .setLore(generateDynamicItemLore(currentCorruptionRadius))
        );
    }

    @Override
    public void useItemInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            PlayerInventory playerInventory = player.getInventory();
            if (player.isSneaking()) {
                if (currentCorruptionRadius == 1) {
                    player.sendMessage(getPlayInvalidRadiusMessageComponent());
                    return;
                }
                currentCorruptionRadius--;
                player.sendMessage(getPlayRadiusUpdateMessageComponent(currentCorruptionRadius));
            } else {
                currentCorruptionRadius++;
                player.sendMessage(getPlayRadiusUpdateMessageComponent(currentCorruptionRadius));
            }

            ItemStack updatedItem = getBuilder()
                    .setLore(generateDynamicItemLore(currentCorruptionRadius))
                    .toItemStack();
            playerInventory.setItemInMainHand(updatedItem);
        }
    }

    @EventHandler
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        Player player = event.getPlayer();

        ItemStack trident = event.getItemStack();
        if (trident.getItemMeta().getPersistentDataContainer().has(CORRUPTER_TRIDENT_TAG)) {
            Trident tridentEntity = (Trident) event.getProjectile();

            Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (muteLightningTask) -> {
                if (!tridentEntity.isValid()) {
                    muteLightningTask.cancel();
                }
                player.stopSound(SoundCategory.WEATHER);
            }, 0L, 1L);
            Bukkit.getServer().getScheduler().runTaskTimer(ItemsPlugin.getInstance(), (task) -> {
                try {
                    if (tridentEntity.isOnGround()) {
                        Helper.createSurfaceLayer(
                                tridentEntity.getWorld(),
                                tridentEntity.getLocation(),
                                currentCorruptionRadius,
                                CORRUPTED_MATERIALS,
                                true,
                                affectedBlocks ->
                                        affectedBlocks.forEach(
                                                block -> tridentEntity.getWorld().strikeLightningEffect(block.getLocation()))
                        );
                        task.cancel();
                    }
                } catch (Exception ex) {
                    player.sendMessage(Component.text("Something went wrong!", NamedTextColor.RED));
                    task.cancel();
                }
            }, 0L, 1L);
        }
    }

    private static List<Component> generateDynamicItemLore(int providedCorruptionRadius) {
        return List.of(
                Component.empty(),
                Component.text("A totally safe tool!", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Radius: ", NamedTextColor.YELLOW)
                        .append(Component.text(providedCorruptionRadius, NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("Left-click to increase radius!", NamedTextColor.GREEN),
                Component.text("Sneak + Left-click to decrease radius!", NamedTextColor.RED)
        );
    }

    private TextComponent getPlayRadiusUpdateMessageComponent(int providedCorruptionRadius) {
        return Component.newline()
                .append(Component.text("Radius updated to ", NamedTextColor.GREEN)
                        .append(Component.text(providedCorruptionRadius, NamedTextColor.YELLOW))
                        .appendNewline());
    }

    private TextComponent getPlayInvalidRadiusMessageComponent() {
        return Component.newline()
                .append(Component.text("Radius can't be ", NamedTextColor.RED)
                        .append(Component.text("<1", NamedTextColor.YELLOW))
                        .append(Component.text('!', NamedTextColor.RED)
                                .appendNewline()));
    }
}
