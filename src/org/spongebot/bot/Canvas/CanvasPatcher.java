package org.spongebot.bot.canvas;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class CanvasPatcher {

    public static void patchConstructor(ClassNode classNode) {
        ListIterator<MethodNode> mnli = classNode.methods.listIterator();
        while (mnli.hasNext()) {
            MethodNode mn = mnli.next();
            if (mn.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
                while (ainli.hasNext()) {
                    AbstractInsnNode ain = ainli.next();
                    if (ain.getOpcode() == Opcodes.INVOKESPECIAL) {
                        MethodInsnNode min = (MethodInsnNode) ain;
                        if (min.owner.equals("java/awt/Canvas") && min.name.equals("<init>")) {
                            mn.instructions.set(min, new MethodInsnNode(Opcodes.INVOKESPECIAL, "org/spongebot/bot/canvas/BotCanvas", "<init>", "()V"));
                            return;
                        }
                    }
                }
            }
        }
    }
}
