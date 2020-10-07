package me.zoemartin.piratesBot.core.util;

import me.zoemartin.piratesBot.core.interfaces.Helper;

public class Help {
    private static Helper helper = null;

    public static void setHelper(Helper helper) {
        Help.helper = helper;
    }

    public static Helper getHelper() {
        return helper;
    }
}
