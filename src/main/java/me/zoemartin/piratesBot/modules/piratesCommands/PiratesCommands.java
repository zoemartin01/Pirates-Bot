package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.rubie.Bot;
import me.zoemartin.rubie.core.annotations.LoadModule;
import me.zoemartin.rubie.core.interfaces.Module;

@LoadModule
public class PiratesCommands implements Module {
    @Override
    public void init() {
        Bot.addListener(new VoiceRole.Listener());
    }

    @Override
    public void initLate() {
        VoiceRole.initConfigs();
    }
}
