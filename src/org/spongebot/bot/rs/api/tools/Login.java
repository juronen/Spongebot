package org.spongebot.bot.rs.api.tools;

import org.spongebot.bot.rs.Context;
import org.spongebot.bot.rs.reflection.Reflection;

public class Login {

    public static String loginUser;
    public static String loginPass;

    public static boolean login() {
        //Reflection.setStaticField("client", "bw", 0);
        while (true) {
            if (Context.client.getLoginState() == 10) {
                System.out.println("Successfully logged in!");
                Misc.sleep(2000); // Making sure the welcome screen is up
                //Reflection.invokeStatic("client", "mo"); // Ladies and gentlemen, "closeAllInterfaces();"
                break;
            }
            String status = Context.client.getLoginMessage();
            if (!(status.equals("") || status.equals("Enter your username/email & password."))) {
                System.err.println("LOGIN ERROR: " + status);
                return false;
            }
            System.out.println("Status: " + status);
            Context.client.login();
            Misc.sleep(2000);
            // You need to let dem packets to go through after each call, otherwise you get
            // your ass ran over by a NullPointerException
        }
        return true;
    }

    public static boolean login(String username, String password) {
        String[] user = loginUser.split("\\.");
        String[] pass = loginPass.split("\\.");
        Reflection.setStaticField(user[0], user[1], username);
        Reflection.setStaticField(pass[0], pass[1], password);
        return login();
    }
}
