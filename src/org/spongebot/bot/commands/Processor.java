package org.spongebot.bot.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Processor extends Thread {

    private BufferedReader input;

    private List<ProcessingTask> pts;

    public Processor() {
        input = new BufferedReader(new InputStreamReader(System.in));
        pts = new ArrayList();
    }

    public void run() {
        String line;
        try {
            while ((line = input.readLine()) != null) {
                if (line.equals(""))
                    continue;
                System.out.println("Processing command " + line);
                for (ProcessingTask pt : pts) {
                    pt.process(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTask(ProcessingTask p) {
        pts.add(p);
    }
}
