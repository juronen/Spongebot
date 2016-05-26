package org.spongebot.loader.updater.stream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map.Entry;

public class StreamTransform implements Opcodes {

    private ClassNode classNode;

    public StreamTransform(ClassNode classNode) {
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
                    // ^ Let's always branch to the happy place!
                }
            }
        }
    }

    private void injectWrapper(String newName, MethodNode mn) {
        System.out.println("Injecting wrapper for " + newName + " " + mn.desc);

        String desc;
        if (newName.contains("Long"))
            desc = "(J)V";
        else
            desc = "(I)V";

        MethodNode wrapper = new MethodNode(ACC_PUBLIC, newName, desc, null, null);

        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);

        if (newName.contains("Long"))
            wrapper.visitVarInsn(LLOAD, 1);
        else
            wrapper.visitVarInsn(ILOAD, 1);

        if (mn.desc.length() == 5)
            wrapper.visitInsn(ICONST_0);

        wrapper.visitMethodInsn(INVOKEVIRTUAL, classNode.name, mn.name, mn.desc);
        wrapper.visitInsn(RETURN);
        wrapper.visitEnd();

        Util.removeTrapCode(mn);

        classNode.methods.add(wrapper);
    }


    public boolean run() {
        if (!classNode.superName.equals(Loader.botToClient("Node")))
            return false;
        if (classNode.methods.size() < 20)
            return false;
        boolean inStream = false;
        ListIterator<MethodNode> mnli = classNode.methods.listIterator();
        while (mnli.hasNext()) {
            MethodNode mn = mnli.next();
            if (mn.name.equals("<init>")) {
                if (mn.desc.equals("([B)V")) {
                    inStream = true;
                }
            } else if (mn.desc.endsWith("[F"))
                return false;
        }
        if (!inStream)
            return false;
        // Create wrappers for every put/write method
        HashMap<String, MethodNode> wrappers = new HashMap();
        for (MethodNode mn : classNode.methods) {
            // We want anything that has the desc (XX)V
            if (mn.desc.length() == 5 && mn.desc.endsWith(")V") && !mn.name.contains("init")) {
                wrappers.put("_" + mn.name, mn);
            }
        }
        // We need to do this to avoid a concurrent modification
        for (Entry<String, MethodNode> e : wrappers.entrySet()) {
            injectWrapper(e.getKey(), e.getValue());
        }
        PacketStreamTransform.streamName = classNode.name;
        System.out.println("Stream identified as " + classNode.name);
        return true;
    }


}
