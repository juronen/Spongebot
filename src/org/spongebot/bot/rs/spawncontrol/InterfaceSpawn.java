package org.spongebot.bot.rs.spawncontrol;

import org.spongebot.bot.accessors.IWidget;
import org.spongebot.bot.rs.api.Widget;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.ScriptController;
import org.spongebot.bot.script.interfaces.InterfaceSpawnCallback;

public class InterfaceSpawn {

    public static void onSpawn(final IWidget widget, final int hash) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Some of the interface properties / members that a user will want to use will not have been
                // loaded by the time this callback is hit, so the script should wait a fraction of a second
                // before attempting to do anythign with the interface
                Script script = ScriptController.getCurrentScript();
                if (script != null) {
                    if (script instanceof InterfaceSpawnCallback) {
                        ((InterfaceSpawnCallback) script).interfaceSpawned(new Widget(widget), hash >> 16, hash & 0xFFFF);
                    }
                }
            }
        }).start();
    }
}
