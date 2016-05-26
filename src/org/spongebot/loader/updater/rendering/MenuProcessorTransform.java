package org.spongebot.loader.updater.rendering;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

public class MenuProcessorTransform implements Opcodes {

    public static void run(ClassNode classNode) {
        for (MethodNode mn : classNode.methods) {
            if (mn.desc.startsWith("([L" + Loader.botToClient("Widget") + ";IIIIIIII")) {
                System.out.println("Menutrans " + classNode.name + " " + mn.name);
                Util.addReturn(mn, "menuProcessor");
            }
        }
    }
}
