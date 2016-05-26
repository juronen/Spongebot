package org.spongebot.bot.rs.api;

import org.objectweb.asm.tree.MethodInsnNode;
import org.spongebot.bot.accessors.*;
import org.spongebot.bot.rs.api.tools.Walking;
import org.spongebot.bot.rs.reflection.Reflection;
import org.spongebot.loader.updater.Storage;

import java.util.HashMap;
import java.util.Map;

public class Client {

    private IClient client;

    public HashMap<Integer, IItemDefinition> itemDefCache = new HashMap();
    public HashMap<Integer, IObjectDefinition> objDefCache = new HashMap();

    public Client(IClient client) {
        this.client = client;
    }

    public IWidget[][] getWidgets() {
        return client.getWidgets();
    }

    public boolean clickObject(GameObject g, String action) {

        int localX = g.getLocalX();
        int localY = g.getLocalY();
        int worldX = getBaseX() + localX;
        int worldY = getBaseY() + localY;

        int uid = getRegion().getGameObjectUID(0, localX, localY);
        if (uid <= 0) // This might be frequent, let's not print about it. Inb4 debugging for hours.
            return false;

        String[] actions = g.getActions();

        if (actions == null) {
            System.out.println("Broken object definitions / actions!");
            return false;
        }

        int actionIndex = -1;

        for (int i = 0; i < actions.length; i++) {
            if (action.equals(actions[i])) {
                actionIndex = i;
                break;
            }
        }

        if (actionIndex == -1) {
            System.out.println("Action: " + action + " was not found!");
            return false;
        }

        client.clickObject(localX, localY, uid);

        if (Storage.objectPackets[actionIndex].size() != 4) {
            System.out.println("Malformed object interaction packet");
            return false;
        }
        for (Map.Entry<String, Object> e : Storage.objectPackets[actionIndex].entrySet()) {
            if (e.getKey().equals("opcode")) {
                int opcode = (int) e.getValue();
                putOpcode(opcode);
            } else {
                switch (e.getKey()) {
                    case "x":
                        putStream(((MethodInsnNode) e.getValue()).name, worldX);
                        break;
                    case "y":
                        putStream(((MethodInsnNode) e.getValue()).name, worldY);
                        break;
                    case "id":
                        putStream(((MethodInsnNode) e.getValue()).name, uid >> 14 & 32767);
                        break;
                    default:
                        System.out.println("Malformed object interaction packet");
                        return false;
                }
            }
        }
        return true;
    }

    public boolean walkTo(int x, int y, boolean free, int w, int h, int type) {
        Walking.destX = x;
        Walking.destY = y;
        int bx = client.getBaseX();
        int by = client.getBaseY();
        if (x > 103)
            x -= bx;
        if (y > 103)
            y -= by;
        return client.walk(getLocalPlayer().getPathX()[0], getLocalPlayer().getPathY()[0], x, y, free, 0, 0, w, h, 0, type);
    }

    public INPC[] getLocalNPCs() {
        return client.getLocalNpcs();
    }

    public int getGameObjectUID(int x, int y) {
        try {
            return getRegion().getGameObjectUID(0, x, y);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getLoginMessage() {
        return client.getLoginMessage();
    }

    public String getLoadingMessage() {
        return client.getLoadingMessage();
    }

    public int getLoginState() {
        return client.getLoginState();
    }

    public void login() {
        client.login();
    }

    public Player getLocalPlayer() {
        return new Player(client.getLocalPlayer());
    }

    public Region getRegion() {
        return new Region(client.getRegion());
    }

    public IObjectDefinition getObjectDefinition(int id) {
        if (objDefCache.containsKey(id))
            return objDefCache.get(id);
        IObjectDefinition def = client.getObjectDefinition(id);
        if (def.getName() == null)
            def = def.doSomething();
        if (def.getName() == "null")
            def = def.doSomething();
        objDefCache.put(id, def);
        return def;
    }

    public IItemDefinition getItemDefinition(int id) {
        if (itemDefCache.containsKey(id))
            return itemDefCache.get(id);
        IItemDefinition def = client.getItemDefinition(id);
        itemDefCache.put(id, def);
        return def;
    }

    public int getBaseX() {
        return client.getBaseX();
    }

    public int getBaseY() {
        return client.getBaseY();
    }

    public void putOpcode(int opcode) {
        client.getPacketStream().putOpcode(opcode);
    }

    public void putStream(String method, int value) {
        Reflection.invoke(client.getPacketStream(), "_" + method, new Object[]{value}, new Class<?>[]{int.class});
    }

}
