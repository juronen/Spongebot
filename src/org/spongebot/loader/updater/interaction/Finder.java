package org.spongebot.loader.updater.interaction;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.RSClassLoader;
import org.spongebot.loader.updater.Storage;
import org.spongebot.loader.updater.searching.Util;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ListIterator;

public class Finder implements Opcodes {

    public static void createWalk(ClassNode classNode) {
        if (Storage.methods.containsKey("client.walk"))
            return;
        ListIterator<MethodNode> mnli = classNode.methods.listIterator();
        while (mnli.hasNext()) {
            MethodNode mn = mnli.next();
            if (mn.desc.startsWith("(IIIIZIIIIII") && mn.desc.length() == 15) {
                System.out.println("Walk " + classNode.name + " " + mn.name);
                MethodNode walk = new MethodNode(ACC_PUBLIC, "walk", "(IIIIZIIIIII)Z", null, null);
                walk.visitCode();
                walk.visitVarInsn(ILOAD, 1);
                walk.visitVarInsn(ILOAD, 2);
                walk.visitVarInsn(ILOAD, 3);
                walk.visitVarInsn(ILOAD, 4);
                walk.visitVarInsn(ILOAD, 5);
                walk.visitVarInsn(ILOAD, 6);
                walk.visitVarInsn(ILOAD, 7);
                walk.visitVarInsn(ILOAD, 8);
                walk.visitVarInsn(ILOAD, 9);
                walk.visitVarInsn(ILOAD, 10);
                walk.visitVarInsn(ILOAD, 11);
                walk.visitInsn(ICONST_0);
                walk.visitMethodInsn(INVOKESTATIC, classNode.name, mn.name, mn.desc);
                walk.visitInsn(IRETURN);
                walk.visitEnd();
                Storage.methods.put("client.walk", walk);
                RSClassLoader.addDisarmTarget(new MethodInsnNode(0, classNode.name, mn.name, mn.desc));
            }
        }
    }

    public static void createClickObject(ClassNode classNode) {
        if (Storage.methods.containsKey("client.clickObject"))
            return;
        m:
        for (MethodNode mn : classNode.methods) {
            if (mn.desc.length() == 7 && mn.desc.endsWith(")Z") && Modifier.isStatic(mn.access)) { // three real ints, one "junk"
                AbstractInsnNode ain = mn.instructions.getFirst();
                while (ain != null) {
                    if (Util.match(ain, new VarInsnNode(ISTORE, 4)))
                        break;
                    ain = ain.getNext();
                }
                if (ain != null) {
                    AbstractInsnNode[] pat = new AbstractInsnNode[]{
                            new InsnNode(IAND),
                            new IntInsnNode(SIPUSH, 32767),
                            new InsnNode(ISHR),
                            new IntInsnNode(BIPUSH, 14)
                    };
                    AbstractInsnNode cur = ain.getPrevious();
                    for (AbstractInsnNode insn : pat) {
                        if (!Util.match(cur, insn))
                            continue m;
                        cur = cur.getPrevious();
                    }
                    MethodNode clickObject = new MethodNode(ACC_PUBLIC, "clickObject", "(III)V", null, null);
                    RSClassLoader.addDisarmTarget(new MethodInsnNode(0, classNode.name, mn.name, mn.desc));
                    clickObject.visitCode();
                    clickObject.visitVarInsn(ILOAD, 1);
                    clickObject.visitVarInsn(ILOAD, 2);
                    clickObject.visitVarInsn(ILOAD, 3);
                    clickObject.visitLdcInsn(0);
                    clickObject.visitMethodInsn(INVOKESTATIC, classNode.name, mn.name, mn.desc);
                    clickObject.visitInsn(POP);
                    clickObject.visitInsn(RETURN);
                    Storage.methods.put("client.clickObject", clickObject);
                }
            }
        }
    }

    public static void createGetUID(ClassNode classNode, List<MethodInsnNode> candidates) {
        if (classNode.name.equals(Loader.botToClient("Region"))) {
            MethodNode meth = null;
            for (MethodNode mn : classNode.methods) {
                if (mn.desc.equals("(III)I") && !Modifier.isStatic(mn.access)) {
                    boolean b = false;
                    for (MethodInsnNode min : candidates) {
                        if (min.name.equals(mn.name))
                            b = true;
                    }
                    if (!b)
                        continue;

                    AbstractInsnNode[] pat = new AbstractInsnNode[]{
                            new VarInsnNode(ALOAD, 0),
                            new FieldInsnNode(GETFIELD, null, null, null),
                            new VarInsnNode(ILOAD, 1),
                            new InsnNode(AALOAD),
                            new VarInsnNode(ILOAD, 2),
                            new InsnNode(AALOAD),
                            new VarInsnNode(ILOAD, 3),
                            new InsnNode(AALOAD),
                            new VarInsnNode(ASTORE, 4)
                    };
                    if (Util.findPattern(mn, pat) != null) {
                        pat = new AbstractInsnNode[]{
                                new IntInsnNode(BIPUSH, 29),
                                new InsnNode(ISHR),
                                new InsnNode(ICONST_3),
                                new InsnNode(IAND)
                        };
                        if (Util.findPattern(mn, pat) != null) {
                            meth = new MethodNode(ACC_PUBLIC, "getGameObjectUID", "(III)I", null, null);
                            meth.visitCode();
                            meth.visitVarInsn(ALOAD, 0);
                            meth.visitVarInsn(ILOAD, 1);
                            meth.visitVarInsn(ILOAD, 2);
                            meth.visitVarInsn(ILOAD, 3);
                            meth.visitMethodInsn(INVOKEVIRTUAL, classNode.name, mn.name, "(III)I");
                            meth.visitInsn(IRETURN);
                            meth.visitEnd();
                        }
                    }
                }
            }
            if (meth != null)
                classNode.methods.add(meth);
        }
    }
}
