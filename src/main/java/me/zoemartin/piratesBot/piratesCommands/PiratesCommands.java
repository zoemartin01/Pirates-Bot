package me.zoemartin.piratesBot.piratesCommands;

import me.zoemartin.rubie.Bot;
import me.zoemartin.rubie.core.annotations.Module;
import me.zoemartin.rubie.core.interfaces.ModuleInterface;

@Module
public class PiratesCommands implements ModuleInterface {
    @Override
    public void init() {
        Bot.addListener(new VoiceRole.Listener());
    }

    @Override
    public void initLate() {
        VoiceRole.initConfigs();
    }
}
