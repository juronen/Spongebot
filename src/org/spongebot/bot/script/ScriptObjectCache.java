package org.spongebot.bot.script;

import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.GameObject;
import org.spongebot.bot.rs.api.Region;
import org.spongebot.bot.script.interfaces.ObjectFilter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptObjectCache {

    private static ConcurrentHashMap<Integer, GameObject> objectCache = new ConcurrentHashMap();

    private static final int MAX_DIST = 104 * 104 * 2; // region diagonal

    public static void add(int uid, GameObject g) {
        objectCache.put(uid, g);
    }

    public static void remove(int uid) {
        objectCache.remove(uid);
    }

    public static void addAll(HashMap<Integer, GameObject> objs) {
        objectCache.putAll(objs);
    }

    public static GameObject getObjectAt(int x, int y) {
        // This way I can either pass local or world coords to it
        if (x > 103)
            x -= Context.client.getBaseX();
        if (y > 103)
            y -= Context.client.getBaseY();
        Region region = Context.client.getRegion();
        Iterator<Map.Entry<Integer, GameObject>> iterator = objectCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, GameObject> e = iterator.next();
            GameObject g = e.getValue();
            if (g == null) {
                iterator.remove();
                continue;
            }
            if (region.getGameObjectUID(0, g.getLocalX(), g.getLocalY()) == e.getKey()) {
                if (g.getLocalX() == x && g.getLocalY() == y)
                    return g;
            } else {
                iterator.remove();
            }
        }
        return null;
    }

    public static GameObject findNearest(ObjectFilter filter) {
        GameObject g = null;
        int dist = MAX_DIST;
        int px = Context.client.getLocalPlayer().getLocalX();
        int py = Context.client.getLocalPlayer().getLocalY();
        Region region = Context.client.getRegion();
        Iterator<Map.Entry<Integer, GameObject>> iterator = objectCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, GameObject> e = iterator.next();
            GameObject go = e.getValue();
            if (go == null) {
                iterator.remove();
                continue;
            }
            if (region.getGameObjectUID(0, go.getLocalX(), go.getLocalY()) == e.getKey()) {
                if (filter.accept(go)) {
                    int dX = go.getLocalX() - px;
                    int dY = go.getLocalY() - py;
                    int newDist = dX * dX + dY * dY;
                    if (newDist < dist) {
                        dist = newDist;
                        g = go;
                    }
                }
            } else {
                iterator.remove();
            }
        }
        return g;
    }

    public static GameObject findNearest() {
        GameObject g = null;
        int dist = MAX_DIST;
        int px = Context.client.getLocalPlayer().getLocalX();
        int py = Context.client.getLocalPlayer().getLocalY();
        Region region = Context.client.getRegion();
        Iterator<Map.Entry<Integer, GameObject>> iterator = objectCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, GameObject> e = iterator.next();
            GameObject go = e.getValue();
            if (go == null) {
                iterator.remove();
                continue;
            }
            if (region.getGameObjectUID(0, go.getLocalX(), go.getLocalY()) == e.getKey()) {
                int dX = go.getLocalX() - px;
                int dY = go.getLocalY() - py;
                int newDist = dX * dX + dY * dY;
                if (newDist < dist) {
                    dist = newDist;
                    g = e.getValue();
                }
            } else {
                iterator.remove();
            }
        }
        return g;
    }

    public static GameObject[] getSorted() {
        TreeMap<Integer, GameObject> sorted = new TreeMap();
        int px = Context.client.getLocalPlayer().getLocalX();
        int py = Context.client.getLocalPlayer().getLocalY();
        Region region = Context.client.getRegion();
        Iterator<Map.Entry<Integer, GameObject>> iterator = objectCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, GameObject> e = iterator.next();
            GameObject go = e.getValue();
            if (go == null) {
                iterator.remove();
                continue;
            }
            if (region.getGameObjectUID(0, go.getLocalX(), go.getLocalY()) == e.getKey()) {
                int lx = go.getLocalX();
                int ly = go.getLocalY();
                int dx = lx - px;
                int dy = ly - py;
                sorted.put(dx * dx + dy * dy, go);
            } else
                iterator.remove();
        }
        return sorted.values().toArray(new GameObject[sorted.values().size()]);
    }
}
