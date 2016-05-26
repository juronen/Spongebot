package org.spongebot.bot.script.interfaces;

import org.spongebot.bot.rs.api.GameObject;

public interface ObjectFilter {

    public boolean accept(GameObject g);
}
