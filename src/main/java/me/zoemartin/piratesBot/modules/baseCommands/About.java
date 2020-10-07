package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.interfaces.Command;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class About implements Command {
    @Override
    public String name() {
        return "about";
    }

    @Override
    public String regex() {
        return "about|botinfo";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("About").setColor(0xdf136c);

        String version = getClass().getPackage().getImplementationVersion();

        eb.addField("Bot Version", version == null ? "DEV BUILD" : version, true);
        eb.addField("Java Version", System.getProperty("java.version"), true);
        eb.addField("JDA Version", JDA.class.getPackage().getImplementationVersion(), true);
        eb.addField("Author", "<@!212591138945630213> / zowee#0001", true);
        eb.addField("Source Code", "https://github.com/zoemartin01/Pirates-Bot", false);
        eb.setThumbnail(Bot.getJDA().getSelfUser().getAvatarUrl());
        eb.setFooter("Made with JDA",
            "https://raw.githubusercontent.com/DV8FromTheWorld/JDA/assets/assets/readme/logo.png");

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String description() {
        return "Shows info about the bot";
    }
}
