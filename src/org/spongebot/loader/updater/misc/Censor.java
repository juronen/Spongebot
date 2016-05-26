package org.spongebot.loader.updater.misc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

import java.util.ListIterator;

public class Censor implements Opcodes {

    public static void add(ClassNode classNode) {
        FieldInsnNode fin = new FieldInsnNode(GETFIELD, Loader.botToClient("Player"), Loader.fields.get("Player").get("getName"), "Ljava/lang/String;");
        for (MethodNode mn : classNode.methods) {
            ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
            while (ainli.hasNext()) {
                AbstractInsnNode ain = ainli.next();
                if (Util.match(ain, fin)) {
                    mn.instructions.insert(ain, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/callbacks/UserCensor", "apply", "(Ljava/lang/String;)Ljava/lang/String;"));
                }
            }
        }
    }
}
