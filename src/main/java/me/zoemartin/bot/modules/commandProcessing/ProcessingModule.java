package me.zoemartin.bot.modules.commandProcessing;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.util.DatabaseUtil;

@LoadModule
public class ProcessingModule implements Module {
    @Override
    public void init() {
        Bot.addListener(new CommandListener());
        CommandManager.setCommandProcessor(new CommandHandler());
        DatabaseUtil.setMapped(MemberPermission.class);
        DatabaseUtil.setMapped(RolePermission.class);
        DatabaseUtil.setMapped(Prefixes.class);
    }

    @Override
    public void initLate() {
        PermissionHandler.initPerms();
        Prefixes.init();
    }
}
