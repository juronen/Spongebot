package org.spongebot.loader.updater.spawns;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

import java.util.Map;

public class InventorySpawnInjector implements Opcodes {

    public static void run(ClassNode classNode) {

        Map<String, String> widgetFields = Loader.fields.get("Widget");

        AbstractInsnNode[] pat = new AbstractInsnNode[]{
                new FieldInsnNode(GETFIELD, Loader.botToClient("Widget"), widgetFields.get("getItems"), "[I"),
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE), // index
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE), // item id... 9000 = ignore once again
                new InsnNode(IASTORE)
        };

        for (MethodNode mn : classNode.methods) {
            AbstractInsnNode[] ret = Util.findPattern(mn, pat);
            while (ret != null) {
                mn.instructions.insertBefore(ret[0], new InsnNode(DUP));
                int varIndex = ((VarInsnNode) ret[1]).var;
                int varID = ((VarInsnNode) ret[2]).var;
                AbstractInsnNode next = ret[3].getNext();
                mn.instructions.insertBefore(next, new VarInsnNode(ILOAD, varIndex));
                mn.instructions.insertBefore(next, new VarInsnNode(ILOAD, varID));
                mn.instructions.insertBefore(next, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/rs/spawncontrol/InventorySpawn", "onSpawn", "(Lorg/spongebot/bot/accessors/IWidget;II)V"));
                ret = Util.findPattern(mn, pat, ret[2]);
                // Lets do dis for every occurence in the method, otherwise
                // there will be probrem
            }
        }
    }
}
