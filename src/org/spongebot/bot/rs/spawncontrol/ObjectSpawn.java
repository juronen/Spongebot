package org.spongebot.bot.rs.spawncontrol;

import org.spongebot.bot.accessors.IGameObject;
import org.spongebot.bot.rs.api.GameObject;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.ScriptController;
import org.spongebot.bot.script.ScriptObjectCache;
import org.spongebot.bot.script.interfaces.ObjectFilter;
import org.spongebot.bot.script.interfaces.ObjectRemoveCallback;
import org.spongebot.bot.script.interfaces.ObjectSpawnCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectSpawn {

    private static ConcurrentHashMap<Integer, IGameObject> cache = new ConcurrentHashMap();

    // Will eventually be made to just wrap ScriptObjectCache and not cache anything itself

    public static List<IGameObject> get(int id) {
        ArrayList<IGameObject> objects = new ArrayList();
        for (Map.Entry<Integer, IGameObject> e : cache.entrySet()) {
            int hash = e.getKey();
            if ((hash >> 14 & 32767) == id)
                objects.add(e.getValue());
        }
        return objects;
    }

    public static void add(IGameObject object) {
        if (object == null)
            return;
        int hash = object.getHash();
        int id = (hash) >> 14 & 32767;
        if (id == 2047)
            return;
        // There's a mystical invisible object with the id 2047 constantly being
        // spawned and unspawned where you are

        Script script = ScriptController.getCurrentScript();
        if (script != null) {
            GameObject go = new GameObject(object);
            if (script instanceof ObjectFilter) {
                if (((ObjectFilter) script).accept(go)) {
                    ScriptObjectCache.add(hash, go);
                    if (script instanceof ObjectSpawnCallback)
                        ((ObjectSpawnCallback) script).onObjectSpawn(go);
                }
            } else {
                ScriptObjectCache.add(hash, go);
                if (script instanceof ObjectSpawnCallback) {
                    ((ObjectSpawnCallback) script).onObjectSpawn(go);
                }
            }
        } else { // if a script is running, the only objects cached should be the ones requested by the script
            cache.put(hash, object);
        }
    }

    public static void remove(IGameObject object) {
        if (object == null)
            return;
        int hash = object.getHash();
        int id = (hash) >> 14 & 32767;
        if (id == 2047)
            return;
        Script script = ScriptController.getCurrentScript();
        if (script != null) {
            ScriptObjectCache.remove(hash);
            if (script instanceof ObjectRemoveCallback) {
                ((ObjectRemoveCallback) script).onObjectRemoved(new GameObject(object));
            }
        } else
            cache.remove(hash);
    }

    public static void clear() {
        cache.clear();
    }

    public static void initScriptCache() {
        Script script = ScriptController.getCurrentScript();
        boolean b = script instanceof ObjectFilter;
        for (Map.Entry<Integer, IGameObject> e : cache.entrySet()) {
            GameObject gameObject = new GameObject(e.getValue());
            if (script instanceof ObjectFilter) {
                if (((ObjectFilter) script).accept(gameObject))
                    ScriptObjectCache.add(e.getKey(), gameObject);
            } else
                ScriptObjectCache.add(e.getKey(), gameObject);
        }
    }

    public static IGameObject[] getAll() {
        Collection<IGameObject> objs = cache.values();
        return objs.toArray(new IGameObject[objs.size()]);
    }
}
