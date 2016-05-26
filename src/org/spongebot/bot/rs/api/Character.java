package org.spongebot.bot.rs.api;

import org.spongebot.bot.accessors.ICharacter;
import org.spongebot.bot.rs.Context;

public class Character {

    ICharacter character;

    public Character(ICharacter character) {
        this.character = character;
    }

    public int getLocalX() {
        return (character.getLocalX() >> 7) & 0xFF;
    }

    public int getLocalY() {
        return (character.getLocalY() >> 7) & 0xFF;
    }

    public int getX() {
        return getLocalX() + Context.client.getBaseX();
    }

    public int getY() {
        return getLocalY() + Context.client.getBaseY();
    }

    public int getAnimation() {
        return character.getAnimation();
    }

    public int[] getPathX() {
        return character.getPathX();
    }

    public int[] getPathY() {
        return character.getPathY();
    }

    public int distance(int lx, int ly) {
        return (int) Math.sqrt((getLocalX() - lx) * (getLocalX() - lx) + (getLocalY() - ly) * (getLocalY() - ly));
    }

}
