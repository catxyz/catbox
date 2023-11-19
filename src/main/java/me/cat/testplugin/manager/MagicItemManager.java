package me.cat.testplugin.manager;

import me.cat.testplugin.abstraction.AbstractMagicItem;

import java.util.List;
import java.util.Objects;

public class MagicItemManager {

    private List<AbstractMagicItem> registeredMagicItems;

    public boolean isMagicItemRegistered(AbstractMagicItem otherMagicItem) {
        return registeredMagicItems.stream()
                .anyMatch(thisMagicItem ->
                        Objects.equals(thisMagicItem.getBuilder().getItemId(), otherMagicItem.getBuilder().getItemId()));
    }

    public List<AbstractMagicItem> getRegisteredMagicItems() {
        return registeredMagicItems;
    }
}
