package me.zoemartin.bot.modules.baseCommands;

import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.LoadModule;

@LoadModule
public class BaseCommands implements Module {
    @Override
    public void init() {
        CommandManager.register(new Ping());
        CommandManager.register(new Help());
        CommandManager.register(new Usage());
    }
}
