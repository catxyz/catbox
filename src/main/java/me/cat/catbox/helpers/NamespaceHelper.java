package me.cat.catbox.helpers;

import me.cat.catbox.CatboxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class NamespaceHelper {

    @NotNull
    public static NamespacedKey newSelfPluginTag(String key) {
        return newPluginTag(CatboxPlugin.get(), key);
    }

    @NotNull
    public static NamespacedKey newCustomTag(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @NotNull
    public static NamespacedKey newMinecraftTag(String key) {
        return NamespacedKey.minecraft(key);
    }

    @NotNull
    public static NamespacedKey newPluginTag(Plugin plugin, String key) {
        return new NamespacedKey(plugin, key);
    }
}
