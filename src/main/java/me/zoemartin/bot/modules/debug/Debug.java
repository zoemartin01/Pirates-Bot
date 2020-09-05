package me.zoemartin.bot.modules.debug;

import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;

@LoadModule
public class Debug implements Module {
    @Override
    public void init() {
        CommandManager.register(new Sleep());
        CommandManager.register(new Shutdown());
    }
}
