package org.spongebot.loader.updater.spawns;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

import java.util.ListIterator;

public class GroundItemSpawnInjector implements Opcodes {

    public static void run(ClassNode classNode) {
        String dequeClass = Loader.botToClient("NodeQueue"); // A bit of a naming mismatch going on here
        String nodeClass = Loader.botToClient("Node");
        for (MethodNode mn : classNode.methods) {
            if (!Util.findInstruction(mn, new InsnNode(ATHROW)))
                continue;
            if (mn.desc.length() != 4)
                continue;
            ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
            while (ainli.hasNext()) {
                AbstractInsnNode ain = ainli.next();
                if (Util.match(ain, new MethodInsnNode(INVOKEVIRTUAL, dequeClass, null, "(L" + nodeClass + ";)V"))) {
                    AbstractInsnNode prev = ain.getPrevious();
                    for (int i = 0; i < 9; i++)
                        prev = Util.getPrevious(prev);
                    if (Util.match(prev, new FieldInsnNode(GETSTATIC, "client", null, null))) {
                        VarInsnNode item = (VarInsnNode) ain.getPrevious();
                        VarInsnNode yCoord = (VarInsnNode) item.getPrevious().getPrevious();
                        VarInsnNode xCoord = (VarInsnNode) yCoord.getPrevious().getPrevious();
                        mn.instructions.insertBefore(ain, new VarInsnNode(ALOAD, item.var));
                        mn.instructions.insertBefore(ain, new VarInsnNode(ILOAD, xCoord.var));
                        mn.instructions.insertBefore(ain, new VarInsnNode(ILOAD, yCoord.var));
                        mn.instructions.insertBefore(ain, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/rs/spawncontrol/GroundItemSpawn", "onSpawn", "(Lorg/spongebot/bot/accessors/IItem;II)V"));
                        System.out.println("Injected gitem spawn callback into " + mn.name + " in " + classNode.name);
                        break;
                    }
                }
            }
        }
    }
}
