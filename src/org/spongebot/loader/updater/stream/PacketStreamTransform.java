package org.spongebot.loader.updater.stream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.bot.callbacks.Printer;
import org.spongebot.loader.updater.searching.Util;

import java.util.ListIterator;

public class PacketStreamTransform implements Opcodes {

    public static String streamName = ""; // Sketchy at best
    public static String className = "";
    public static String methodName = "";

    private ClassNode classNode;

    public PacketStreamTransform(ClassNode classNode) {
        this.classNode = classNode;
    }

    private static void disarm(MethodNode mn) {
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (ain.getOpcode() == Opcodes.NEW) {
                TypeInsnNode tin = (TypeInsnNode) ain;
                if (tin.desc.contains("IllegalStateException")) {
                    JumpInsnNode jin = (JumpInsnNode) ain.getPrevious();
                    LabelNode ln = jin.label;
                    mn.instructions.set(ain.getPrevious(), new JumpInsnNode(GOTO, ln));
                    // ^ Let's always branch to the happy place
                }
            }
        }
    }

    private void injectWrapper(String refName, MethodNode mn) {

        MethodNode wrapper = new MethodNode(ACC_PUBLIC, refName, "(I)V", null, null);

        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ILOAD, 1);
        System.out.println("Injecting wrapper for " + refName + " " + mn.name + " " + mn.desc);
        if (mn.desc.length() == 5)
            wrapper.visitInsn(ICONST_0);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, classNode.name, mn.name, mn.desc);
        wrapper.visitInsn(RETURN);
        wrapper.visitEnd();

        disarm(mn);

        classNode.methods.add(wrapper);
    }

    private boolean findPutOpcode(MethodNode mn) {
        if (mn.desc.startsWith("(I") && mn.desc.endsWith(")V") && Util.findInstruction(mn, new InsnNode(ATHROW))) {
            if (Util.findInstruction(mn, new InsnNode(I2B))
                    && Util.findInstruction(mn, new MethodInsnNode(INVOKEVIRTUAL, null, null, null))) {
                injectWrapper("putOpcode", mn);
                Printer.putOpcode = mn.name;
                methodName = mn.name;
                return true;
            }
        }
        return false;
    }

    public boolean run() {
        if (classNode.superName.equals(streamName)) {
            className = classNode.name;
            System.out.println("Injecting packetstream " + classNode.name);
            classNode.interfaces.add("org/spongebot/bot/accessors/IPacketStream");
            ListIterator<MethodNode> mnli = classNode.methods.listIterator();
            while (mnli.hasNext()) {
                MethodNode mn = mnli.next();
                if (findPutOpcode(mn))
                    break;
            }
            return true;
        }
        return false;
    }
}
