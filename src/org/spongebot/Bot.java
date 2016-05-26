package org.spongebot;

import org.spongebot.bot.Configuration;
import org.spongebot.bot.accessors.IItemDefinition;
import org.spongebot.bot.accessors.IObjectDefinition;
import org.spongebot.bot.callbacks.Printer;
import org.spongebot.bot.commands.ProcessingTask;
import org.spongebot.bot.commands.Processor;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.Client;
import org.spongebot.bot.rs.api.GameObject;
import org.spongebot.bot.rs.api.tools.Inventory;
import org.spongebot.bot.rs.api.tools.Login;
import org.spongebot.bot.rs.api.tools.Objects;
import org.spongebot.bot.rs.reflection.Reflection;
import org.spongebot.bot.script.Script;
import org.spongebot.bot.script.ScriptController;


public class Bot {

    public static void main(String[] args) {

        new BotWindow().setVisible(true);

        ScriptController.load();

        Processor processor = new Processor();

        // The default commands usable from the command line, a script will be able to add to these

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-dumpobj")) {
                    GameObject[] objs = Objects.getSorted();
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        try {
                            int n = Math.min(Integer.parseInt(parts[1]), objs.length);
                            System.out.println("Dumping the first " + n + " objects...");
                            for (int i = 0; i < n; i++) {
                                GameObject obj = objs[i];
                                System.out.println("[Object Dump] " + obj.getHash() + " " + (obj.getHash() >> 29 & 3) + " " + (obj.getID()) + " " + obj.getName() + " " + obj.getLocalX() + " " + obj.getLocalY());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Syntax: -dumpobj <NUM>");
                        }
                    } else {
                        for (GameObject obj : objs) {
                            System.out.println("[Object Dump] " + obj.getHash() + " " + (obj.getID()) + " " + obj.getName() + " " + obj.getLocalX() + " " + obj.getLocalY());
                        }
                    }
                }
            }
        });


        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-terminate"))
                    System.exit(0);
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-listscripts"))
                    ScriptController.list();
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-loadscripts"))
                    ScriptController.load();
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-stop"))
                    ScriptController.stop();
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-toggleprinter"))
                    Printer.ON = !Printer.ON;
            }
        });


        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-start")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        ScriptController.start(parts[1]);
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-status")) {
                    Script script = ScriptController.getCurrentScript();
                    if (script != null) {
                        System.out.println("Printing status for script: " + script.getClass().getSimpleName());
                        for (String s : script.getStatus())
                            System.out.println(">> " + s);
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-dropall")) {
                    Inventory.dropAll();
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-sound")) {
                    Reflection.setStaticField("client", "mc", false);
                }
            }
        });


        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-itemdef")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        try {
                            IItemDefinition def = Context.client.getItemDefinition(Integer.parseInt(parts[1]));
                            if (def == null)
                                System.out.println("Item definition null");
                            else {
                                System.out.println("Name: " + def.getName());
                                for (String s : def.getInventoryActions()) {
                                    System.out.println("Action: " + s);
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Syntax: -itemdef id");
                        }
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-objdef")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        try {
                            IObjectDefinition def = Context.client.getObjectDefinition(Integer.parseInt(parts[1]));
                            if (def == null)
                                System.out.println("Object definition null");
                            else {
                                if (def.getName() == null)
                                    def = def.doSomething();
                                System.out.println("Name: " + def.getName());
                                for (String s : def.getActions()) {
                                    System.out.println("Action: " + s);
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Syntax: -objdef id");
                        }
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-render")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        try {
                            boolean state = Boolean.parseBoolean(parts[1]);
                            Configuration.toggle(state);
                        } catch (NumberFormatException e) {
                            System.out.println("Syntax: -cpu <level>");
                        }
                    } else if (parts.length == 3) {
                        try {
                            boolean state = Boolean.parseBoolean(parts[2]);
                            Configuration.toggle(parts[1], state);
                        } catch (NumberFormatException e) {
                            System.out.println("Syntax: -cpu <level>");
                        }
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.equals("-dumplocal")) {
                    Client client = Context.client;
                    System.out.println("[Dumping local player] Local Coordinates: " + client.getLocalPlayer().getLocalX() +
                            " " + client.getLocalPlayer().getLocalY() +
                            " " + (client.getLocalPlayer().getLocalX() + client.getBaseX()) + " " + (client.getLocalPlayer().getLocalY() + client.getBaseY()));
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-closestobj")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        try {
                            int id = Integer.parseInt(parts[1]);
                            GameObject g = Objects.findClosest(id);
                            System.out.println("Closest object for id " + id + " is at " + g.getLocalX() + " " + g.getLocalY());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-getat")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 3) {
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            GameObject g = Objects.getAt(x, y);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-login")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 3) {
                        Login.login(parts[1], parts[2]);
                    } else
                        Login.login();
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-setcensor")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        Configuration.censor = parts[1];
                    } else
                        Configuration.censor = "";
                }
            }
        });


        // Instantaneously decanting the entire inventory, lolwat
        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-decant")) {
                    int idA = Integer.parseInt(command.split(" ")[1]);
                    int idB = Integer.parseInt(command.split(" ")[2]);
                    for (int i = 0; i < 27; i += 2) {
                        Inventory.use(i, idA, i + 1, idB);
                    }
                }
            }
        });

        processor.addTask(new ProcessingTask() {
            @Override
            public void process(String command) {
                if (command.startsWith("-decantX")) {
                    int idA = Integer.parseInt(command.split(" ")[1]);
                    int idB = Integer.parseInt(command.split(" ")[2]);
                    for (int i = 0; i < 26; i += 4) {
                        Inventory.use(i, idA, i + 2, idB);
                    }
                }
            }
        });

        processor.start();

        Context.consoleProcessor = processor;
    }
}
