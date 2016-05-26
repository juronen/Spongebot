package org.spongebot.bot.script.bundled;

import org.spongebot.bot.rs.api.GameObject;
import org.spongebot.bot.rs.api.Item;
import org.spongebot.bot.rs.api.Tile;
import org.spongebot.bot.rs.api.tools.Bank;
import org.spongebot.bot.rs.api.tools.Inventory;
import org.spongebot.bot.rs.api.tools.Objects;
import org.spongebot.bot.rs.api.tools.Walking;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.interfaces.InventoryCallback;
import org.spongebot.bot.script.interfaces.ObjectFilter;

public class FlaxPicker extends Script implements ObjectFilter, InventoryCallback {

    private static final Tile[] path = new Tile[]{
            new Tile(2726, 3491), new Tile(2727, 3465), new Tile(2741, 3443)
    };

    private int picked = 0;

    private ObjectFilter flaxFilter = new ObjectFilter() {
        @Override
        public boolean accept(GameObject g) {
            for (String s : g.getActions())
                if ("Pick".equals(s))
                    return true;
            return false;
        }
    };

    private ObjectFilter bankFilter = new ObjectFilter() {
        @Override
        public boolean accept(GameObject g) {
            for (String s : g.getActions())
                if ("Bank".equals(s))
                    return true;
            return false;
        }
    };

    @Override
    public String[] getStatus() {
        return new String[]{
                "[---------- Integrated FlaxPicker ----------]",
                pad("[", "Flax picked: " + picked, "]", 49),
                "[-----------------------------------------------]"
        };
    }

    @Override
    public boolean onStart() {
        return true;
    }

    @Override
    public void onStop() {
        System.out.println(getStatus());
        println("Finished");
    }

    private void toFlax() {
        println("Walking to flax");
        for (int i = 1; i < 3; i++) {
            Walking.walkTo(path[i].x, path[i].y);
        }
    }

    private void toBank() {
        println("Walking to bank");
        for (int i = 1; i >= 0; i--) {
            Walking.walkTo(path[i].x, path[i].y);
        }
    }

    private boolean openBank() {
        GameObject g = getNearest(bankFilter);
        if (g != null) {
            return g.interact("Bank", false, true, 2000, 15000);
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (Inventory.getCount() < 28) {
                if (getNearest(flaxFilter) == null)
                    toFlax();
                else {
                    GameObject g = getNearest(flaxFilter);
                    if (client.getLocalPlayer().distance(g.getLocalX(), g.getLocalY()) > 10)
                        toFlax();
                }
            }
            while (Inventory.getCount() < 28) {
                GameObject g = getNearest(flaxFilter);
                if (g == null)
                    continue;
                g.interact("Pick", true, false, 100, 5000);
            }
            if (Objects.findClosest(25808) == null)
                toBank();
            else {
                GameObject g = getNearest(bankFilter);
                if (client.getLocalPlayer().distance(g.getLocalX(), g.getLocalY()) > 10)
                    toBank();
            }
            if (Inventory.getCount() >= 28) {
                if (!openBank())
                    return;
                Bank.depositAll();
                sleep(2000, 3000);
            }
        }
    }

    @Override
    public boolean accept(GameObject g) {
        for (String s : g.getActions())
            if ("Pick".equals(s) || "Bank".equals(s))
                return true;
        return false;
    }

    @Override
    public void onItemAction(int widget, int id, int index) {
        if (id > 0) {
            if (new Item(id).getName().equals("Flax")) {
                picked++;
                println("Flax: " + picked);
            }
        }
    }
}
