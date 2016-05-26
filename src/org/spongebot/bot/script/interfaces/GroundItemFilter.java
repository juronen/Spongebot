package org.spongebot.bot.script.interfaces;

import org.spongebot.bot.rs.api.GroundItem;

public interface GroundItemFilter {

    public boolean accept(GroundItem g);

}
