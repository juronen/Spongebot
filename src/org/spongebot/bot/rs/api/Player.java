package org.spongebot.bot.rs.api;

import org.spongebot.bot.accessors.IPlayer;

public class Player extends Character {

    IPlayer player;

    public Player(IPlayer player) {
        super(player);
        this.player = player;
    }

    public String getName() {
        return player.getName();
    }
}
