package org.spongebot.bot.rs.api.tools;

import org.spongebot.bot.accessors.IGameObject;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.GameObject;
import org.spongebot.bot.rs.spawncontrol.ObjectSpawn;

import java.util.List;
import java.util.TreeMap;

public class Objects {

    public static GameObject findClosest(int id) {
        List<IGameObject> objects = ObjectSpawn.get(id);
        int dist = Integer.MAX_VALUE;
        IGameObject closestObj = null;
        for (IGameObject object : objects) {
            int lx = object.getLocalX() >> 7;
            int ly = object.getLocalY() >> 7;
            if (Context.client.getRegion().getGameObjectUID(0, lx, ly) == 0) {
                ObjectSpawn.remove(object);
                continue;
            }
            int px = Context.client.getLocalPlayer().getLocalX();
            int py = Context.client.getLocalPlayer().getLocalY();
            int dx = lx - px;
            int dy = ly - py;
            int ndist = dx * dx + dy * dy;
            if (ndist < dist) {
                dist = ndist;
                closestObj = object;
            }
        }
        return new GameObject(closestObj);
    }

    public static GameObject[] getAll() {
        IGameObject[] objects = ObjectSpawn.getAll();
        System.out.println("Object cache has " + objects.length + " objs");
        GameObject[] ret = new GameObject[objects.length];
        System.out.println(objects[0].getLocalX() >> 7);
        for (int i = 0; i < objects.length; i++) {
            try {
                if (Context.client.getGameObjectUID(objects[i].getLocalX() >> 7, objects[i].getLocalY() >> 7) == 0) {
                    ObjectSpawn.remove(objects[i]);
                    continue;
                }
                ret[i] = new GameObject(objects[i]);
            } catch (Exception e) {
                e.printStackTrace();
                ret[i] = null;
            }
        }
        return ret;
    }

    public static GameObject[] getSorted() {
        GameObject[] objects = getAll();
        TreeMap<Integer, GameObject> sorted = new TreeMap();
        int px = Context.client.getLocalPlayer().getLocalX();
        int py = Context.client.getLocalPlayer().getLocalY();
        for (GameObject g : objects) {
            if (g == null)
                continue;
            try {
                int lx = g.getLocalX();
                int ly = g.getLocalY();
                int dx = lx - px;
                int dy = ly - py;
                sorted.put(dx * dx + dy * dy, g);
            } catch (Exception e) {

            }
        }
        return sorted.values().toArray(new GameObject[sorted.values().size()]);
    }

    public static GameObject getAt(int worldX, int worldY) {
        if (worldX > 103)
            worldX -= Context.client.getBaseX();
        if (worldY > 103)
            worldY -= Context.client.getBaseY();
        for (GameObject g : getSorted()) {
            try {
                if (g.getLocalX() == worldX && g.getLocalY() == worldY) {
                    return g;
                }
            } catch (NullPointerException e) {
                continue;
            }
        }
        return null;
    }

}
