package org.spongebot.loader.updater.definitions;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;


public class DefinitionFinder extends ClassVisitor implements Opcodes {

    private String returnType;
    private String defClassName;
    private MethodInsnNode target;
    private MethodNode getter;

    /*
     * classNode.accept(new DefinitionFinder("NPC", "av")) would attempt to find a call
     * to the NPC definition getter in the client, and if so, it will create a wrapper for it.
     * Note that you have to take care of the InvalidStateException yourself, I suggest just
     * swapping it out for a goto and destroying the two instructions above it.
     */
    public DefinitionFinder(String defClassName, String returnType) {
        super(ASM4);
        this.returnType = returnType;
        this.defClassName = defClassName;
    }

    public MethodInsnNode getTarget() {
        return target;
    }

    public MethodNode getGetter() {
        return getter;
    }

    private String innerDesc(String full) {
        String s = full.substring(full.indexOf("(") + 1);
        s = s.substring(0, s.indexOf(")"));
        return s;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // The NPC/Object/Item definition getters are static methods that return the definition. As for pretty much
        // any method in the client, there are dupes, but atleast for these dupes are not called, not even from
        // other duplicate methods, which makes using this very convenient.
        MethodVisitor mv = new MethodVisitor(ASM4) {
        };
        final String mn = name;
        final String nd = desc;
        InstructionAdapter ia = new InstructionAdapter(mv) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                super.visitMethodInsn(opcode, owner, name, desc);
                if (target == null) {
                    if (opcode == INVOKESTATIC && desc.endsWith(")L" + returnType + ";") && desc.startsWith("(I") && innerDesc(desc).length() == 2) {
                        System.out.println("Found definition getter " + owner + " " + name + " " + desc);
                        getter = new MethodNode(ACC_PUBLIC, "get" + defClassName + "Definition", "(I)Lorg/spongebot/bot/accessors/I" + defClassName + "Definition;", null, null);
                        getter.visitCode();
                        getter.visitVarInsn(ILOAD, 1);
                        getter.visitInsn(ICONST_0); // We have to disarm the target method later
                        getter.visitMethodInsn(opcode, owner, name, desc);
                        getter.visitInsn(ARETURN);
                        getter.visitEnd();
                        target = new MethodInsnNode(opcode, owner, name, desc);
                    }
                }
            }
        };
        return ia;
    }
}
