package org.spongebot.bot.callbacks;

import org.spongebot.bot.accessors.IWidget;
import org.spongebot.bot.rs.api.tools.Bank;

public class SetWidgetText {

    // Convenient :)
    public static void check(final IWidget widget, final String a, final String className, final String mName, final String fName) {
        if (a.equals("Deposit-All")) {
            Bank.bankWidget = widget;
        }
    }
}
