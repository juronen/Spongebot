package org.spongebot.bot.accessors;

public interface INPCDefinition {

    public String getName();

    public String[] getActions();

    public int getLevel();

    public int getID();

    public INPCDefinition doSomething();

}
