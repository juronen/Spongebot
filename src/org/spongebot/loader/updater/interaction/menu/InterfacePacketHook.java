package org.spongebot.loader.updater.interaction.menu;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.updater.Storage;
import org.spongebot.loader.updater.searching.Util;

import java.util.ListIterator;

public class InterfacePacketHook implements Opcodes {

    private static final AbstractInsnNode[] patternA = new AbstractInsnNode[]{
            new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
            new InsnNode(ICONST_1),
            new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
            new InsnNode(IADD),
            new InsnNode(ISHR),
            new InsnNode(ICONST_1),
            new InsnNode(IAND)
    };

    private static final AbstractInsnNode[] patternB = new AbstractInsnNode[]{
            new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
            new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
            new InsnNode(ICONST_1),
            new InsnNode(IADD),
            new InsnNode(ISHR),
            new InsnNode(ICONST_1),
            new InsnNode(IAND)
    };

    private static int[] intConstants = {ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5};

    private static int getIntegerConstant(int opcode) {
        for (int i = 0; i < 6; i++)
            if (opcode == intConstants[i])
                return i;
        return -1;
    }

    private static int getIntegerConstant(AbstractInsnNode intValue) {
        int val = getIntegerConstant(intValue.getOpcode());
        if (val == -1) {
            if (intValue instanceof IntInsnNode) {
                val = ((IntInsnNode) intValue).operand;
            } else if (intValue instanceof LdcInsnNode) {
                val = (int) ((LdcInsnNode) intValue).cst;
            } else {
                return 0xB00B135;
            }
        }
        return val;
    }

    // Most likely the data types will remain the same for each option, but let's have some foresight shall we
    public static void run(ClassNode classNode) {
        for (MethodNode mn : classNode.methods) {
            if (mn.desc.startsWith("(IIILjava/lang/String;")) {
                if (Util.findInstruction(mn, new InsnNode(ATHROW))) {
                    if ((Util.findPattern(mn, patternA) != null) || (Util.findPattern(mn, patternB) != null)) {
                        Util.removeTrapCode(mn);
                        for (int i = 1; i < 11; i++) { // Jagex upgraded the option capacity along with the bank updates in september
                            ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
                            while (ainli.hasNext()) {
                                AbstractInsnNode ain = ainli.next();
                                if (Util.match(ain, new VarInsnNode(ILOAD, 0))) {
                                    AbstractInsnNode prev = Util.getPrevious(ain);
                                    AbstractInsnNode n1 = Util.getNext(ain);
                                    AbstractInsnNode n2 = Util.getNext(n1);
                                    int action;
                                    AbstractInsnNode jump;
                                    if (((action = getIntegerConstant(prev)) != 0xB00B135 && (jump = n1) instanceof JumpInsnNode)
                                            || ((action = getIntegerConstant(n1)) != 0xB00B135 && (jump = n2) instanceof JumpInsnNode)) {
                                        if (action != i)
                                            continue;
                                        System.out.println("Picking up action " + action);
                                        JumpInsnNode jin = (JumpInsnNode) jump;
                                        AbstractInsnNode next;
                                        if (jin.getOpcode() == IF_ICMPEQ) {
                                            next = mn.instructions.get(mn.instructions.indexOf(jin.label)).getNext();
                                        } else {
                                            next = Util.getNext(jin);
                                        }
                                        if (next.getOpcode() == GOTO)
                                            next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                                        if (next.getOpcode() == -1)
                                            next = next.getNext();
                                        next = Util.findInstructionSnake(mn, new MethodInsnNode(INVOKEVIRTUAL, null, null, null), next, false);
                                        Storage.interfaceOpcodes[i - 1] = getIntegerConstant(Util.getPrevious(Util.getPrevious(next)));
                                        // ^ = putOpcode
                                        for (int z = 0; z < 2; z++) {
                                            next = Util.getNext(next);
                                            next = Util.findInstructionSnake(mn, new MethodInsnNode(INVOKEVIRTUAL, null, null, null), next, false);
                                            Storage.interfacePackets[i - 1][z] = ((MethodInsnNode) next).name;
                                            System.out.println("i: " + i + " z: " + z + " " + ((MethodInsnNode) next).name);
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
}
