package org.spongebot.bot.script;

import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.tools.Misc;
import org.spongebot.bot.rs.reflection.Reflection;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

// So, after a while I couldn't be bothered to really find out where the timed logout is done. Dis happend.
public class ScreenMover implements Runnable {

    private static boolean active = false;
    public static String currentKeyboard = "";

    @Override
    public void run() {
        active = true;
        while (active) {
            Misc.sleep(5000, 30000);
            int code = new Random().nextBoolean() ? KeyEvent.VK_RIGHT : KeyEvent.VK_LEFT;
            String[] path = currentKeyboard.split("\\.");
            Object keyboard = Reflection.getStaticField(path[0], path[1]);
            KeyListener keyListener = (KeyListener) keyboard;
            keyListener.keyPressed(new KeyEvent(Context.applet, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, code));
            Misc.sleep(200, 800);
            keyListener.keyReleased(new KeyEvent(Context.applet, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, code));
        }
    }

    public static void stop() {
        active = false;
    }
}
