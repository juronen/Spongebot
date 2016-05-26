package org.spongebot.bot.rs.api;

import org.spongebot.bot.accessors.IItem;
import org.spongebot.bot.rs.Context;

public class Item {

    private int id;
    private int amount;

    public Item(int id) {
        this.id = id;
    }

    public Item(IItem item) { // Need this for ground items
        this.id = item.getID();
        this.amount = item.getStackSize();
    }

    public Item(int id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getName() {
        return Context.client.getItemDefinition(id).getName();
    }

    public String[] getInventoryActions() {
        return Context.client.getItemDefinition(id).getInventoryActions();
    }

    public String[] getGroundActions() {
        return Context.client.getItemDefinition(id).getGroundActions();
    }

}
