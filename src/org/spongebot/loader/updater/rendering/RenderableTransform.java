package org.spongebot.loader.updater.rendering;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

public class RenderableTransform {

    public static void run(ClassNode classNode) {
        if (classNode.name.equals(Loader.botToClient("Renderable"))) {
            for (MethodNode mn : classNode.methods) {
                disableRenderAtPoint(mn);
            }
        }
    }

    public static void disableRenderAtPoint(MethodNode mn) {
        if (mn.desc.equals("(IIIIIIIII)V")) {
            Util.addReturn(mn, "renderAtPointR");
        }
    }
}
