package org.spongebot.loader.updater.spawns;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.Loader;
import org.spongebot.loader.updater.searching.Util;

public class ObjectSpawnInjector implements Opcodes {

    public static void run(ClassNode classNode) {
        if (!classNode.name.equals(Loader.botToClient("Region")))
            return;
        String spawnDescriptor = "(IIIIIIIIL" + Loader.botToClient("Renderable") + ";IZII)Z";
        String removeDescriptor = "(L" + Loader.botToClient("GameObject") + ";)V";
        for (MethodNode mn : classNode.methods) {

            if (mn.desc.equals(spawnDescriptor)) {
                System.out.println("Injected object spawn callback to " + mn.name + " in region.");
                AbstractInsnNode ain = mn.instructions.getFirst();
                while (!Util.match(ain, new FieldInsnNode(GETFIELD, Loader.botToClient("SceneTile"), null, "[L" + Loader.botToClient("GameObject") + ";")))
                    ain = ain.getNext();
                for (int i = 0; i < 6; i++)
                    ain = ain.getNext();
                mn.instructions.insertBefore(ain, new InsnNode(DUP));
                mn.instructions.insertBefore(ain, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/rs/spawncontrol/ObjectSpawn", "add", "(Lorg/spongebot/bot/accessors/IGameObject;)V"));
            }

            if (mn.desc.equals(removeDescriptor)) {
                System.out.println("Injected object remove callback to " + mn.name + " in region.");
                AbstractInsnNode ain = mn.instructions.getFirst();
                mn.instructions.insertBefore(ain, new VarInsnNode(ALOAD, 1));
                mn.instructions.insertBefore(ain, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/rs/spawncontrol/ObjectSpawn", "remove", "(Lorg/spongebot/bot/accessors/IGameObject;)V"));
            }
        }
    }
}
