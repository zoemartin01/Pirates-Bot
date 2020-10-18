package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;

@LoadModule
public class PiratesCommands implements Module {
    @Override
    public void init() {
        DatabaseUtil.setMapped(VoiceRoleConfig.class);
        CommandManager.register(new Assemble());
        CommandManager.register(new Scatter());
        CommandManager.register(new RandomGroupsCommand());
        CommandManager.register(new VoiceRole());
        CommandManager.register(new SpeedDates());
        CommandManager.register(new MoveAll());
        Bot.addListener(new VoiceRole());
    }

    @Override
    public void initLate() {
        VoiceRole.initConfigs();
    }
}
