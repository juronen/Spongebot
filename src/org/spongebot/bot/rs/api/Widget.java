package org.spongebot.bot.rs.api;

import org.spongebot.bot.accessors.IWidget;

public class Widget {

    IWidget widget;

    public Widget(IWidget widget) {
        this.widget = widget;
    }

    public int[] getItems() {
        return widget.getItems();
    }

    public int getID() {
        return widget.getID();
    }

    public int[] getStackSizes() {
        return widget.getStackSizes();
    }

    public String[] getActions() {
        return widget.getActions();
    }

    public String getText() {
        return widget.getText();
    }
}
