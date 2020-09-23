package me.zoemartin.bot.modules.misc;

import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;

@LoadModule
public class Misc implements Module {
    @Override
    public void init() {
        CommandManager.register(new Enlarge());
        CommandManager.register(new Avatar());
    }
}
