package org.spongebot.bot.rs.api.tools;

import org.spongebot.bot.rs.Constants;
import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.Client;

public class Walking {

    public static int destX = 0;
    public static int destY = 0;

    public static void walkTo(int worldX, int worldY) {
        Client client = Context.client;
        boolean finished = false;
        while (!finished) {
            finished = walk(worldX, worldY);
            long time = System.currentTimeMillis();
            while (System.currentTimeMillis() - time < 50000) {
                //int px = client.getLocalPlayer().getX();
                //int py = client.getLocalPlayer().getY();
                Misc.sleep(100, 200);
                if (client.getLocalPlayer().distance(destX - client.getBaseX(), destY - client.getBaseY()) < 4) {
                    //if (px == client.getLocalPlayer().getX() && py == client.getLocalPlayer().getY()) {
                    break;
                }
            }
        }
    }

    // Walks to a tile. If the target tile is outside the current 104 * 104 region,
    // it will walk to the tile closest to the target tile in the current region.
    // Returns true if the target tile was in the current region.
    public static boolean walk(int worldX, int worldY) {

        Client client = Context.client;

        int baseX = client.getBaseX();
        int baseY = client.getBaseY();

        int targetLocalX = worldX - baseX;
        int targetLocalY = worldY - baseY;

        System.out.println("[Walking] Target locals: " + targetLocalX + " " + targetLocalY);

        if (targetLocalX < 0 || targetLocalX > 103 || targetLocalY < 0 || targetLocalY > 103) {

            System.out.println("[Walking] Relocating target...");
            int playerLocalX = client.getLocalPlayer().getLocalX();
            int playerLocalY = client.getLocalPlayer().getLocalY();

            try {
                double slope = ((double) (targetLocalY - playerLocalY)) / ((double) (targetLocalX - playerLocalX));
                System.out.println("[Walking] Slope: " + slope);
                double newTLX = playerLocalX;
                double newTLY = playerLocalY;
                // I'll be safe with the borders, not a 100% sure on when it starts to load a new region as you walk
                while (newTLX > 2 && newTLX < 101 && newTLY > 2 && newTLY < 101) {
                    if (targetLocalX < playerLocalX) {
                        newTLX -= 1.0D;
                        newTLY -= slope;
                    } else {
                        newTLX += 1.0D;
                        newTLY += slope;
                    }
                }
                System.out.println("[Walking] Relocated walking target to " + (baseX + (int) newTLX) + " " + (baseY + (int) newTLY));
                client.walkTo(baseX + (int) newTLX, baseY + (int) newTLY, true, 0, 0, Constants.WALKING_WALK);
            } catch (ArithmeticException e) {
                // Division by zero / we are on the same X
                if (targetLocalY > playerLocalY) // North
                    client.walkTo(worldX, baseY + 102, true, 0, 0, Constants.WALKING_WALK);
                else // South
                    client.walkTo(worldX, baseY + 2, true, 0, 0, Constants.WALKING_WALK);
            }
            return false;
        } else {
            client.walkTo(worldX, worldY, true, 0, 0, Constants.WALKING_WALK);
            return true;
        }
    }
}
