package org.spongebot.bot.rs.api;

// The client does not have an actual grounditem class, however it is convenient to have a wrapper like this as the
// scripts shouldn't really have to do anything with the coords if they don't actually need to
public class GroundItem {

    private Item item;

    private int localX;
    private int localY;

    public Item getItem() {
        return item;
    }

    public int getLocalX() {
        return localX;
    }

    public int getLocalY() {
        return localY;
    }

    public GroundItem(Item item, int localX, int localY) {
        this.item = item;
        this.localX = localX;
        this.localY = localY;
    }

    public boolean take() {
        return false;
    }

}
