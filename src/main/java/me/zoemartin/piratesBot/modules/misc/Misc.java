package me.zoemartin.piratesBot.modules.misc;

import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;

@LoadModule
public class Misc implements Module {
    @Override
    public void init() {
        CommandManager.register(new Enlarge());
        CommandManager.register(new Avatar());
        CommandManager.register(new Nick());
    }
}
