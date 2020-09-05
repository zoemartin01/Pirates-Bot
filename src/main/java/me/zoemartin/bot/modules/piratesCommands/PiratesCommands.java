package me.zoemartin.bot.modules.piratesCommands;

import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.LoadModule;

@LoadModule
public class PiratesCommands implements Module {
    @Override
    public void init() {
        CommandManager.register(new Assemble());
        CommandManager.register(new Scatter());
    }
}
