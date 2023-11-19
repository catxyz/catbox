package me.cat.testplugin;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class TestPlugin extends JavaPlugin implements Listener {

    private static TestPlugin INSTANCE;
    private boolean abilitiesDisabled = false;

    @Override
    public void onEnable() {
        INSTANCE = this;

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new SkullShootListener(), this);

        this.getServer().getCommandMap().register("fallback", new Command("disableAbilities") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) {
                    Bukkit.getServer().getLogger().info("Can't do that from here");
                    return false;
                } else {
                    if (!abilitiesDisabled()) {
                        player.sendMessage(Component.text("Abilities disabled!", NamedTextColor.RED));
                        setAbilitiesDisabled(true);
                    } else {
                        player.sendMessage(Component.text("Abilities enabled!", NamedTextColor.GREEN));
                        setAbilitiesDisabled(false);
                    }
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

    public boolean abilitiesDisabled() {
        return abilitiesDisabled;
    }
}
