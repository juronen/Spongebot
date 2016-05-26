package org.spongebot.loader.updater.spawns;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

import java.util.ListIterator;

public class NPCSpawnInjector implements Opcodes {

    public static void run(ClassNode classNode) {
        for (MethodNode mn : classNode.methods) {
            if (mn.desc.length() == 4 && mn.desc.endsWith(")V")) {
                if (Util.findInstruction(mn, new InsnNode(ATHROW))) {
                    if (Util.findInstruction(mn, new TypeInsnNode(NEW, Loader.botToClient("NPC")))) {
                        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
                        while (ainli.hasNext()) {
                            AbstractInsnNode ain = ainli.next();
                            if (ain.getOpcode() == INVOKEVIRTUAL) {
                                MethodInsnNode min = (MethodInsnNode) ain;
                                if (min.owner.equals(Loader.botToClient("NPC")) && min.desc.endsWith(")V") && min.desc.startsWith("(IIZ") && min.desc.length() == 7) {
                                    AbstractInsnNode[] pat = new AbstractInsnNode[]{
                                            new FieldInsnNode(GETSTATIC, null, null, "L" + Loader.botToClient("Player") + ";"),
                                            new FieldInsnNode(GETFIELD, null, null, "[I")
                                    };
                                    pat = Util.findPattern(mn, pat);
                                    if (pat != null) {
                                        AbstractInsnNode prev = Util.getPrevious(pat[0]);
                                        if (prev.getOpcode() != ALOAD) {
                                            prev = Util.getPrevious(prev);
                                            if (prev.getOpcode() != ALOAD)
                                                continue;
                                        }
                                        System.out.println("Injecting NPC spawn callback into " + classNode.name + "." + mn.name);
                                        AbstractInsnNode bipush = mn.instructions.getFirst();
                                        while (!Util.match(bipush, new IntInsnNode(BIPUSH, 27)))
                                            bipush = bipush.getNext();
                                        bipush = bipush.getNext(); // Jump
                                        bipush = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) bipush).label)).getNext();
                                        while (bipush.getOpcode() != ISTORE)
                                            bipush = bipush.getNext();
                                        AbstractInsnNode next = min.getNext();
                                        mn.instructions.insertBefore(next, new VarInsnNode(ALOAD, ((VarInsnNode) prev).var));
                                        mn.instructions.insertBefore(next, new VarInsnNode(ILOAD, ((VarInsnNode) bipush).var));
                                        mn.instructions.insertBefore(next, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/rs/spawncontrol/NPCSpawn", "npcSpawned", "(Lorg/spongebot/bot/accessors/INPC;I)V"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
