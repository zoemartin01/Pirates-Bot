package me.zoemartin.piratesBot.modules.commandProcessing;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;

@LoadModule
public class ProcessingModule implements Module {
    @Override
    public void init() {
        Bot.addListener(new CommandListener());
        CommandManager.setCommandProcessor(new CommandHandler());
        DatabaseUtil.setMapped(MemberPermission.class);
        DatabaseUtil.setMapped(RolePermission.class);
        DatabaseUtil.setMapped(Prefixes.class);
        DatabaseUtil.setMapped(LoggedError.class);
    }

    @Override
    public void initLate() {
        PermissionHandler.initPerms();
        Prefixes.init();
    }
}
