package org.spongebot.loader.updater.searching;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.updater.searching.asm.NullInsnNode;

import java.util.ListIterator;

public class Util implements Opcodes {

    public static final int FLAG_IGNORE = 9000;

    public static void addReturn(MethodNode mn, String bool) {
        if (!mn.desc.endsWith(")V") && !mn.desc.endsWith(")I"))
            return;
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        AbstractInsnNode first = mn.instructions.getFirst();
        mn.instructions.insertBefore(first, new LabelNode(l0));
        mn.instructions.insertBefore(first, new LdcInsnNode(bool));
        mn.instructions.insertBefore(first, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/Configuration", "isDrawing", "(Ljava/lang/String;)Z"));
        mn.instructions.insertBefore(first, new JumpInsnNode(IFEQ, new LabelNode(l1)));
        mn.instructions.insertBefore(first, new JumpInsnNode(GOTO, new LabelNode(l2)));
        mn.instructions.insertBefore(first, new LabelNode(l1));
        if (mn.desc.endsWith(")I")) {
            mn.instructions.insertBefore(first, new InsnNode(ICONST_0));
            mn.instructions.insertBefore(first, new InsnNode(IRETURN));
        } else
            mn.instructions.insertBefore(first, new InsnNode(RETURN));
        mn.instructions.insertBefore(first, new LabelNode(l2));
        System.out.println("Added return to " + mn.name);
    }

    public static AbstractInsnNode getNext(AbstractInsnNode a) {
        AbstractInsnNode next = a.getNext();
        if (next.getOpcode() == -1)
            next = next.getNext();
        return next;
    }

    public static AbstractInsnNode getPrevious(AbstractInsnNode a) {
        AbstractInsnNode prev = a.getPrevious();
        if (prev.getOpcode() == -1)
            prev = prev.getPrevious();
        return prev;
    }

    public static void removeTrapCode(MethodNode mn) {
        if (!Util.findInstruction(mn, new InsnNode(ATHROW)))
            return;
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (ain.getOpcode() == Opcodes.NEW) {
                TypeInsnNode tin = (TypeInsnNode) ain;
                if (tin.desc.contains("IllegalStateException")) {
                    int index = mn.instructions.indexOf(ain) - 3;
                    JumpInsnNode jin = (JumpInsnNode) getPrevious(ain);
                    LabelNode ln = jin.label;
                    mn.instructions.set(mn.instructions.get(index), new JumpInsnNode(GOTO, ln));
                    mn.instructions.insert(getPrevious(getPrevious(jin)), new InsnNode(ICONST_0));
                }
            } else if (ain.getOpcode() == Opcodes.RETURN && ain.getPrevious() instanceof JumpInsnNode) {
                int index = mn.instructions.indexOf(ain) - 3;
                JumpInsnNode jin = (JumpInsnNode) getPrevious(ain);
                LabelNode ln = jin.label;
                mn.instructions.set(mn.instructions.get(index), new JumpInsnNode(GOTO, ln));
                mn.instructions.insert(getPrevious(getPrevious(jin)), new InsnNode(ICONST_0));
            }
        }
    }

    public static int countInstructionStop(MethodNode mn, AbstractInsnNode instruction, AbstractInsnNode stop) {
        int count = 0;
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (match(ain, instruction))
                count++;
            if (match(ain, stop))
                break;
        }
        return count;
    }

    public static boolean findInstruction(MethodNode mn, AbstractInsnNode ain) {
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode next = ainli.next();
            if (match(next, ain))
                return true;
        }
        return false;
    }

    public static AbstractInsnNode getInstruction(MethodNode mn, AbstractInsnNode ain) {
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode next = ainli.next();
            if (match(next, ain))
                return next;
        }
        return null;
    }

    public static boolean findInstruction(MethodNode mn, AbstractInsnNode instruction, AbstractInsnNode start) {
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext())
            if (match(ainli.next(), start))
                break;
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (match(ain, instruction))
                return true;
        }
        return false;
    }

    public static AbstractInsnNode findInstructionSnake(MethodNode mn, AbstractInsnNode insn, AbstractInsnNode start, boolean strict) {
        int position = mn.instructions.indexOf(start);
        while (start != null) {
            if (start.getOpcode() == -1)
                start = start.getNext();
            if (start.getOpcode() == GOTO) {
                start = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) start).label)).getNext();
            }
            if (start.getOpcode() == -1)
                start = start.getNext();
            if (match(start, insn)) {
                return start;
            }
            if (strict)
                if (start.getOpcode() == insn.getOpcode())
                    return null;
            if (start.getOpcode() == RETURN || start.getOpcode() == IRETURN || start.getOpcode() == ARETURN)
                return null;
            start = getNext(start);
            if (start == null)
                return null;
            if (mn.instructions.indexOf(start) == position) {
                // Oh shit, we are back in square 1!
                return null;
            }
        }
        return null;
    }

    public static int countInstruction(MethodNode mn, AbstractInsnNode instruction) {
        int count = 0;
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (match(ain, instruction))
                count++;
        }
        return count;
    }

    public static AbstractInsnNode[] findPattern(MethodNode mn, AbstractInsnNode[] pattern) {
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        i:
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            AbstractInsnNode[] fill = new AbstractInsnNode[pattern.length];
            for (int i = 0; i < pattern.length; i++) {
                if (ain == null)
                    continue i;
                if (ain.getOpcode() == Opcodes.GOTO) {
                    try {
                        ain = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) ain).label)).getNext();
                    } catch (Exception e) {
                        continue i;
                    }
                }
                if (ain.getOpcode() == -1)
                    ain = ain.getNext();
                fill[i] = ain;
                if (!match(ain, pattern[i]))
                    continue i;
                ain = getNext(ain);
            }
            return fill;
        }
        return null;
    }

    public static AbstractInsnNode[] findPattern(MethodNode mn, AbstractInsnNode[] pattern, AbstractInsnNode start) {
        LabelNode jumpMarker = null;
        if (start == null)
            return null;
        AbstractInsnNode ain = start;
        i:
        while (ain.getNext() != null) {
            ain = ain.getNext();
            AbstractInsnNode[] fill = new AbstractInsnNode[pattern.length];
            AbstractInsnNode next = mn.instructions.get(mn.instructions.indexOf(ain));
            for (int i = 0; i < pattern.length; i++) {
                if (next == null)
                    continue i;
                if (next.getOpcode() == Opcodes.GOTO) {
                    JumpInsnNode jin = (JumpInsnNode) next;
                    if (jumpMarker == null)
                        jumpMarker = jin.label;
                    else if (jin.label.equals(jumpMarker))
                        return null;
                    next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                }
                if (next.getOpcode() == -1)
                    next = next.getNext();
                fill[i] = next;
                if (!match(next, pattern[i])) {
                    continue i;
                }
                next = next.getNext();
            }
            return fill;
        }
        return null;
    }

    // Snakes through labels
    public static AbstractInsnNode[] findPatternSnake(MethodNode mn, AbstractInsnNode[] pattern, AbstractInsnNode start) {
        LabelNode jumpMarker = null;
        if (start == null)
            return null;
        AbstractInsnNode ain = start;
        i:
        while (ain.getNext() != null) {
            AbstractInsnNode[] fill = new AbstractInsnNode[pattern.length];
            AbstractInsnNode next = mn.instructions.get(mn.instructions.indexOf(ain));
            for (int i = 0; i < pattern.length; i++) {
                if (next == null)
                    return null;
                if (next.getOpcode() == Opcodes.GOTO) {
                    JumpInsnNode jin = (JumpInsnNode) next;
                    if (jumpMarker == null)
                        jumpMarker = jin.label;
                    else if (jin.label.equals(jumpMarker)) // If we've already done this jump, get out
                        return null;
                    next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                }
                if (next.getOpcode() == -1) // We don't want labels interfering
                    next = next.getNext();
                fill[i] = next;
                if (i == 0) {
                    ain = next.getNext();
                    if (ain == null)
                        return null;
                }
                if (!match(next, pattern[i])) {
                    continue i;
                }
                next = next.getNext();
            }
            return fill;
        }
        return null;
    }

    public static boolean match(AbstractInsnNode haystack, AbstractInsnNode needle) {
        if (needle instanceof MethodInsnNode)
            return match(haystack, (MethodInsnNode) needle);
        else if (needle instanceof FieldInsnNode)
            return match(haystack, (FieldInsnNode) needle);
        else if (needle instanceof IntInsnNode)
            return match(haystack, (IntInsnNode) needle);
        else if (needle instanceof TypeInsnNode)
            return match(haystack, (TypeInsnNode) needle);
        else if (needle instanceof VarInsnNode)
            return match(haystack, (VarInsnNode) needle);
        else if (needle instanceof LdcInsnNode)
            return match(haystack, (LdcInsnNode) needle);
        else if (needle instanceof JumpInsnNode)
            return haystack instanceof JumpInsnNode;
        else if (needle instanceof NullInsnNode)
            return true;
        return haystack.getOpcode() == needle.getOpcode();
    }

    public static boolean match(AbstractInsnNode a, MethodInsnNode b) {
        if (!(a instanceof MethodInsnNode))
            return false;
        if (b.getOpcode() == 0)
            return true;
        MethodInsnNode c = (MethodInsnNode) a;
        if (b.desc != null)
            if (!b.desc.equals(c.desc))
                return false;
        if (b.owner != null)
            if (!b.owner.equals(c.owner))
                return false;
        if (b.name != null)
            if (!b.name.equals(c.name))
                return false;
        return c.getOpcode() == b.getOpcode();
    }

    public static boolean match(AbstractInsnNode a, FieldInsnNode b) {
        if (!(a instanceof FieldInsnNode))
            return false;
        if (b.getOpcode() == 0)
            return true;
        FieldInsnNode c = (FieldInsnNode) a;
        if (b.desc != null)
            if (!b.desc.equals(c.desc))
                return false;
        if (b.owner != null)
            if (!b.owner.equals(c.owner))
                return false;
        if (b.name != null)
            if (!b.name.equals(c.name))
                return false;
        return c.getOpcode() == b.getOpcode();
    }

    public static boolean match(AbstractInsnNode a, IntInsnNode b) {
        if (!(a instanceof IntInsnNode))
            return false;
        if (b.getOpcode() == 0)
            return true;
        IntInsnNode c = (IntInsnNode) a;
        if (b.operand != Util.FLAG_IGNORE)
            if (c.operand != b.operand)
                return false;
        return c.getOpcode() == b.getOpcode();
    }

    public static boolean match(AbstractInsnNode a, TypeInsnNode b) {
        if (!(a instanceof TypeInsnNode))
            return false;
        if (b.getOpcode() == 0)
            return true;
        TypeInsnNode c = (TypeInsnNode) a;
        if (c.desc != null)
            if (!c.desc.equals(b.desc))
                return false;
        return c.getOpcode() == b.getOpcode();
    }

    public static boolean match(AbstractInsnNode a, VarInsnNode b) {
        if (!(a instanceof VarInsnNode))
            return false;
        if (b.getOpcode() == 0)
            return true;
        VarInsnNode c = (VarInsnNode) a;
        if (b.var != Util.FLAG_IGNORE)
            if (c.var != b.var)
                return false;
        return c.getOpcode() == b.getOpcode();
    }

    public static boolean match(AbstractInsnNode a, LdcInsnNode b) {
        if (!(a instanceof LdcInsnNode))
            return false;
        if (b.getOpcode() == 0)
            return true;
        LdcInsnNode c = (LdcInsnNode) a;
        if (b.cst != null)
            if (!c.cst.equals(b.cst))
                return false;
        return c.getOpcode() == b.getOpcode();
    }
}
