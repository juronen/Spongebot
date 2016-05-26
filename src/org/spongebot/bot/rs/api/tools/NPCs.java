package org.spongebot.bot.rs.api.tools;

import org.spongebot.bot.accessors.INPC;
import org.spongebot.bot.accessors.INPCDefinition;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.NPC;

public class NPCs {

    public static NPC findByName(String name) {
        INPC[] npcs = Context.client.getLocalNPCs();
        for (int i = 0; i < npcs.length; i++) {
            INPC npc = npcs[i];
            if (npc != null) {
                INPCDefinition def = npc.getDefinition();
                String npcName = def.getName();
                if (name.equals(npcName)) {
                    return new NPC(npc, i);
                }
            }
        }
        return null;
    }

}
