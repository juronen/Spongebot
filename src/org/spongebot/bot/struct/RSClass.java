package org.spongebot.bot.struct;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public class RSClass {

    private ArrayList<RSField> fields = new ArrayList();
    private ArrayList<RSMethod> methods = new ArrayList();

    private String botClass;
    private String target;

    public RSClass(String botClass, String target) {
        this.botClass = botClass;
        this.target = target;
    }

    public void inject(ClassNode classNode) {
        if (!classNode.name.equals(target))
            return;
        if (!classNode.interfaces.contains(botClass))
            classNode.interfaces.add(botClass);
        for (RSField rsField : fields) {
            rsField.inject(classNode);
        }
        for (RSMethod rsMethod : methods) {
            rsMethod.inject(classNode);
        }
    }

    public void putField(RSField rsField) {
        fields.add(rsField);
    }
}
