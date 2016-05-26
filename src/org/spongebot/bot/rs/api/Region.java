package org.spongebot.bot.rs.api;

import org.spongebot.bot.accessors.IRegion;

public class Region {

    IRegion region;

    public Region(IRegion region) {
        this.region = region;
    }

    public int getGameObjectUID(int z, int x, int y) {
        return region.getGameObjectUID(z, x, y);
    }
}
