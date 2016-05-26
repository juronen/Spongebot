package org.spongebot;

import org.spongebot.bot.rs.Context;
import org.spongebot.loader.Loader;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class BotWindow extends JFrame {

    public BotWindow() {
        setAlwaysOnTop(true);
        setTitle("Spongebot"); // Had trouble making up my mind here
        setLayout(new BorderLayout(0, 0));

        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Applet applet = Loader.load();
        Context.applet = applet;
        applet.setPreferredSize(new Dimension(765, 503));
        applet.setVisible(true);

        getContentPane().add(applet);

        getContentPane().setPreferredSize(new Dimension(765, 503));
        pack();

        revalidate();
        repaint();

    }
}
