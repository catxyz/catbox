package me.cat.toybox.impl.abstraction.interfaces;

import org.bukkit.entity.Entity;

public interface EntityLifetimeLooper {

    void defineLifetimeFor(Entity... entities);
}
