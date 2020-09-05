package me.zoemartin.bot.modules.logger;

import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;

@LoadModule
public class Logs implements Module {
    @Override
    public void init() {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
    }
}
