package org.spongebot.loader.updater.login;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.bot.rs.api.tools.Login;
import org.spongebot.loader.updater.Storage;
import org.spongebot.loader.updater.searching.Util;

import java.lang.reflect.Modifier;
import java.util.ListIterator;

public class LoginHook implements Opcodes {

    public static void run(ClassNode classNode) {
        for (MethodNode mn : classNode.methods) {
            if (mn.desc.length() == 4 && mn.desc.endsWith(")V") && Modifier.isStatic(mn.access) && Util.findInstruction(mn, new InsnNode(ATHROW))) {
                ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
                while (ainli.hasNext()) {
                    AbstractInsnNode ain = ainli.next();
                    if (Util.match(ain, new LdcInsnNode(9.9999999E7D))) {
                        AbstractInsnNode pass = Util.findInstructionSnake(mn, new FieldInsnNode(GETSTATIC, null, null, "Ljava/lang/String;"), ain, false);
                        if (pass != null) {
                            AbstractInsnNode user = Util.findInstructionSnake(mn, new FieldInsnNode(GETSTATIC, null, null, "Ljava/lang/String;"), pass.getNext(), false);
                            if (pass != null) {
                                FieldInsnNode fuser = (FieldInsnNode) user;
                                FieldInsnNode fpass = (FieldInsnNode) pass;
                                Login.loginUser = fuser.owner + "." + fuser.name;
                                Login.loginPass = fpass.owner + "." + fpass.name;
                                MethodNode login = new MethodNode(ACC_PUBLIC, "login", "()V", null, null);
                                login.visitCode();
                                login.visitInsn(ICONST_0);
                                login.visitMethodInsn(INVOKESTATIC, classNode.name, mn.name, mn.desc);
                                login.visitInsn(RETURN);
                                login.visitEnd();
                                Storage.methods.put("client.login", login);
                                System.out.println("Detected login method " + classNode.name + "." + mn.name + " - User field: " + fuser.owner + "." + fuser.name + " Pass field: " + fpass.owner + "." + fpass.name);
                                Util.removeTrapCode(mn);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
