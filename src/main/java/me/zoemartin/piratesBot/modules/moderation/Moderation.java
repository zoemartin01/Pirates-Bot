package me.zoemartin.piratesBot.modules.moderation;

import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;

@LoadModule
public class Moderation implements Module {
    @Override
    public void init() {
        DatabaseUtil.setMapped(WarnEntity.class);
        CommandManager.register(new Warn());
        CommandManager.register(new RoleManagement());
    }
}
