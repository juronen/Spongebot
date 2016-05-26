package org.spongebot.bot.rs.randoms;

import org.spongebot.bot.rs.randoms.events.BeeKeeper;
import org.spongebot.bot.rs.randoms.events.StrangeBox;
import org.spongebot.bot.script.ScriptController;

import java.util.HashMap;
import java.util.Map.Entry;

public class RandomsController {

    private static HashMap<String, RandomEvent> events = new HashMap();

    private static boolean active = false;

    private static long delay = 2000L;

    static {
        events.put("BeeKeeper", new BeeKeeper());
        events.put("StrangeBox", new StrangeBox());
    }

    // So, in the hypothetical scenario that this will be a finished bot with a community and shit,
    // I figured it'd be useful for scripters / users to be able to override the bot's random event handlers
    // in case I take a month or five off drinking margaritas at the beach. The way this would work is
    // someone posts a patch on the forums, then a scripter compiles their script along with the random patch
    // and calls RandomsController.patch("kanker", MyPatchedEvent); in the beginning of their script.

    public static void patch(String name, RandomEvent e) {
        events.put(name, e);
    }

    public static void start() {
        active = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (active) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (Entry<String, RandomEvent> e : events.entrySet()) {
                        RandomEvent solver = e.getValue();
                        if (solver.activated()) {
                            System.out.println("Random event " + e.getKey() + " activated!");
                            if (solver.solve()) {
                                System.out.println("Random event " + e.getKey() + " has been solved!");
                            } else {
                                System.out.println("Failed solving random event " + e.getKey());
                                ScriptController.stop();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public static void stop() {
        active = false;
    }

    public static void setDelay(long l) {
        delay = l;
    }


}
