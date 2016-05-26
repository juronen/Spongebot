package org.spongebot.bot.callbacks;

public class Logout {

    public static void logout() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        System.out.println("The client has logged you out!");
        for (StackTraceElement e : elements) {
            System.out.println("I--> " + e.getClassName() + "." + e.getMethodName() + " line " + e.getLineNumber());
        }
    }
}
