package org.spongebot.bot.struct;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class RSMethod implements Opcodes {

    private String refactoredName;
    private String desc;
    private String name;

    public RSMethod(String refactoredName, String desc, String name) {
        this.refactoredName = refactoredName;
        this.desc = desc;
        this.name = name;
    }

    public void inject(ClassNode classNode) {
        MethodNode mn = new MethodNode(ACC_PUBLIC, refactoredName, desc, null, null);
        mn.visitCode();
        mn.visitVarInsn(ALOAD, 0);
        for (int i = 1; i < Type.getArgumentTypes(desc).length + 1; i++) {
            System.out.println(Type.getArgumentTypes(desc)[i - 1].getClassName());
            switch (Type.getArgumentTypes(desc)[i - 1].getClassName()) {
                case "int":
                    mn.visitVarInsn(ILOAD, i);
                    break;
                case "long":
                    mn.visitVarInsn(LLOAD, i);
                    break;
                default:
                    mn.visitVarInsn(ALOAD, i);
                    break;
            }
        }
        mn.visitMethodInsn(INVOKEVIRTUAL, classNode.name, name, desc);
        Type returnType = Type.getReturnType(desc);
        switch (returnType.getClassName()) {
            case "int":
                mn.visitInsn(IRETURN);
                break;
            case "long":
                mn.visitInsn(LRETURN);
                break;
            default:
                mn.visitInsn(ARETURN);
                break;
        }
        mn.visitEnd();
        classNode.methods.add(mn);
    }
}
