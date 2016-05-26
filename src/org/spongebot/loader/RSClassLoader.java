package org.spongebot.loader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongebot.bot.canvas.CanvasPatcher;
import org.spongebot.bot.script.ScreenMover;
import org.spongebot.bot.struct.RSClass;
import org.spongebot.loader.updater.Storage;
import org.spongebot.loader.updater.definitions.DefinitionFinder;
import org.spongebot.loader.updater.interaction.Finder;
import org.spongebot.loader.updater.interaction.menu.PacketHook;
import org.spongebot.loader.updater.login.LoginHook;
import org.spongebot.loader.updater.misc.Audio;
import org.spongebot.loader.updater.searching.Util;
import org.spongebot.loader.updater.stream.PacketStreamTransform;
import org.spongebot.loader.updater.stream.StreamTransform;
import org.znu.core.structures.export.CompactClass;
import org.znu.core.structures.export.CompactField;

import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RSClassLoader extends ClassLoader implements Opcodes {

    private JarFile jarFile;

    private List<RSClass> rsClasses;

    private CompactClass client;

    private HashMap<String, ClassNode> classes = new HashMap();

    private static List<MethodInsnNode> disarmTargets = new ArrayList();

    public static HashMap<String, Class<?>> defined = new HashMap();

    public RSClassLoader(JarFile jarFile, List<RSClass> rsClasses, CompactClass client) {
        this.jarFile = jarFile;
        this.rsClasses = rsClasses;
        this.client = client;
        this.loadClasses();
    }


    public static void addDisarmTarget(MethodInsnNode min) {
        disarmTargets.add(min);
    }

    // This will allow scripts to conveniently get progress data
    private void addSkillCallback(ClassNode classNode, String varName, String bMethod) {
        ListIterator<MethodNode> mnli = classNode.methods.listIterator();
        while (mnli.hasNext()) {
            MethodNode mn = mnli.next();
            if (mn.desc.contains(";"))
                continue;
            ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
            while (ainli.hasNext()) {
                AbstractInsnNode ain = ainli.next();
                if (Util.match(ain, (AbstractInsnNode) new FieldInsnNode(Opcodes.GETSTATIC, "client", varName, "[I"))) {
                    AbstractInsnNode next = ain.getNext().getNext().getNext().getNext();

                    if (next.getOpcode() != Opcodes.GETSTATIC) {
                        continue;
                    }

                    int skillID = ((VarInsnNode) ain.getNext()).var;

                    mn.instructions.insertBefore(ain, new VarInsnNode(ILOAD, skillID));

                    // New value
                    mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ILOAD, ((VarInsnNode) ain.getNext().getNext()).var));

                    // Old value
                    mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETSTATIC, "client", varName, "[I"));
                    mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ILOAD, skillID));
                    mn.instructions.insertBefore(ain, new InsnNode(Opcodes.IALOAD));

                    // Difference
                    mn.instructions.insertBefore(ain, new InsnNode(Opcodes.ISUB));

                    mn.instructions.insertBefore(ain, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/spongebot/bot/callbacks/SkillCallback", bMethod, "(II)V"));

                    System.out.println("Injected skill callback " + bMethod);
                    break;
                }
            }
        }
    }

    private void addOpcodePrinter(ClassNode classNode) {
        ListIterator<MethodNode> mnli = classNode.methods.listIterator();
        while (mnli.hasNext()) {
            MethodNode mn = mnli.next();
            if (mn.name.equals(PacketStreamTransform.methodName) && classNode.name.equals(PacketStreamTransform.className)) {
                AbstractInsnNode first = mn.instructions.get(0);
                mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ILOAD, 1));
                mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/spongebot/bot/callbacks/Printer", "printOpcode", "(I)V"));
            } else if (classNode.name.equals(PacketStreamTransform.streamName)) {
                if (!mn.name.contains("init") && mn.desc.endsWith(")V") && !mn.desc.contains("[") && (mn.desc.startsWith("(I") || mn.desc.startsWith("(J") || mn.desc.startsWith("(Ljava/lang/String;"))) {
                    AbstractInsnNode first = mn.instructions.get(0);
                    mn.instructions.insertBefore(first, new LdcInsnNode(mn.name));
                    if (mn.desc.startsWith("(I")) {
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ILOAD, 1));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/spongebot/bot/callbacks/Printer", "print", "(Ljava/lang/String;I)V"));
                    } else if (mn.desc.startsWith("(J")) {
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.LLOAD, 1));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/spongebot/bot/callbacks/Printer", "print", "(Ljava/lang/String;J)V"));
                    } else if (mn.desc.startsWith("(Ljava/lang/String;")) {
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/spongebot/bot/callbacks/Printer", "print", "(Ljava/lang/String;Ljava/lang/String;)V"));
                    }
                }
            }
        }
    }

    private void hideAddress(MethodNode mn) {
        if (!Util.findInstruction(mn, new LdcInsnNode("%dns")))
            return;
        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
        while (ainli.hasNext()) {
            AbstractInsnNode ain = ainli.next();
            if (ain.getOpcode() == LDC) {
                LdcInsnNode lin = (LdcInsnNode) ain;
                if (lin.cst instanceof String) {
                    if (((String) lin.cst).length() == 0) {
                        mn.instructions.insertBefore(lin, new LdcInsnNode("Last logged in from: SKYNET"));
                        mn.instructions.insertBefore(lin, new InsnNode(ARETURN));
                        System.out.println("Address censored");
                        return;
                    }
                }
            }
        }
    }

    private boolean isObjMenuCreator(MethodNode mn) {
        AbstractInsnNode[] pat = new AbstractInsnNode[]{
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                new InsnNode(IADD),
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                new InsnNode(IADD),
                new MethodInsnNode(INVOKESTATIC, null, null, null),
        };
        AbstractInsnNode[] pat2 = new AbstractInsnNode[]{
                new IntInsnNode(BIPUSH, 7),
                new InsnNode(ISHR)
        };
        return Util.findInstruction(mn, new InsnNode(ATHROW)) && Util.findPattern(mn, pat) != null && Util.findPattern(mn, pat2) != null;
    }

    private boolean isNPCMenuCreator(MethodNode mn) {
        return Util.findInstruction(mn, new InsnNode(ATHROW)) && Util.findInstruction(mn, new IntInsnNode(SIPUSH, 400))
                && Util.findInstruction(mn, new FieldInsnNode(GETFIELD, Loader.botToClient("NPCDefinition"), null, "[Ljava/lang/String;"));
    }

    private boolean isWidgetMenuCreator(MethodNode mn) {
        return Util.findInstruction(mn, new InsnNode(ATHROW)) && mn.desc.startsWith("(L" + Loader.botToClient("Widget") + ";") && mn.desc.length() >= 9 && mn.desc.endsWith(")V");
    }

    private void loadClasses() {
        String[] baseX = client.getFields().get("getBaseX").getName().split("\\.");
        String[] baseY = client.getFields().get("getBaseY").getName().split("\\.");
        List<MethodInsnNode> uidCandidates = new ArrayList();
        try {
            Enumeration<?> enumeration = jarFile.entries();

            int[] npcIndices = null;
            int[] objIndices = null;
            int[] intIndices = null;
            int[] itemIndices = null;

            PacketHook objPacketHook = new PacketHook(Loader.botToClient("ObjectDefinition"));
            PacketHook npcPacketHook = new PacketHook(Loader.botToClient("NPCDefinition"));
            //PacketHook interfacePacketHook = new PacketHook(Loader.botToClient("Widget"));
            PacketHook itemPacketHook = new PacketHook(Loader.botToClient("ItemDefinition"));

            HashMap<String, String> definitions = new HashMap();
            definitions.put("NPC", Loader.botToClient("NPCDefinition"));
            definitions.put("Object", Loader.botToClient("ObjectDefinition"));
            definitions.put("Item", Loader.botToClient("ItemDefinition"));

            DefinitionFinder[] dfs = new DefinitionFinder[definitions.size()];
            for (int i = 0; i < dfs.length; i++) {
                dfs[i] = new DefinitionFinder((String) definitions.keySet().toArray()[i], (String) definitions.values().toArray()[i]);
            }

            boolean streamDone = false;

            AbstractInsnNode[] regionPat = new AbstractInsnNode[]{
                    new MethodInsnNode(INVOKEVIRTUAL, Loader.botToClient("Region"), null, "(III)I")
            };

            while (enumeration.hasMoreElements()) {
                JarEntry entry = (JarEntry) enumeration.nextElement();
                if (entry.getName().endsWith(".class")) {
                    ClassReader classReader = new ClassReader(jarFile.getInputStream(entry));
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                    if (!streamDone)
                        if (new StreamTransform(classNode).run())
                            streamDone = true;

                    ListIterator<MethodNode> mnli = classNode.methods.listIterator();
                    while (mnli.hasNext()) {

                        MethodNode mn = mnli.next();

                        AbstractInsnNode[] ret = Util.findPattern(mn, regionPat);
                        while (ret != null) {
                            uidCandidates.add((MethodInsnNode) ret[0]);
                            ret = Util.findPattern(mn, regionPat, ret[0].getNext());
                        }

                        if (objIndices == null) {
                            objIndices = objPacketHook.mapOptionIndices(mn, false);
                        }

                        if (isNPCMenuCreator(mn)) {
                            if (npcIndices == null) {
                                npcIndices = npcPacketHook.mapOptionIndices(mn, true);
                            }
                        }

                        if (isWidgetMenuCreator(mn)) {
                            /* Jagex did some refactoring here too, interface packets are now handled in a way that
                            makes sense. */
                            if (itemIndices == null) {
                                itemIndices = itemPacketHook.mapItemOptionIndices(mn);
                            }
                        }
                    }

                    for (DefinitionFinder df : dfs) {
                        if (df.getTarget() != null)
                            continue;
                        classNode.accept(df);
                    }

                    LoginHook.run(classNode);

                    Finder.createWalk(classNode);
                    Finder.createClickObject(classNode);

                    classes.put(classNode.name, classNode);
                }
            }

            for (DefinitionFinder df : dfs) {
                MethodInsnNode target = df.getTarget();
                if (target != null) {
                    MethodNode getter = df.getGetter();
                    Storage.methods.put("client." + getter.name, getter);
                    disarmTargets.add(target);
                }
            }

            enumeration = jarFile.entries();
            classes.clear();

            while (enumeration.hasMoreElements()) {
                JarEntry entry = (JarEntry) enumeration.nextElement();
                if (entry.getName().endsWith(".class")) {
                    ClassReader classReader = new ClassReader(jarFile.getInputStream(entry));
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                    if (classNode.superName.equals("java/awt/Canvas")) {
                        System.out.println("Replacing canvas in class: " + classNode.name);
                        classNode.visit(classNode.version, classNode.access, classNode.name, classNode.signature, "org/spongebot/bot/canvas/BotCanvas", new String[]{});
                        CanvasPatcher.patchConstructor(classNode);
                    }

                    ListIterator<MethodNode> mnli = classNode.methods.listIterator();
                    while (mnli.hasNext()) {

                        MethodNode mn = mnli.next();

                        ListIterator<AbstractInsnNode> ainli = mn.instructions.iterator();
                        while (ainli.hasNext()) {
                            AbstractInsnNode ain = ainli.next();
                            if (ain.getOpcode() == ALOAD) {
                                try {
                                    if (ain.getNext().getNext().getOpcode() == AALOAD)
                                        continue;
                                    if (ain.getNext().getNext().getNext().getOpcode() == AASTORE)
                                        continue;
                                } catch (Exception e) {

                                }
                                AbstractInsnNode next = ain.getNext();
                                LabelNode ln1 = new LabelNode(new Label());
                                mn.instructions.insertBefore(next, new InsnNode(DUP));
                                mn.instructions.insertBefore(next, new TypeInsnNode(INSTANCEOF, "java/lang/String"));
                                mn.instructions.insertBefore(next, new JumpInsnNode(IFEQ, ln1));
                                mn.instructions.insertBefore(next, new LdcInsnNode("DUP"));
                                mn.instructions.insertBefore(next, new LdcInsnNode(classNode.name));
                                mn.instructions.insertBefore(next, new LdcInsnNode(mn.name));
                                mn.instructions.insertBefore(next, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/callbacks/Misc", "check", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));
                                mn.instructions.insertBefore(next, ln1);
                            }
                        }

                        hideAddress(mn);

                        AbstractInsnNode[] arrP = new AbstractInsnNode[]{
                                new FieldInsnNode(GETFIELD, Loader.botToClient("Widget"), null, "[Ljava/lang/String;"),
                                new VarInsnNode(ILOAD, Util.FLAG_IGNORE),
                                new VarInsnNode(ALOAD, Util.FLAG_IGNORE),
                                new InsnNode(AASTORE)
                        };

                        if ((arrP = Util.findPattern(mn, arrP)) != null) {
                            AbstractInsnNode ain = arrP[0];
                            FieldInsnNode fin = (FieldInsnNode) arrP[0];
                            VarInsnNode newV = (VarInsnNode) arrP[2];
                            mn.instructions.insertBefore(ain, new InsnNode(DUP));
                            mn.instructions.insertBefore(ain, new VarInsnNode(ALOAD, newV.var));
                            mn.instructions.insertBefore(ain, new LdcInsnNode(classNode.name));
                            mn.instructions.insertBefore(ain, new LdcInsnNode(mn.name));
                            mn.instructions.insertBefore(ain, new LdcInsnNode(fin.name));
                            mn.instructions.insertBefore(ain, new MethodInsnNode(INVOKESTATIC, "org/spongebot/bot/callbacks/SetWidgetText", "check", "(Lorg/spongebot/bot/accessors/IWidget;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));

                        }


                        if (PacketHook.isMenuProcessor(mn)) {
                            System.out.println("Menu action processor: " + classNode.name + "." + mn.name);
                            for (int i = 0; i < 5; i++) {
                                Storage.objectPackets[i] = objPacketHook.getPackets(mn, objIndices[i], new FieldInsnNode(0, baseX[0], baseX[1], null), new FieldInsnNode(0, baseY[0], baseY[1], null), 0);
                                Storage.npcPackets[i] = npcPacketHook.getPackets(mn, npcIndices[i], null, null, 1);
                                //  Storage.interfacePackets[i] = interfacePacketHook.getPackets(mn, intIndices[i], null, null, 2);
//                                Storage.itemPackets[i] = itemPacketHook.getPackets(mn, itemIndices[i], null, null, 3);
                            }
                        }

                    }

                    //ObjectSpawnInjector.run(classNode);
                    //  InterfaceSpawnInjector.run(classNode);
                    //  InventorySpawnInjector.run(classNode);
                    //   GroundItemSpawnInjector.run(classNode);
                    //   NPCSpawnInjector.run(classNode);

//                    addSkillCallback(classNode, client.getFields().get("getExperiences").getName(), "updateExperience");
//                    addSkillCallback(classNode, client.getFields().get("getCurrentLevels").getName(), "updateCurrentLevel");
//                    addSkillCallback(classNode, client.getFields().get("getMaxLevels").getName(), "updateMaxLevel");

                    //  AnimableModelTransform.run(classNode);
                    //   ModelTransform.run(classNode);
                    ///   RasterizerTransform.run(classNode);
                    //   RenderableTransform.run(classNode);
                    //   MenuProcessorTransform.run(classNode);
                    //   InterfacePacketHook.run(classNode);
                    //   Finder.createGetUID(classNode, uidCandidates);
                    //   Censor.add(classNode);
                    Audio.disable(classNode);

                    // ag.v


                    if (classNode.name.equals("client")) {
                        addClientInterface(classNode);
                    }

                    for (RSClass rsClass : rsClasses) {
                        rsClass.inject(classNode);
                    }

                    for (Map.Entry<String, MethodNode> e : Storage.methods.entrySet()) {
                        if (classNode.name.equals(e.getKey().split("\\.")[0])) {
                            System.out.println("Adding method " + e.getKey().split("\\.")[1] + " to " + e.getKey().split("\\.")[0] + " " + e.getValue().desc);
                            classNode.methods.add(e.getValue());
                        }
                    }

                    new StreamTransform(classNode).run();
                    new PacketStreamTransform(classNode).run();

                    addOpcodePrinter(classNode);
                    classes.put(classNode.name, classNode);
                }
            }
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addClientInterface(ClassNode classNode) {

        List<String> requiredFields = new ArrayList();
        requiredFields.add("getRegion");
        requiredFields.add("getBaseX");
        requiredFields.add("getBaseY");
        requiredFields.add("getLocalPlayer");
        requiredFields.add("getWidgets");
        requiredFields.add("getLoginState");
        requiredFields.add("getLoginMessage");
        requiredFields.add("getLoadingMessage");
        requiredFields.add("getExperiences");
        requiredFields.add("getCurrentLevels");
        requiredFields.add("getMaxLevels");
        requiredFields.add("getLocalNpcs");
        requiredFields.add("getPacketStream");
        requiredFields.add("getNPCIndices");
        requiredFields.add("getKeyboard");

        classNode.interfaces.add("org/spongebot/bot/accessors/IClient");

        for (String fieldName : requiredFields) {
            CompactField cf = client.getFields().get(fieldName);
            if (cf == null) {
                System.out.println("WARNING: Missing required client field " + fieldName);
                continue;
            }
            String name = cf.getName();
            if (!name.contains("."))
                name = "client." + name;
            if (fieldName.equals("getKeyboard")) {
                ScreenMover.currentKeyboard = name;
                continue;
            }
            String[] parts = name.split("\\.");
            if (cf.getDescriptor().equals("I")) {
                addGenericGetter(classNode, fieldName, parts[0], parts[1], cf.getMultiplier());
            } else {
                if (!cf.getDescriptor().contains("L"))
                    addGenericGetter(classNode, fieldName, parts[0], parts[1], cf.getDescriptor(), cf.getDescriptor());
                else {
                    String prefix = cf.getDescriptor().substring(0, cf.getDescriptor().indexOf("L") + 1);
                    String clientName = cf.getDescriptor().replace(prefix, "").replace(";", "");
                    String botName;
                    if (fieldName.equals("getPacketStream"))
                        botName = "org/spongebot/bot/accessors/IPacketStream";
                    else
                        botName = "org/spongebot/bot/accessors/I" + Loader.clientToBot(clientName);
                    addGenericGetter(classNode, fieldName, parts[0], parts[1], cf.getDescriptor(), prefix + botName + ";");
                }
            }
        }
    }

    private void addGenericGetter(ClassNode classNode, String methodName, String owner, String name, int multiplier) {
        MethodNode mn = new MethodNode(ACC_PUBLIC, methodName, "()I", null, null);
        mn.visitCode();
        mn.visitFieldInsn(GETSTATIC, owner, name, "I");
        mn.visitLdcInsn(multiplier);
        mn.visitInsn(IMUL);
        mn.visitInsn(IRETURN);
        mn.visitEnd();
        classNode.methods.add(mn);
    }

    private void addGenericGetter(ClassNode classNode, String methodName, String owner, String name, String clientDesc, String botDesc) {
        MethodNode mn = new MethodNode(ACC_PUBLIC, methodName, "()" + botDesc, null, null);
        mn.visitCode();
        mn.visitFieldInsn(GETSTATIC, owner, name, clientDesc);
        mn.visitInsn(ARETURN);
        mn.visitEnd();
        classNode.methods.add(mn);
    }

    private byte[] getBytes(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        String clsName = name.replaceAll("\\.", "/");
        ClassNode node = null;

        if (classes.containsKey(clsName))
            node = classes.get(clsName);

        if (node != null) {
            for (MethodNode mn : node.methods) {
                for (MethodInsnNode min : disarmTargets) {
                    if (mn.name.equals(min.name) && mn.desc.equals(min.desc) && node.name.equals(min.owner))
                        Util.removeTrapCode(mn); // was disarm
                }
            }
            byte[] clsData = getBytes(node);
            if (clsData != null) {
                Class<?> cls = defineClass(name, clsData, 0, clsData.length,
                        null);
                defined.put(clsName, cls);
                if (resolve)
                    resolveClass(cls);
                return cls;
            }
        }
        return super.findSystemClass(name);
    }
}
