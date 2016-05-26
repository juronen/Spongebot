package org.spongebot.bot.callbacks;

import org.spongebot.bot.Configuration;
import org.spongebot.bot.rs.Context;

public class UserCensor {

    public static String apply(String o) {
        if (Configuration.censor.equals("") || o == null)
            return o;
        try {
            if (o.equals(Context.client.getLocalPlayer().getName()))
                return Configuration.censor;
        } catch (Exception e) {
            e.printStackTrace();
            // This should never get thrown as the local player will only be null if you're logged out,
            // and if you're logged out then player names won't be used anywhere. Some shit might go down because of
            // lagg though, and I'd rather not have it explode because of that.
        }
        return o;
    }
}
