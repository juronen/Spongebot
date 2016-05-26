package org.spongebot.bot.rs.api.tools;

import org.spongebot.bot.accessors.IWidget;
import org.spongebot.bot.rs.Constants;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.Widget;
import org.spongebot.loader.updater.Storage;

import java.util.ArrayList;
import java.util.List;

public class Bank {

    public static IWidget bankWidget = null;

    private static void sendAction(Widget widget, String action, int index) {
        String[] actions = widget.getActions();
        if (actions == null)
            System.out.println("[Bank] Widget " + widget + " " + (widget.getID() >> 16) + " " + (widget.getID() & 0xFFFF) + " " + " has no actions!");
        for (int i = 0; i < Math.min(10, actions.length); i++) {
            if (action.equals(actions[i])) {
                String[] codes = Storage.interfacePackets[i];
                Context.client.putOpcode(Storage.interfaceOpcodes[i]);
                Context.client.putStream(codes[0], widget.getID());
                Context.client.putStream(codes[1], index);
                return;
            }
        }
        System.out.println("Bank.sendAction - Action " + action + " was not found!");
    }

    public static void depositAll() {
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, Constants.INVENTORY_NORMAL_CHILD);
        int[] slots = inventory.getItems();
        List<Integer> ids = new ArrayList();
        for (int i = 0; i < 28; i++) {
            if (slots[i] > 0) {
                if (ids.contains(slots[i]))
                    continue;
                sendAction(new Widget(bankWidget), "Deposit-All", i);
                ids.add(slots[i]);
            }
        }
    }

    public static void depositAll(int id) {
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, Constants.INVENTORY_NORMAL_CHILD);
        int[] slots = inventory.getItems();
        for (int i = 0; i < 28; i++) {
            if (slots[i] == id) {
                sendAction(new Widget(bankWidget), "Deposit-All", i);
                break;
            }
        }
    }
}
