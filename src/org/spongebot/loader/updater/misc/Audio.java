package org.spongebot.loader.updater.misc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Audio implements Opcodes {

    public static void disable(ClassNode classNode) {
        boolean audioClass = false;
        for (MethodNode mn : classNode.methods) {
            if (mn.exceptions.contains("javax/sound/sampled/LineUnavailableException")) {
                // Found the right class
                audioClass = true;
                break;
            }
        }
        if (audioClass) {
            for (MethodNode mn : classNode.methods) {
                if (!mn.name.contains("init")) {
                    mn.instructions.insertBefore(mn.instructions.getFirst(), new InsnNode(RETURN));
                }
            }
        }
    }
}
