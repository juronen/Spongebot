package org.spongebot.bot.callbacks;

public class Printer {

    // This is for arbitrary printing methods that I write in the heat of the moment to debug some things in the client

    public static boolean ON = false;

    public static String putOpcode = "";

    public static void printOpcode(int i) {
        if (ON) {
            // Added the dashes to make it easier to distinguish between sets of data
            System.out.println("[Stream] >> Opcode " + i + " --------------------------------------------------------");
        }
    }

    public static void print(String a, int i) {
        if (ON) {
            System.out.println("[Stream] >> " + a + ": " + i);
        }
    }

    public static void print(String a, long i) {
        if (ON) {
            System.out.println("[Stream] >> " + a + ": " + i);
        }
    }

    public static void print(String a, String i) {
        if (ON) {
            System.out.println("[Stream] >> " + a + ": " + i);
        }
    }
}
