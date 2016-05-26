package org.spongebot.bot.script.bundled;

import org.spongebot.bot.rs.api.GameObject;
import org.spongebot.bot.rs.api.tools.Inventory;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.interfaces.InventoryCallback;
import org.spongebot.bot.script.interfaces.ObjectSpawnCallback;

public class TerribleMiner extends Script implements InventoryCallback, ObjectSpawnCallback {

    private static final int TIN_X = 3225;
    private static final int TIN_Y = 3148;

    private int tinID = -1;

    @Override
    public String[] getStatus() {
        return new String[]{};
    }

    @Override
    public boolean onStart() {
        return true;
    }

    @Override
    public void onStop() {
    }

    @Override
    public void run() {
        GameObject rock = getAt(TIN_X, TIN_Y);
        tinID = rock.getID();
        rock.interact("Mine", true, false, 0);
        while (true)
            sleep(500, 1000);
    }

    @Override
    public void onItemAction(int widget, int id, final int index) {
        if (id != 0) {
            println("Inventory item spawn - ID: " + id + " index: " + index);
            String name = client.getItemDefinition(id).getName();
            if (name.equals("Tin ore") || name.equals("Copper ore") || name.equals("Clay")) {
                println("Successfully mined a rock, dropping...");
                new Thread(new Runnable() { // Rock will be dropped without interrupting current activity
                    @Override
                    public void run() {
                        sleep(100, 200); // Don't remove
                        Inventory.drop(index);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onObjectSpawn(final GameObject g) {
        if (g.getLocalX() == (TIN_X - client.getBaseX()) && g.getLocalY() == (TIN_Y - client.getBaseY())) {
            println("Rock spawn - ID: " + g.getID());
            if (g.getID() == tinID) {
                println("Tin spawned, mining...");
                new Thread(new Runnable() {
                    // It needs to wait a little, before attempting to mine it, the script will get the message that the
                    // object has spawned before it has been fully configured
                    @Override
                    public void run() {
                        g.interact("Mine", true, false, 0);
                    }
                }).start();
            }
        }
    }
}
