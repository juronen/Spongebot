package org.spongebot.bot.accessors;

public interface ICharacter extends IRenderable {

    public int getLocalX();

    public int getLocalY();

    public int getAnimation();

    public int[] getPathX();

    public int[] getPathY();
}
