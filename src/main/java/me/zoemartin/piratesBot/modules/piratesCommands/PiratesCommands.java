package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.LoadModule;

@LoadModule
public class PiratesCommands implements Module {
    @Override
    public void init() {
        CommandManager.register(new Assemble());
        CommandManager.register(new Scatter());
        CommandManager.register(new RandomGroupsCommand());
    }
}
