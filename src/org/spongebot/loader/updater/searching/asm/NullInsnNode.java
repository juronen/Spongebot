package org.spongebot.loader.updater.searching.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;

public class NullInsnNode extends AbstractInsnNode {

    public NullInsnNode() {
        super(0);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void accept(MethodVisitor methodVisitor) {
    }

    @Override
    public AbstractInsnNode clone(Map<LabelNode, LabelNode> labelNodeLabelNodeMap) {
        return null;
    }
}
