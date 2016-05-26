package org.spongebot.bot.rs.api;

import org.spongebot.bot.accessors.IGameObject;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.tools.Misc;

public class GameObject {

    IGameObject gameObject;

    public GameObject(IGameObject gameObject) {
        this.gameObject = gameObject;
    }

    public int getLocalX() {
        return gameObject.getLocalX() >> 7;
    }

    public int getLocalY() {
        return gameObject.getLocalY() >> 7;
    }

    public int getHash() {
        return gameObject.getHash();
    }

    public int getID() {
        return getHash() >> 14 & 32767;
    }

    public String[] getActions() {
        return Context.client.getObjectDefinition(getID()).getActions();
    }

    public String getName() {
        return Context.client.getObjectDefinition(getID()).getName();
    }

    public boolean exists() {
        return Context.client.getGameObjectUID(getLocalX(), getLocalY()) > 0;
    }

    public boolean interact(String action, boolean waitAnim, boolean waitDist, int waitAfter, int... timeout) {
        try {
            int x = getLocalX();
            int y = getLocalY();
            Player local = Context.client.getLocalPlayer();
            if (Context.client.clickObject(this, action)) {
                long time = 0L;
                if (timeout.length > 0)
                    time = System.currentTimeMillis(); // Let's have some decency
                while (waitAnim || waitDist) {
                    if (!exists())
                        return false;
                    // ^ Need to decide whether this should be true or false :/ Could mean either for flax, trees, etc
                    if (timeout.length > 0)
                        if (System.currentTimeMillis() - time > timeout[0])
                            return false;
                    waitAnim = local.getAnimation() > 0;
                    waitDist = local.distance(x, y) > 2;
                    Misc.sleep(100, 150);
                }
                Misc.sleep(waitAfter, waitAfter + 50);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
