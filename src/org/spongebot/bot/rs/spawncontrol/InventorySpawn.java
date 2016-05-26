package org.spongebot.bot.rs.spawncontrol;

import org.spongebot.bot.accessors.IWidget;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.ScriptController;
import org.spongebot.bot.script.interfaces.InventoryCallback;

public class InventorySpawn {

    public static void onSpawn(IWidget widget, int index, int itemID) {
        int p = widget.getID() >> 16;
        int c = widget.getID() & 0xFFFF;
        //System.out.println("Item: " + itemID + " " + index + " " + p + " " + c);
        Script script = ScriptController.getCurrentScript();
        if (script != null) {
            if (script instanceof InventoryCallback) {
                ((InventoryCallback) script).onItemAction(widget.getID() >> 16, itemID, index);
            }
        }
    }
}
