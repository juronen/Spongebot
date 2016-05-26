package org.spongebot.bot.canvas;

import java.awt.*;
import java.awt.image.BufferedImage;

// This is really useless since debug drawing is a bit counterproductive towards the idea of being lightweight
public class BotCanvas extends java.awt.Canvas {

    private BufferedImage image;
    private Graphics graphics;

    public BotCanvas() {
        super();
        //  image = new BufferedImage(765, 503, BufferedImage.TRANSLUCENT);
    }

    @Override
    public Graphics getGraphics() {
        return super.getGraphics();
    }
}