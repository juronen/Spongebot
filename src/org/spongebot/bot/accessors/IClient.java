package org.spongebot.bot.accessors;

public interface IClient {

    public void clickObject(int localX, int localY, int UID);

    public IPacketStream getPacketStream();

    public boolean walk(int fx, int fy, int tx, int ty, boolean free, int a, int b, int c, int d, int e, int type);

    public int getBaseX();

    public int getBaseY();

    public IPlayer getLocalPlayer();

    public IPlayer[] getLocalPlayers();

    public INPC[] getLocalNpcs();

    public IRegion getRegion();

    public IWidget[][] getWidgets();

    public int[] getMaxLevels();

    public int[] getCurrentLevels();

    public int[] getExperiences();

    public int[] getNPCIndices();

    public int getWeight();

    public int getEnergy();

    public IItemDefinition getItemDefinition(int id);

    public IObjectDefinition getObjectDefinition(int id);

    public INPCDefinition getNPCDefinition(int id);

    public String getLoginMessage();

    public String getLoadingMessage();

    public int getLoginState();

    public void closeAllWidgets();

    public void login();

}
