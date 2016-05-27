package org.spongebot.bot.script;

import org.spongebot.bot.rs.MouseTimer;
import org.spongebot.bot.rs.spawncontrol.ObjectSpawn;
import org.spongebot.bot.script.bundled.FlaxPicker;
import org.spongebot.bot.script.bundled.TerribleMiner;

import java.util.ArrayList;
import java.util.List;

public class ScriptController {

    private static List<Script> scripts = new ArrayList();

    private static Thread scriptThread = null;
    private static Script currentScript = null;

    public static void load() {

        // Bundled scripts
        scripts.add(new FlaxPicker());
        scripts.add(new TerribleMiner());

        // Load from disk...
    }

    public static void list() {
        for (Script script : scripts) {
            System.out.println("[ScriptController] List: " + script.getClass().getSimpleName());
        }
    }

    public static void start(String name) {
        for (Script s : scripts) {
            if (s.getClass().getSimpleName().equals(name)) {
                final Script script = s;
                System.out.println("[ScriptController] Starting script " + script.getClass().getSimpleName());
                scriptThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (script.onStart()) {
                            script.run();
                            script.onStop();
                            ScriptController.stop();
                        }
                    }
                });
                MouseTimer.time = System.currentTimeMillis();
                currentScript = script;
                scriptThread.start();
                ObjectSpawn.initScriptCache();
                new Thread(new ScreenMover()).start();
                return;
            }
        }
    }

    public static void stop() {
        if (scriptThread != null) {
            try {
                ScreenMover.stop();
                scriptThread.stop();
                scriptThread = null;
                currentScript.onStop();
                currentScript = null;
            } catch (/*NuclearHolocaust*/Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Script getCurrentScript() {
        return currentScript;
    }
}
