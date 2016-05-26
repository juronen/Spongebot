package org.spongebot.bot.rs.api.tools;

import org.objectweb.asm.tree.MethodInsnNode;
import org.spongebot.bot.rs.Constants;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.Item;
import org.spongebot.bot.rs.api.Widget;
import org.spongebot.loader.updater.Storage;

import java.util.Map;

public class Inventory {

    private static void sendAction(Widget widget, String action, int index, int id) {
        String[] actions = Context.client.getItemDefinition(id).getInventoryActions();
        for (int i = 0; i < actions.length; i++) {
            if (action.equals(actions[i])) {
                for (Map.Entry<String, Object> e : Storage.itemPackets[i].entrySet()) {
                    String k = e.getKey();
                    if (k.equals("opcode")) {
                        Context.client.putOpcode((int) e.getValue());
                    } else {
                        int val = 0;
                        switch (k) {
                            case "2":
                                val = index;
                                break;
                            case "3":
                                val = Constants.INVENTORY_NORMAL_PARENT << 16;
                                break;
                            case "5":
                                val = id - 1;
                                break;
                        }
                        Context.client.putStream(((MethodInsnNode) e.getValue()).name, val);
                    }
                }
                return;
            }
        }
        System.out.println("Inventory.sendAction - Action " + action + " was not found!");
    }

    // == 149 << 16
    public static int getInventoryID() {
        return Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0).getID();
    }

    // == 15 << 16
    public static int getBankInventoryID() {
        return Widgets.get(Constants.INVENTORY_BANK_PARENT, 0).getID();
    }

    public static int getCount() {
        int count = 0;
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0);
        int[] slots = inventory.getItems();
        for (int i = 0; i < 28; i++) {
            if (slots[i] > 0)
                count++;
        }
        return count;
    }

    public static int getCount(int... ids) {
        int count = 0;
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0);
        int[] slots = inventory.getItems();
        for (int i = 0; i < 28; i++) {
            for (int id : ids)
                if (slots[i] == id)
                    count++;
        }
        return count;
    }

    public static void dropAll() {
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0);
        int[] slots = inventory.getItems();
        for (int i = 0; i < 28; i++) {
            sendAction(inventory, "Drop", i, slots[i]);
        }
    }

    public static void drop(int index) {
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0);
        int[] slots = inventory.getItems();
        sendAction(inventory, "Drop", index, slots[index]);
    }

    public static void use(int indexA, int idA, int indexB, int idB) {
        // Testing
    }

    public Item[] getItemsFull() {
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0);
        int[] slots = inventory.getItems();
        int[] stacks = inventory.getStackSizes();
        Item[] items = new Item[28];
        for (int i = 0; i < 28; i++) {
            if (slots[i] < 1)
                items[i] = null;
            else
                items[i] = new Item(slots[i], stacks[i]);
        }
        return items;
    }

    public Item[] getItems() {
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0);
        int[] slots = inventory.getItems();
        int[] stacks = inventory.getStackSizes();
        Item[] items = new Item[28];
        for (int i = 0; i < 28; i++) {
            if (slots[i] < 1)
                items[i] = null;
            else
                items[i] = new Item(slots[i], stacks[i]);
        }
        return items;
    }

    public Item getItem(int slot) {
        Widget inventory = Widgets.get(Constants.INVENTORY_NORMAL_PARENT, 0);
        return new Item(inventory.getItems()[slot], inventory.getStackSizes()[slot]);
    }
}
