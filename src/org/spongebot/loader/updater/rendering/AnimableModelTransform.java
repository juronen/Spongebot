package org.spongebot.loader.updater.rendering;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

import java.lang.reflect.Constructor;

public class AnimableModelTransform implements Opcodes {

    public static void run(ClassNode classNode) {
        if (classNode.superName.equals(Loader.botToClient("Renderable"))) {
            Class<?> clazz = classNode.getClass();
            for (Constructor<?> constructor : clazz.getConstructors()) {
                String desc = Type.getConstructorDescriptor(constructor);
                if (desc.startsWith("([I")) {
                    for (MethodNode mn : classNode.methods) {
                        disableLighting(mn);
                    }
                    return;
                }
            }
        }
    }

    public static void disableLighting(MethodNode mn) {
        if (Util.countInstruction(mn, new IntInsnNode(SIPUSH, 8192)) == 3) {
            Util.addReturn(mn, "lighting");
        }
    }
}
