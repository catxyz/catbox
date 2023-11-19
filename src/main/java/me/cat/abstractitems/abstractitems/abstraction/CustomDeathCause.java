package me.cat.abstractitems.abstractitems.abstraction;

public enum CustomDeathCause {
    CUSTOM_ITEM("custom_item", "Custom Item"),
    NATURAL("natural_death", "Natural Death");

    private final String id;
    private final String friendlyName;

    CustomDeathCause(String id, String friendlyName) {
        this.id = id;
        this.friendlyName = friendlyName;
    }

    public String getId() {
        return id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
