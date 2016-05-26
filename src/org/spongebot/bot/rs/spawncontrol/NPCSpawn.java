package org.spongebot.bot.rs.spawncontrol;

import org.spongebot.bot.accessors.INPC;
import org.spongebot.bot.rs.api.NPC;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.ScriptController;
import org.spongebot.bot.script.interfaces.NPCFilter;
import org.spongebot.bot.script.interfaces.NPCSpawnCallback;

public class NPCSpawn {

    public static void npcSpawned(INPC npc, int index) {
        System.out.println("NPC spawn: " + new NPC(npc).getDefinition().getName() + " - Index: " + index);
        Script script = ScriptController.getCurrentScript();
        if (script != null) {
            NPC npc1 = new NPC(npc, index);
            if (script instanceof NPCSpawnCallback) {
                if (script instanceof NPCFilter) {
                    if (((NPCFilter) script).accept(npc1))
                        ((NPCSpawnCallback) script).onNPCSpawn(npc1);
                } else
                    ((NPCSpawnCallback) script).onNPCSpawn(npc1);
            }
        }
    }
}
