package me.cat.toybox.impl.abstraction.item;

import com.google.common.base.Preconditions;
import me.cat.toybox.ToyboxPlugin;
import org.bukkit.NamespacedKey;

public class SharedItemTags {

    public static final NamespacedKey CUSTOM_ARMOR_STAND_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "custom_armor_stand",
            ToyboxPlugin.get()
    ));
    public static final NamespacedKey USES_FIREWORKS_TAG = Preconditions.checkNotNull(NamespacedKey.fromString(
            "uses_fireworks",
            ToyboxPlugin.get()
    ));
}
