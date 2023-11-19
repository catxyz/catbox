package me.cat.testplugin;

import me.cat.testplugin.abstractitems.manager.AbstractItemManager;
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

public class TestPlugin extends JavaPlugin implements Listener {

    private static TestPlugin INSTANCE;
    private AbstractItemManager abstractItemManager;
    private boolean abilitiesDisabled = false;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.abstractItemManager = new AbstractItemManager(this);
        registerCommands();

        Bukkit.getPluginManager().registerEvents(this, this);
        //Bukkit.getPluginManager().registerEvents(new SkullShootListener(), this);
    }

    private void registerCommands() {
        this.getServer().getCommandMap().register("abstract", new Command("disableAbilities") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;

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

        this.getServer().getCommandMap().register("abstract", new Command("giveCustomItem") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;

                if (args.length > 1) {
                    player.sendMessage(Component.text("Usage -> /" + commandLabel + " <item_id>", NamedTextColor.RED));
                    return false;
                }

                ItemStack itemToGive = abstractItemManager.getMappedItemIdAndStack().get(args[0]);
                if (itemToGive == null) {
                    player.sendMessage(Component.text("Invalid item id!", NamedTextColor.RED));
                    return false;
                } else {
                    player.getInventory().addItem(itemToGive);
                }

                return true;
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

    public void setAbilitiesDisabled(boolean abilitiesDisabled) {
        this.abilitiesDisabled = abilitiesDisabled;
    }

    public static TestPlugin getInstance() {
        return INSTANCE;
    }

    public AbstractItemManager getAbstractItemManager() {
        return abstractItemManager;
    }

    public boolean abilitiesDisabled() {
        return abilitiesDisabled;
    }
}
