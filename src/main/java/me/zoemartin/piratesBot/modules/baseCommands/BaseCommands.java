package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.LoadModule;

@LoadModule
public class BaseCommands implements Module {
    @Override
    public void init() {
        CommandManager.register(new Ping());
        CommandManager.register(new Help());
        CommandManager.register(new Usage());
        CommandManager.register(new About());
        CommandManager.register(new UserInfo());
        CommandManager.register(new RoleInfo());
        CommandManager.register(new ServerInfo());
        CommandManager.register(new Permission());
        CommandManager.register(new Prefix());
    }
}
