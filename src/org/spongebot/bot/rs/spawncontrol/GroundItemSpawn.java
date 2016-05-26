package org.spongebot.bot.rs.spawncontrol;

import org.spongebot.bot.accessors.IItem;
import org.spongebot.bot.rs.api.GroundItem;
import org.spongebot.bot.rs.api.Item;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.ScriptController;
import org.spongebot.bot.script.interfaces.GroundItemCallback;
import org.spongebot.bot.script.interfaces.GroundItemFilter;

public class GroundItemSpawn {

    public static void onSpawn(IItem item, int localX, int localY) {
        //System.out.println("GroundItem spawn: ID: " + item.getID() + " Stack: " + item.getStackSize() + " X: " + localX + " Y: " + localY);
        Script script = ScriptController.getCurrentScript();
        if (script != null) {
            // <- add to cache
            GroundItem groundItem = new GroundItem(new Item(item), localX, localY);
            if (script instanceof GroundItemCallback) {
                if (script instanceof GroundItemFilter) {
                    if (((GroundItemFilter) script).accept(groundItem))
                        ((GroundItemCallback) script).onGroundItemSpawn(groundItem);
                } else
                    ((GroundItemCallback) script).onGroundItemSpawn(groundItem);
            }
        }
    }
}
