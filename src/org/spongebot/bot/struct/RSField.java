package org.spongebot.bot.struct;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class RSField implements Opcodes {

    private String desc;
    private String name;
    private String refactoredName;
    private String refactoredType;

    private int multiplier;

    public RSField(String refactoredName, String refactoredType, String desc, String name) {
        this.desc = desc;
        this.name = name;
        this.refactoredName = refactoredName;
        this.multiplier = 0;
        this.refactoredType = refactoredType;
    }

    public RSField(String refactoredName, String refactoredType, String desc, String name, int multiplier) {
        this.desc = desc;
        this.name = name;
        this.refactoredName = refactoredName;
        this.multiplier = multiplier;
        this.refactoredType = refactoredType;
    }

    public void inject(ClassNode classNode) {
        MethodNode mn = new MethodNode(ACC_PUBLIC, refactoredName, "()" + refactoredType, null, null);
        System.out.println("Injecting: " + refactoredName + " " + "()" + refactoredType + " into " + classNode.name + " to get " + name);
        mn.visitCode();
        mn.visitVarInsn(ALOAD, 0);
        mn.visitFieldInsn(GETFIELD, classNode.name, name, desc);
        if (multiplier != 0) {
            mn.visitLdcInsn(multiplier);
            mn.visitInsn(IMUL);
        }
        mn.visitInsn(desc.equals("I") ? IRETURN : ARETURN);
        mn.visitEnd();
        classNode.methods.add(mn);
    }
}
