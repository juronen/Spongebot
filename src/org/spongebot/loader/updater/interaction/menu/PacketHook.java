package org.spongebot.loader.updater.interaction.menu;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.loader.updater.searching.Util;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

public class PacketHook implements Opcodes {

    private static int[] intConstants = {ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5};

    private String definition;

    public PacketHook(String definition) {
        this.definition = definition;
    }

    private int getIntegerConstant(int opcode) {
        for (int i = 0; i < 6; i++)
            if (opcode == intConstants[i])
                return i;
        return -1;
    }

    private int getIntegerConstant(AbstractInsnNode intValue) {
        int val = getIntegerConstant(intValue.getOpcode());
        if (val == -1) {
            if (intValue instanceof IntInsnNode) {
                val = ((IntInsnNode) intValue).operand;
            } else if (intValue instanceof LdcInsnNode) {
                val = (int) ((LdcInsnNode) intValue).cst;
            } else {
                System.out.println("Could not convert int instruction to value !");
                return 0xB00B135;
                // ^ Ehh this isn't even necessary, I'm already making sure its doable from a higher level
            }
        }
        return val;
    }

    private AbstractInsnNode getNext(AbstractInsnNode a) {
        AbstractInsnNode next = a.getNext();
        if (next.getOpcode() == -1)
            next = next.getNext();
        return next;
    }

    private AbstractInsnNode getPrevious(AbstractInsnNode a) {
        AbstractInsnNode prev = a.getPrevious();
        if (prev.getOpcode() == -1)
            prev = prev.getPrevious();
        return prev;
    }

    // WORKS, FELT LIKE BUILDING A LINE OF MILLION DOMINOS
    public int[] mapOptionIndices(MethodNode menuCreator, boolean NPC) {
        if (!Util.findInstruction(menuCreator, new InsnNode(ATHROW)))
            return null;
        if (!Util.findInstruction(menuCreator, new FieldInsnNode(GETFIELD, definition, null, "[Ljava/lang/String;")))
            return null;
        Util.removeTrapCode(menuCreator);
        int[] vals = new int[5];
        ListIterator<AbstractInsnNode> ainli = menuCreator.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (ain.getOpcode() == GETFIELD && Util.getNext(ain).getOpcode() == ASTORE) {
                FieldInsnNode fin = (FieldInsnNode) ain;
                if (fin.owner.equals(definition) && fin.desc.equals("[Ljava/lang/String;")) {
                    AbstractInsnNode checkBool = Util.findInstructionSnake(menuCreator, new FieldInsnNode(GETSTATIC, null, null, "Z"), fin, false);
                    if (checkBool == null) {
                        System.out.println("Couldn't find bool check after getting objdef actions");
                        return null;
                    }
                    JumpInsnNode jin = (JumpInsnNode) getNext(checkBool);
                    AbstractInsnNode next;
                    // We want to go where it'd take us if the boolean was false
                    if (jin.getOpcode() == IFEQ) {
                        next = menuCreator.instructions.get(menuCreator.instructions.indexOf(jin.label)).getNext();
                    } else { //IFNE
                        jin = (JumpInsnNode) jin.getNext(); // This is going to be a GOTO
                        next = menuCreator.instructions.get(menuCreator.instructions.indexOf(jin.label)).getNext();
                    }
                    // It's going to get interesting here. They can do
                    // a) ALOAD array
                    //    IFNULL
                    //    GOTO
                    // b) ACONST_NULL // These top two can swap
                    //    ALOAD array
                    //    IF_ACMPEQ (can be swapped for IF_ACMPNE)
                    //    GOTO

                    // Let's check scenario A first, much nicer :)
                    boolean go = true;
                    if (getNext(next).getOpcode() == IFNULL) {
                        if (getNext(getNext(next)).getOpcode() == GOTO)
                            jin = (JumpInsnNode) getNext(getNext(next));
                        else {
                            go = false;
                            next = getNext(getNext(next));
                        }
                    } else { // Fuck
                        if (next.getOpcode() != ACONST_NULL) {
                            next = getNext(next);
                            jin = (JumpInsnNode) getNext(next);
                            if (next.getOpcode() != ACONST_NULL || getPrevious(next).getOpcode() != ALOAD) {
                                System.out.println("Couldn't find null test");
                                return null;
                            }
                        } else
                            jin = (JumpInsnNode) getNext(getNext(next));
                        if (jin.getOpcode() == IF_ACMPEQ) {
                            jin = (JumpInsnNode) getNext(jin);
                        } else if (jin.getOpcode() != IF_ACMPNE) {
                            System.out.println("Could not determine jump in null test");
                            return null;
                        }
                    }
                    if (go)
                        next = menuCreator.instructions.get(menuCreator.instructions.indexOf(jin.label)).getNext();
                    if (Util.match(next, new InsnNode(ICONST_4)) || Util.match(next, new IntInsnNode(BIPUSH, 4))) {
                        next = getNext(next);
                        if (next.getOpcode() == ISTORE) {
                            //VarInsnNode vin = (VarInsnNode)next;
                            // Okay, I'm fairly positive they'll keep using the same loop model here.
                            // Atleast it's the same for #15 and #16. Currently they're counting down from 4.
                            next = getNext(next);
                            if (next.getOpcode() == GOTO) {
                                jin = (JumpInsnNode) next;
                                next = menuCreator.instructions.get(menuCreator.instructions.indexOf(jin.label)).getNext();
                            }
                            next = getNext(next);
                            if (next.getOpcode() != IFLT) {
                                System.out.println("Loop model has changed");
                                return null;
                            }
                            next = getNext(next); // Hop over the IFLT
                            if (next.getOpcode() == GOTO) {
                                next = menuCreator.instructions.get(menuCreator.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                            }
                            // Next up, another null check
                            // a)
                            //   1. ACONST_NULL
                            //      ALOAD array
                            //      ILOAD index (loopCounter)
                            //      AALOAD
                            //   2. ALOAD array
                            //      ILOAD index (loopCounter)
                            //      AALOAD
                            //      ACONST_NULL
                            //
                            //      either one of the above two, and after that IF_ACMPNE or IF_ACMPEQ
                            //
                            // b) The much nicer case, nothing can be moved around :)
                            //    ALOAD array
                            //    ILOAD index (loopCounter)
                            //    AALOAD
                            //    IFNULL

                            if (next.getOpcode() == ACONST_NULL || next.getOpcode() == ALOAD) {
                                jin = (JumpInsnNode) getNext(getNext(getNext(getNext(next))));
                                if (getNext(getNext(getNext(next))).getOpcode() == IFNULL) {
                                    // ^ Hop over the ifnull and straight to the goto
                                    next = menuCreator.instructions.get(menuCreator.instructions.indexOf(jin.label)).getNext();
                                } else {
                                    if (jin.getOpcode() == IF_ACMPEQ) {
                                        next = menuCreator.instructions.get(menuCreator.instructions.indexOf(((JumpInsnNode) jin.getNext()).label)).getNext();
                                    } else if (jin.getOpcode() == IF_ACMPNE) {
                                        next = menuCreator.instructions.get(menuCreator.instructions.indexOf(jin.label)).getNext();
                                    } else {
                                        System.out.println("Failure passing through second null check #1");
                                    }
                                }
                            } else {
                                System.out.println("Failure passing through second null check #2");
                                return null;
                            }

                            if (NPC) {
                                // The NPC menu creator has a check for whether an option.equals("Attack")

                                for (int i = 0; i < 4; i++) {
                                    if (Util.match(next, new MethodInsnNode(INVOKEVIRTUAL, null, "equalsIgnoreCase", null)))
                                        break;
                                    next = getNext(next);
                                }

                                if (!Util.match(next, new MethodInsnNode(INVOKEVIRTUAL, null, "equalsIgnoreCase", null))) {
                                    System.out.println("Failure negating check for -Attack-");
                                    return null;
                                }

                                next = getNext(next);

                                if (next.getOpcode() == IFNE)
                                    next = getNext(next);
                                else if (next.getOpcode() != IFEQ) {
                                    System.out.println("Failure negating check for -Attack- 2");
                                    return null;
                                }

                                next = menuCreator.instructions.get(menuCreator.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                            }

                            // Next they'll initialize a local var to 0. I'll just go instruction by instruction
                            // here because I'll want to know if anything changes beyond expectations.
                            if (next.getOpcode() == ICONST_0) {
                                next = getNext(next);
                                if (next.getOpcode() == ISTORE) {
                                    // I can't really utilize my pattern searcher here because I can't just follow gotos,
                                    // but might also have to follow another branching instruction
                                    int actionIndex = -1;
                                    while (true) {
                                        next = getNext(next);
                                        if (next.getOpcode() == GOTO)
                                            next = menuCreator.instructions.get(menuCreator.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                                        if (actionIndex == -1) {
                                            actionIndex = getIntegerConstant(next.getOpcode());
                                        }
                                        if (next instanceof JumpInsnNode) {
                                            AbstractInsnNode intValue = null;
                                            if (next.getOpcode() == IF_ICMPNE || next.getOpcode() == IFNE) {
                                                intValue = getNext(next);
                                            } else if (next.getOpcode() == IF_ICMPEQ || next.getOpcode() == IFEQ)
                                                intValue = menuCreator.instructions.get(menuCreator.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                                            if (intValue.getOpcode() == GOTO) {
                                                intValue = menuCreator.instructions.get(menuCreator.instructions.indexOf(((JumpInsnNode) intValue).label)).getNext();
                                            }
                                            if (next.getOpcode() == IFEQ || next.getOpcode() == IFNE)
                                                actionIndex = 0;
                                            int val = getIntegerConstant(intValue);
                                            vals[actionIndex] = val;
                                            if (actionIndex == 4)
                                                return vals;
                                            actionIndex = -1;
                                            next = menuCreator.instructions.get(menuCreator.instructions.indexOf(((JumpInsnNode) next).label));
                                        }
                                    }
                                } else {
                                    System.out.println("O/N:" + NPC + " - Could not find ISTORE");
                                }
                            } else {
                                System.out.println("O/N:" + NPC + " - Could not find ICONST_0");

                            }
                        }
                    } else {
                        System.out.println("Huge failure in " + NPC);
                    }
                }
            }
        }
        return null;
    }

    public int[] mapItemOptionIndices(MethodNode mn) {
        if (!Util.findInstruction(mn, new FieldInsnNode(GETFIELD, definition, null, "[Ljava/lang/String;")))
            return null;
        Util.removeTrapCode(mn);
        int[] ret = new int[5];
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (ain.getOpcode() == ICONST_3) {
                if ((getNext(ain).getOpcode() == ILOAD && getNext(getNext(ain)) instanceof JumpInsnNode)
                        || (getNext(ain) instanceof JumpInsnNode && getPrevious(ain).getOpcode() == ILOAD)) {
                    AbstractInsnNode next = getNext(ain);
                    if (!(next instanceof JumpInsnNode))
                        next = getNext(next);

                    // This loop will basically cover 4 and 3, so we need to take the path that's >= 3
                    if (next.getOpcode() == IF_ICMPLT)
                        next = getNext(next);
                    else if (next.getOpcode() == IF_ICMPGE) {
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                    }

                    if (next.getOpcode() == GOTO)
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();

                    // Here we go again. They're checking the array for null. It's either aload ifnull, or
                    // aconst_null aload if..., where the first 2 can swap. meh.
                    next = getNext(next);
                    if (next.getOpcode() == IFNULL) {
                        next = getNext(next); // It's not null.
                    } else if (getNext(next) instanceof JumpInsnNode) {
                        next = getNext(next);
                        if (next.getOpcode() == IF_ACMPEQ) {
                            // Jump if array is null, it aint, so we skip
                            next = getNext(next);
                        } else if (next.getOpcode() == IF_ACMPNE) {
                            // Array isn't null, cool
                            next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                        } else {
                            System.out.println("Couldn't negate first null check..");
                            return null;
                        }
                    } else {
                        System.out.println("Couldn't negate first null check");
                        return null;
                    }

                    if (next.getOpcode() == GOTO)
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();

                    // Scroll down to next jump, they're checking the current arr element for null
                    next = Util.findInstructionSnake(mn, new JumpInsnNode(0, null), next, false);

                    if (next == null) {
                        System.out.println("Couldn't negate array element null check - #1");
                        return null;
                    }

                    if (next.getOpcode() == IFNULL) {
                        next = getNext(next);
                    } else {
                        if (next.getOpcode() == IF_ACMPEQ) {
                            // Jump if element is null, it aint, so we skip
                            next = getNext(next);
                        } else if (next.getOpcode() == IF_ACMPNE) {
                            // Element isn't null, cool
                            next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                        } else {
                            System.out.println("Couldn't negate array element null check - #2");
                            return null;
                        }
                    }

                    if (next.getOpcode() == GOTO)
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();

                    AbstractInsnNode jump;

                    // They'll check against 3, it's been so since 317
                    if (next.getOpcode() != ICONST_3) {
                        next = getNext(next);
                        jump = getNext(next);
                        if (next.getOpcode() != ICONST_3) {
                            System.out.println("Couldn't find ICONST_3");
                            return null;
                        }
                    } else
                        jump = getNext(getNext(next));

                    int a; // Don't jump
                    int b; // Jump

                    if (jump.getOpcode() == IF_ICMPNE) {
                        // If we follow the jump, we are going to where the value for #4 is given
                        a = 3;
                        b = 4;
                    } else {
                        // If we follow the jump, we are going to where the value for #3 is given
                        a = 4;
                        b = 3;
                    }

                    // 1. Let's not jump

                    next = getNext(jump);
                    if (next.getOpcode() == GOTO)
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                    int val = getIntegerConstant(next);
                    if (val == -1) {
                        System.out.println("Could not get action index code for #" + a + " : 1");
                        return null;
                    }
                    ret[a] = val;

                    // 2. Let's jump

                    next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) jump).label)).getNext();
                    if (next.getOpcode() == GOTO)
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                    val = getIntegerConstant(next);
                    if (val == -1) {
                        System.out.println("Could not get action index code for #" + b + " : 2");
                        return null;
                    }
                    ret[b] = val;


                    AbstractInsnNode[] pattern = new AbstractInsnNode[]{
                            new InsnNode(ICONST_2),
                            new VarInsnNode(ISTORE, Util.FLAG_IGNORE),
                            new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                            new JumpInsnNode(IFLT, null)
                    };

                    pattern = Util.findPattern(mn, pattern);
                    if (pattern == null) {
                        System.out.println("Could not find storing of 2 or Item option loop model has changed");
                        return null;
                    }

                    next = pattern[3];
                    //if (next.getOpcode() != IFLT) { // 2521
                    //    System.out.println("Item option loop model has changed");
                    //    return null;
                    //}

                    next = getNext(next);
                    if (next.getOpcode() == GOTO)
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();

                    next = Util.findInstructionSnake(mn, new JumpInsnNode(0, null), next, false);
                    if (next.getOpcode() == IFNULL) {
                        next = getNext(next);
                    } else {
                        // If they're doing this, they will have pushed a null,
                        // and then they're checking the element against null
                        if (next.getOpcode() == IF_ACMPEQ) {
                            next = getNext(next);
                        } else if (next.getOpcode() == IF_ACMPNE) {
                            next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                        } else {
                            System.out.println("Could not negate array element null check #3");
                            return null;
                        }
                    }

                    //next = getNext(next); <--- This used to be uncommented, and worked, but should not have. Wat.
                    if (next.getOpcode() == GOTO)
                        next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();

                    // Next they'll initialize a local var to 0. I'll just go instruction by instruction
                    // here because I'll want to know if anything changes beyond expectations.
                    if (next.getOpcode() == ICONST_0) {
                        next = getNext(next);
                        if (next.getOpcode() == ISTORE) {
                            // I can't really utilize my pattern searcher here because I can't just follow gotos,
                            // but might also have to follow another branching instruction
                            int actionIndex = -1;
                            while (true) {
                                next = getNext(next);
                                if (next.getOpcode() == GOTO)
                                    next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                                if (actionIndex == -1) {
                                    actionIndex = getIntegerConstant(next.getOpcode());
                                }
                                if (next instanceof JumpInsnNode) {
                                    AbstractInsnNode intValue = null;
                                    if (next.getOpcode() == IF_ICMPNE || next.getOpcode() == IFNE) {
                                        intValue = getNext(next);
                                    } else if (next.getOpcode() == IF_ICMPEQ || next.getOpcode() == IFEQ)
                                        intValue = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label)).getNext();
                                    if (intValue.getOpcode() == GOTO) {
                                        intValue = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) intValue).label)).getNext();
                                    }
                                    if (next.getOpcode() == IFEQ || next.getOpcode() == IFNE)
                                        actionIndex = 0;
                                    ret[actionIndex] = getIntegerConstant(intValue);
                                    if (actionIndex == 2)
                                        return ret;
                                    actionIndex = -1;
                                    next = mn.instructions.get(mn.instructions.indexOf(((JumpInsnNode) next).label));
                                }
                            }
                        } else {
                            System.out.println("ISTORE not found");
                        }
                    } else {
                        System.out.println("ICONST_0 not found");
                    }
                }
            }
        }
        return null;
    }

    public static boolean isMenuProcessor(MethodNode mn) {
        if (mn.desc.length() == 5 && mn.desc.endsWith(")V") && Modifier.isStatic(mn.access)) {
            AbstractInsnNode[] pat = new AbstractInsnNode[]{
                    new FieldInsnNode(GETSTATIC, "client", null, "[I"),
                    new VarInsnNode(ILOAD, 0),
                    new InsnNode(IALOAD),
                    new VarInsnNode(ISTORE, Util.FLAG_IGNORE),
                    new FieldInsnNode(GETSTATIC, "client", null, "[I"),
                    new VarInsnNode(ILOAD, 0),
                    new InsnNode(IALOAD),
                    new VarInsnNode(ISTORE, Util.FLAG_IGNORE),
                    new FieldInsnNode(GETSTATIC, "client", null, "[I"),
                    new VarInsnNode(ILOAD, 0),
                    new InsnNode(IALOAD),
                    new VarInsnNode(ISTORE, Util.FLAG_IGNORE),
                    new FieldInsnNode(GETSTATIC, "client", null, "[I"),
                    new VarInsnNode(ILOAD, 0),
                    new InsnNode(IALOAD),
                    new VarInsnNode(ISTORE, Util.FLAG_IGNORE)
            };
            return Util.findPattern(mn, pat) != null;
        }
        return false;
    }

    // type 0 = obj 1 = npc 2 = interface 3 = item
    public Map<String, Object> getPackets(MethodNode processAction, int actionCode, FieldInsnNode baseX, FieldInsnNode baseY, int type) {
        Map<String, Object> data = new LinkedHashMap();
        Util.removeTrapCode(processAction);
        AbstractInsnNode ain = processAction.instructions.getFirst();
        while (!Util.match(ain, new IntInsnNode(SIPUSH, 2000))) {
            ain = getNext(ain);
            if (ain == null)
                return null;
        }
        int varNum = ((VarInsnNode) (getNext(ain).getOpcode() == ILOAD ? getNext(ain) : getPrevious(ain))).var;
        // I'm too damn tired to make sure all the jumps go dandy, I'll just search from the beginning every time.
        ain = processAction.instructions.getFirst();
        while (ain != null) {
            while (ain.getOpcode() != IF_ICMPNE) {
                ain = getNext(ain);
            }
            AbstractInsnNode intValue = null;
            if (Util.match(getPrevious(ain), new VarInsnNode(ILOAD, varNum))) {
                intValue = getPrevious(getPrevious(ain));
            } else if (Util.match(getPrevious(getPrevious(ain)), new VarInsnNode(ILOAD, varNum)))
                intValue = getPrevious(ain);
            if (intValue != null)
                if (getIntegerConstant(intValue.getOpcode()) > -1 || intValue instanceof IntInsnNode || intValue instanceof LdcInsnNode) {
                    if (getIntegerConstant(intValue) == actionCode) {
                        String info = "";
                        while (data.size() < ((type == 1) ? 2 : 4)) {
                            ain = getNext(ain);
                            if (ain.getOpcode() == GOTO)
                                ain = processAction.instructions.get(processAction.instructions.indexOf(((JumpInsnNode) ain).label)).getNext();
                            if (ain.getOpcode() == GETSTATIC && type == 0) {
                                FieldInsnNode fin = (FieldInsnNode) ain;
                                if (fin.desc.equals("I")) {
                                    if (fin.name.equals(baseX.name) && fin.owner.equals(baseX.owner)) {
                                        info = "x";
                                    } else if (fin.name.equals(baseY.name) && fin.owner.equals(baseY.owner)) {
                                        info = "y";
                                    } else {
                                        System.out.println("Awkward! Found a getstatic that wasn't for XY!" + fin.name + " " + fin.owner);
                                        return null; // No such thing as partially correct here
                                    }
                                }
                            }
                            if (ain.getOpcode() == ILOAD) {
                                if (type == 1)
                                    info = "id";
                                else if (type == 2 || type == 3) {
                                    VarInsnNode vin = (VarInsnNode) ain;
                                    info = String.valueOf(vin.var);
                                }
                            } else if (type == 0 && ain.getOpcode() == ISHR)
                                info = "id";
                            if (ain.getOpcode() == INVOKEVIRTUAL) {
                                if (((MethodInsnNode) ain).desc.length() > 5)
                                    continue; // We don't need to grab the "clickObject" call here.
                                if (data.size() == 0) {
                                    AbstractInsnNode insn = getPrevious(getPrevious(ain));
                                    int opcode = getIntegerConstant(insn);
                                    data.put("opcode", opcode);
                                } else
                                    data.put(info, ain);
                                if (data.size() == ((type == 1) ? 2 : 4))
                                    return data;
                                info = "";
                            }
                        }
                        return null;
                    }
                }
            ain = getNext(ain);
        }
        return null;
    }


}
