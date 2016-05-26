package org.spongebot.bot.script.interfaces;

import org.spongebot.bot.rs.api.NPC;

public interface NPCFilter {

    public boolean accept(NPC npc);
}
