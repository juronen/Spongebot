package org.spongebot.bot.callbacks;

public class Misc {

    // Random shit I test
    public static void check(String s, String cl, String mn) {
        int[] i = null;
        if (s instanceof String) {
            if (((String) s).contains("SPECIAL ATTACK")) {
                System.out.println("Found in " + cl + " " + mn);
                s.getClass();
            }
        }
    }
}
