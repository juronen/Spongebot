package org.spongebot.bot.script;

import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.Client;
import org.spongebot.bot.rs.api.GameObject;
import org.spongebot.bot.script.interfaces.ObjectFilter;

import java.util.Random;

public abstract class Script {

    protected Client client;
    protected Random random;

    public Script() {
        client = Context.client;
        random = new Random();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public GameObject getNearest(ObjectFilter filter) {
        return ScriptObjectCache.findNearest(filter);
    }

    public GameObject getNearest() {
        return ScriptObjectCache.findNearest();
    }

    public GameObject[] getSorted() {
        return ScriptObjectCache.getSorted();
    }

    public GameObject getAt(int x, int y) {
        return ScriptObjectCache.getObjectAt(x, y);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void sleep(int a, int b) {
        try {
            Thread.sleep(random.nextInt(b - a) + a);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void println(Object o) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + o);
    }

    public String pad(String left, String middle, String right, int length) {
        length -= left.length();
        length -= right.length();
        while (middle.length() < length)
            middle += " ";
        return left + middle + right;
    }


    public abstract String[] getStatus();

    public abstract boolean onStart();

    public abstract void onStop();

    public abstract void run();

}
