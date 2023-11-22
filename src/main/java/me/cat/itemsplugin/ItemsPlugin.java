package me.cat.itemsplugin;

import me.cat.itemsplugin.abstractitems.manager.AbstractItemManager;
import me.cat.itemsplugin.abstractitems.manager.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemsPlugin extends JavaPlugin implements Listener {

    private static ItemsPlugin INSTANCE;
    private static final String COMMAND_FALLBACK_PREFIX = "abstract";
    private static final Component COMMAND_MISSING_PERMISSION_COMPONENT = Component.text("Missing permission!", NamedTextColor.RED);
    private AbstractItemManager abstractItemManager;
    private CooldownManager cooldownManager;
    private boolean abilitiesDisabled = false;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.cooldownManager = new CooldownManager();
        this.abstractItemManager = new AbstractItemManager(this);
        registerCommands();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void registerCommands() {
        this.getServer().getCommandMap().register(COMMAND_FALLBACK_PREFIX, new Command("disableAbilities") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;
                if (!sender.isOp()) {
                    player.sendMessage(COMMAND_MISSING_PERMISSION_COMPONENT);
                    return false;
                }

                if (!abilitiesDisabled()) {
                    player.sendMessage(Component.text("Abilities disabled!", NamedTextColor.RED));
                    setAbilitiesDisabled(true);
                } else {
                    player.sendMessage(Component.text("Abilities enabled!", NamedTextColor.GREEN));
                    setAbilitiesDisabled(false);
                }

                return true;
            }
        });

        this.getServer().getCommandMap().register(COMMAND_FALLBACK_PREFIX, new Command("giveAllCustomItems") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;
                if (!sender.isOp()) {
                    player.sendMessage(COMMAND_MISSING_PERMISSION_COMPONENT);
                    return false;
                }

                abstractItemManager.giveAllItems(player);
                return true;
            }
        });

        this.getServer().getCommandMap().register(COMMAND_FALLBACK_PREFIX, new Command("giveCustomItem") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;
                if (!sender.isOp()) {
                    player.sendMessage(COMMAND_MISSING_PERMISSION_COMPONENT);
                    return false;
                }

                if (args.length != 1) {
                    player.sendMessage(Component.text("Usage -> /" + commandLabel + " <item_id>", NamedTextColor.RED));
                    return false;
                }

                String itemId = args[0];
                ItemStack itemToGive = abstractItemManager.getMappedItemIdAndStack().get(itemId);
                if (itemToGive == null) {
                    player.sendMessage(Component.text("Invalid item id!", NamedTextColor.RED));
                    return false;
                } else {
                    player.sendMessage(Helper.getPlayGiveItemMessageComponent(itemId, player.getName()));
                    player.getInventory().addItem(itemToGive);
                }

                return true;
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                if (sender.isOp()) {
                    return abstractItemManager.getRegisteredItems()
                            .stream()
                            .map(item -> item.getBuilder().getItemId())
                            .toList();
                }
                return List.of();
            }
        });
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getDrops().clear();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework) {
            event.setCancelled(true);
        }
    }

    public void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void setAbilitiesDisabled(boolean abilitiesDisabled) {
        this.abilitiesDisabled = abilitiesDisabled;
    }

    public static ItemsPlugin getInstance() {
        return INSTANCE;
    }

    public AbstractItemManager getAbstractItemManager() {
        return abstractItemManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public boolean abilitiesDisabled() {
        return abilitiesDisabled;
    }
}
