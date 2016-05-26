package org.spongebot.bot.rs.api.tools;

import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.api.Widget;
import org.spongebot.bot.rs.reflection.Reflection;

public class Widgets {

    public static void close() {
        Reflection.invokeStatic("client", "mo", new Object[]{}, new Class[]{});
    }

    public static Widget get(int a, int b) {
        try {
            return new Widget(Context.client.getWidgets()[a][b]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
