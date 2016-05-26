package org.spongebot.bot.rs.api.tools;

import org.spongebot.bot.rs.reflection.Reflection;

import java.util.Random;

public class Misc {

    private static Random random = new Random();

    public static void sleep(int a, int b) {
        sleep(random.nextInt(b - a) + a);
    }

    public static void sleep(int a) {
        try {
            Thread.sleep(a);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int getLoginState() {
        return (int) Reflection.getStaticField("client", "bv") * -302349341;
    }

    public static String getLoginMessage() {
        return (String) Reflection.getStaticField("h", "ah");
    }

    public static String getLoadingMessage() {
        return (String) Reflection.getStaticField("h", "ao");
    }
}
