package org.spongebot.bot.rs.api;

import org.objectweb.asm.tree.MethodInsnNode;
import org.spongebot.bot.accessors.INPC;
import org.spongebot.bot.accessors.INPCDefinition;
import org.spongebot.bot.rs.Constants;
import org.spongebot.bot.rs.Context;
import org.spongebot.loader.updater.Storage;

import java.util.Map;

public class NPC extends Character {

    private INPC npc;

    private int index;

    public NPC(INPC npc) {
        super(npc);
        this.npc = npc;
    }

    public NPC(INPC npc, int index) {
        super(npc);
        this.npc = npc;
        this.index = index;
    }

    public INPCDefinition getDefinition() {
        INPCDefinition def = npc.getDefinition();
        //if (def.getName() == null)
        //     def = def.doSomething();
        // if (def.getName().equals("null"))
        //      def = def.doSomething();
        return def;
    }

    public String getName() {
        return getDefinition().getName();
    }

    public boolean interact(String action) {
        INPCDefinition def = npc.getDefinition();
        String[] actions = def.getActions();
        for (int i = 0; i < actions.length; i++) {
            if (action.equals(actions[i])) {
                System.out.println("Locals " + getPathX()[0] + " " + getPathY()[0]);
                boolean b = Context.client.walkTo(getPathX()[0], getPathY()[0], false, 1, 1, Constants.WALKING_INTERACT);
                for (Map.Entry<String, Object> e : Storage.npcPackets[i].entrySet()) {
                    if (e.getKey().equals("opcode"))
                        Context.client.putOpcode((int) e.getValue());
                    else
                        Context.client.putStream(((MethodInsnNode) e.getValue()).name, index);
                }
                return b;
            }
        }
        return false;
    }
}
