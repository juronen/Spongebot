package org.spongebot.bot;

import java.util.HashMap;

public class Configuration {

    public static String censor = "";

    private static HashMap<String, Boolean> renderingMethods = new HashMap();

    static {
        renderingMethods.put("renderAtPointR", true);
        renderingMethods.put("renderAtPointM", true);
        renderingMethods.put("lighting", true);
        renderingMethods.put("menuProcessor", true);
    }

    public static boolean isDrawing(String name) {
        return renderingMethods.get(name);
    }

    public static void toggle(String name, boolean state) {
        renderingMethods.put(name, state);
    }

    public static void toggle(boolean state) {
        for (String name : renderingMethods.keySet())
            renderingMethods.put(name, state);
    }

    public static void listOptions() {
        for (String s : renderingMethods.keySet()) {
            System.out.println(">> " + s);
        }
    }

}
