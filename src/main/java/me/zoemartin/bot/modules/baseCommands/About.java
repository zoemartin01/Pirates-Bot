package me.zoemartin.bot.modules.baseCommands;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.interfaces.Command;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class About implements Command {
    @Override
    public String name() {
        return "about";
    }

    @Override
    public String regex() {
        return "about|info";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("About").setColor(0xdf136c);

        eb.addField("Bot Version", Bot.VERSION, true);
        eb.addField("Java Version", System.getProperty("java.version"), true);
        eb.addField("JDA Version", Bot.JDA_VERSION, true);
        eb.addField("Author", "<@!212591138945630213> / zowee#0001", true);
        eb.setThumbnail(Bot.getJDA().getSelfUser().getAvatarUrl());
        eb.setFooter("Made with JDA",
            "https://raw.githubusercontent.com/DV8FromTheWorld/JDA/assets/assets/readme/logo.png");

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public Permission required() {
        return Permission.UNKNOWN;
    }

    @Override
    public String usage() {
        return "about";
    }

    @Override
    public String description() {
        return "Shows info about the bot";
    }
}
