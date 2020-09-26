package me.zoemartin.piratesBot.modules.slf4jlogs;

import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.interfaces.Module;

@LoadModule
public class Logs implements Module {
    @Override
    public void init() {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("hibernate.types.print.banner", "false");
    }
}
