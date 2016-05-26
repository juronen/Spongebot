package org.spongebot.loader.updater;

import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Storage {

    public static HashMap<String, MethodNode> methods = new HashMap();

    public static Map<String, Object>[] objectPackets = new LinkedHashMap[5];
    public static Map<String, Object>[] npcPackets = new LinkedHashMap[5];
    //public static Map<String, Object>[] interfacePackets = new LinkedHashMap[5];
    public static Map<String, Object>[] itemPackets = new LinkedHashMap[5];

    public static String[][] interfacePackets = new String[10][2];
    public static int[] interfaceOpcodes = new int[10];
    // Changed with the September bank update. For interfaces (and bank item actions), the client sends the
    // interface id, and then an item index, or -1 if it's not an item.

}
