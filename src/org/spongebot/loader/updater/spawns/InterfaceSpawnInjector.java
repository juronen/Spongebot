package org.spongebot.loader.updater.spawns;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

import java.util.ListIterator;

public class InterfaceSpawnInjector implements Opcodes {

    private static AbstractInsnNode[] shr = new AbstractInsnNode[]{
            new VarInsnNode(ILOAD, 0),
            new IntInsnNode(BIPUSH, 16),
            new InsnNode(ISHR),
            new VarInsnNode(ISTORE, 2)
    };

    private static AbstractInsnNode[] mask = new AbstractInsnNode[]{
            new VarInsnNode(ILOAD, 0),
            new LdcInsnNode(65535),
            new InsnNode(IAND),
            new VarInsnNode(ISTORE, 3)
    };

    public static void run(ClassNode classNode) {
        for (MethodNode mn : classNode.methods) {
            if (mn.desc.startsWith("(I") && mn.desc.length() <= 8 && mn.desc.endsWith("L" + Loader.botToClient("Widget") + ";")) {
                if (Util.findPattern(mn, shr) != null && Util.findPattern(mn, mask) != null) {
                    System.out.println("Injecting interface spawn in " + classNode.name + " " + mn.name);
                    ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
                    while (ainli.hasNext()) {
                        AbstractInsnNode ain = ainli.next();
                        if (ain.getOpcode() == ARETURN && ain.getPrevious().getOpcode() == AALOAD) {
                            mn.instructions.insertBefore(ain, new InsnNode(DUP));
                            mn.instructions.insertBefore(ain, new VarInsnNode(ILOAD, 0));
                            mn.instructions.insertBefore(ain, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/rs/spawncontrol/InterfaceSpawn", "onSpawn", "(Lorg/spongebot/bot/accessors/IWidget;I)V"));
                        }
                    }
                }
            }
        }
    }
}
