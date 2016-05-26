package org.spongebot.loader;

import org.spongebot.bot.accessors.IClient;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.Client;
import org.spongebot.bot.struct.RSClass;
import org.spongebot.bot.struct.RSField;
import org.spongebot.loader.updater.imported.Reader;
import org.znu.core.structures.export.CompactClass;
import org.znu.core.structures.export.CompactField;

import java.applet.Applet;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;

public class Loader {

    public static RSClassLoader rsClassLoader = null;

    private static Map<String, String> descMap = new HashMap();
    private static Map<String, String> nameMap = new HashMap();

    private static Map<String, String[]> requiredClasses = new LinkedHashMap();
    public static Map<String, Map<String, String>> fields = new HashMap();

    static {
        requiredClasses.put("GameObject", new String[]{"getHash", "getLocalX", "getLocalY"});
        requiredClasses.put("ObjectDefinition", new String[]{"getName", "getActions"});
        requiredClasses.put("Item", new String[]{"getID", "getStackSize"});
        requiredClasses.put("ItemDefinition", new String[]{"getName", "getInventoryActions", "getGroundActions"});
        requiredClasses.put("NPCDefinition", new String[]{"getID", "getLevel", "getName", "getActions"});
        requiredClasses.put("NPC", new String[]{"getDefinition"});
        requiredClasses.put("Player", new String[]{"getName"});
        requiredClasses.put("Character", new String[]{"getText", "getPathX", "getPathY", "getLocalX", "getLocalY", "getAnimation"});
        requiredClasses.put("Widget", new String[]{"getID", "getItems", "getStackSizes", "getActions", "getText"});
        requiredClasses.put("Node", new String[]{});
        requiredClasses.put("NodeQueue", new String[]{});
        requiredClasses.put("Renderable", new String[]{});
        requiredClasses.put("Model", new String[]{});
        requiredClasses.put("Region", new String[]{});
        requiredClasses.put("SceneTile", new String[]{"getSceneObjects"});

    }

    public static String clientToBot(String name) {
        return descMap.get(name);
    }

    public static String botToClient(String name) {
        if (nameMap.get(name) == null) System.out.println("lookup " + name);
        return nameMap.get(name);
    }

    public static Applet load() {
        try {
            Stub stub = new Stub();
            URL url = new URL(stub.getCodeBase() + "/" + stub.getParameter("_archive"));
            System.out.println("jar:" + url.toString() + "!/");
            // JarURLConnection conn = (JarURLConnection) new URL("jar:" + url.toString() + ".jar!/").openConnection();
            //  JarFile jarFile = conn.getJarFile();
            JarFile jarFile = new JarFile("C:\\Users\\Owner\\Desktop\\RS 07 Work\\30\\30.jar");

            ArrayList<RSClass> injClasses = new ArrayList();

            Reader reader = new Reader(new File("C:\\Users\\Owner\\Desktop\\ZNU\\Fields"));

            Map<String, CompactClass> importedClasses = reader.readDir();

            for (String key : requiredClasses.keySet()) {
                CompactClass cc = importedClasses.get(key);
                if (cc != null) {
                    descMap.put(cc.getRsClass(), cc.getBotClass());
                    nameMap.put(cc.getBotClass(), cc.getRsClass());
                    RSClass rsClass = new RSClass("org/spongebot/bot/accessors/I" + key, cc.getRsClass());
                    String[] requiredFields = requiredClasses.get(key);
                    HashMap<String, String> fieldMap = new HashMap();
                    for (String fieldRequirement : requiredFields) {
                        if (!cc.getFields().containsKey(fieldRequirement)) {
                            System.out.println("WARNING: Required class " + key + " does not contain required field " + fieldRequirement);
                            continue;
                        }
                        CompactField cf = cc.getFields().get(fieldRequirement);
                        fieldMap.put(fieldRequirement, cf.getName());
                        if (Math.abs(cf.getMultiplier()) > 1)
                            rsClass.putField(new RSField(fieldRequirement, cf.getDescriptor(), cf.getDescriptor(), cf.getName(), cf.getMultiplier()));
                        else {
                            if (cf.getDescriptor().contains("L") && !cf.getDescriptor().contains("java")) {
                                String prefix = cf.getDescriptor().substring(0, cf.getDescriptor().indexOf("L") + 1);
                                String clientName = cf.getDescriptor().replace(prefix, "").replace(";", "");
                                String botName = "org/spongebot/bot/accessors/I" + Loader.clientToBot(clientName);
                                rsClass.putField(new RSField(fieldRequirement, prefix + botName + ";", cf.getDescriptor(), cf.getName()));
                            } else
                                rsClass.putField(new RSField(fieldRequirement, cf.getDescriptor(), cf.getDescriptor(), cf.getName()));
                        }
                    }
                    fields.put(key, fieldMap);
                    injClasses.add(rsClass);
                } else {
                    System.out.println("Missing required class " + key);
                }
            }

            rsClassLoader = new RSClassLoader(jarFile, injClasses, importedClasses.get("Client"));
            Class<?> client = rsClassLoader.loadClass("client");
            Object inst = client.newInstance();
            Context.client = new Client((IClient) inst);

            Applet applet = (Applet) inst;
            applet.setStub(stub);
            applet.init();
            applet.start();

            return applet;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
