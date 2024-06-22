package me.cat.catbox;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.papermc.paper.persistence.PersistentDataContainerView;
import me.cat.catbox.helpers.MiscHelper;
import me.cat.catbox.impl.managers.CatboxItemManager;
import me.cat.catbox.impl.managers.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class CatboxPlugin extends JavaPlugin implements Listener {

    private static CatboxPlugin INSTANCE;
    private static final String COMMAND_FALLBACK_PREFIX = "catbox";
    private static final Component UNABLE_USE_COMMAND_COMPONENT = Component.text("You can't use this!", NamedTextColor.RED);
    private CatboxItemManager catboxItemManager;
    private CooldownManager cooldownManager;
    private boolean abilitiesDisabled = false;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.cooldownManager = new CooldownManager();
        this.catboxItemManager = new CatboxItemManager(this, cooldownManager);

        registerCommands();
        registerEvents(
                this,
                cooldownManager
        );
    }

    private void registerCommands() {
        this.getServer().getCommandMap().register(COMMAND_FALLBACK_PREFIX, new Command("disableAbilities") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;
                if (!sender.isOp()) {
                    player.sendMessage(UNABLE_USE_COMMAND_COMPONENT);
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
                    player.sendMessage(UNABLE_USE_COMMAND_COMPONENT);
                    return false;
                }

                if (args.length == 1) {
                    Player targetPlayer = Bukkit.getPlayerExact(args[0]);
                    if (targetPlayer == null) {
                        player.sendMessage(Component.text("Invalid target player!", NamedTextColor.RED));
                    } else {
                        player.sendMessage(Component.text("Gave all items to player ", NamedTextColor.GREEN)
                                .append(targetPlayer.name().color(NamedTextColor.YELLOW))
                                .append(Component.text('!', NamedTextColor.GREEN)));

                        targetPlayer.getInventory().clear();
                        catboxItemManager.giveAllItems(targetPlayer);
                    }
                } else {
                    player.getInventory().clear();
                    catboxItemManager.giveAllItems(player);
                }
                return true;
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                return Bukkit.getServer().getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .toList();
            }
        });

        this.getServer().getCommandMap().register(COMMAND_FALLBACK_PREFIX, new Command("giveCustomItem") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;
                if (!sender.isOp()) {
                    player.sendMessage(UNABLE_USE_COMMAND_COMPONENT);
                    return false;
                }

                if (args.length != 1) {
                    player.sendMessage(Component.text("Usage -> /" + commandLabel + " <item_id>", NamedTextColor.RED));
                    return false;
                }

                String itemId = args[0];
                ItemStack itemToGive = catboxItemManager.getItemStackById(itemId);
                if (itemToGive == null) {
                    player.sendMessage(Component.text("Invalid <item_id>!", NamedTextColor.RED));
                    return false;
                } else {
                    player.sendMessage(MiscHelper.getGiveItemMessageComponent(itemId, player.getName()));
                    player.getInventory().addItem(itemToGive);
                }

                return true;
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                if (sender.isOp()) {
                    return catboxItemManager.getRegisteredItems()
                            .stream()
                            .map(item -> item.builder().itemId())
                            .toList();
                }
                return List.of();
            }
        });

        this.getServer().getCommandMap().register(COMMAND_FALLBACK_PREFIX, new Command("getPdc") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (!(sender instanceof Player player)) return false;
                if (!sender.isOp()) {
                    player.sendMessage(UNABLE_USE_COMMAND_COMPONENT);
                    return false;
                }

                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType() == Material.AIR) {
                    player.sendMessage(Component.text("You're not holding an item in your hand!", NamedTextColor.YELLOW));
                    return false;
                }

                PersistentDataContainerView pdc = itemInHand.getPersistentDataContainer();
                List<NamespacedKey> orderedKeys = Lists.newArrayList(pdc.getKeys());

                List<PersistentDataType<?, ?>> availableDataTypes = Lists.newArrayList();

                for (Field field : PersistentDataType.class.getDeclaredFields()) {
                    if (field.getType() == PersistentDataType.class) {
                        try {
                            field.setAccessible(true);

                            PersistentDataType<?, ?> dataType = (PersistentDataType<?, ?>) field.get(null);
                            availableDataTypes.add(dataType);
                        } catch (IllegalAccessException | IllegalArgumentException ex) {
                            player.sendMessage(Component.text("Couldn't fetch available data types",
                                    NamedTextColor.YELLOW));
                        }
                    }
                }

                Map<String, Object> mappedPdcKeysAndValues = Maps.newHashMap();
                for (NamespacedKey namespacedKey : orderedKeys) {
                    for (PersistentDataType<?, ?> dataType : availableDataTypes) {
                        try {
                            Object value = pdc.get(namespacedKey, dataType);
                            mappedPdcKeysAndValues.put(namespacedKey.getKey(), value);
                        } catch (IllegalArgumentException ex) {
                            Component mappingErrorComponent = Component.text(
                                    "Couldn't map key '" + namespacedKey.getKey() + '\'',
                                    NamedTextColor.YELLOW
                            ).append(Component.text(" -> (" + dataType.getComplexType().getSimpleName() + ')', NamedTextColor.GREEN));

                            player.sendMessage(mappingErrorComponent);
                        }
                    }
                }

                Component lineComponent = Component.text("--------------------------", NamedTextColor.GOLD);

                player.sendMessage(lineComponent);
                mappedPdcKeysAndValues.forEach(
                        (key, value) -> player.sendMessage(Component.text(key, NamedTextColor.GREEN)
                                .append(Component.text(" -> ", NamedTextColor.GOLD))
                                .append(Component.text(String.valueOf(value), NamedTextColor.YELLOW))));
                player.sendMessage(lineComponent);

                return true;
            }
        });
    }

    public void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public void setAbilitiesDisabled(boolean abilitiesDisabled) {
        this.abilitiesDisabled = abilitiesDisabled;
    }

    public static CatboxPlugin get() {
        return INSTANCE;
    }

    public CatboxItemManager getCatboxItemManager() {
        return catboxItemManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public boolean abilitiesDisabled() {
        return abilitiesDisabled;
    }
}
