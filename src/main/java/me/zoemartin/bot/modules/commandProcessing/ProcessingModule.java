package me.zoemartin.bot.modules.commandProcessing;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;

@LoadModule
public class ProcessingModule implements Module {
    @Override
    public void init() {
        Bot.addListener(new CommandListener());
        CommandManager.setCommandProcessor(new CommandHandler());
        PermissionHandler.initPerms();
    }
}
