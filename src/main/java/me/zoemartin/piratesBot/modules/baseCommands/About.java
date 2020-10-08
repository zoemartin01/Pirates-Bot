package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.interfaces.Command;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;

public class About implements Command {
    private String JDA_VERSION = null;

    @Override
    public @NotNull String name() {
        return "about";
    }

    @Override
    public @NotNull String regex() {
        return "about|botinfo";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("About").setColor(0xdf136c);

        String version = getClass().getPackage().getImplementationVersion();

        if (JDA_VERSION == null) findVersion();

        eb.addField("Bot Version", version == null ? "DEV BUILD" : version, true);
        eb.addField("Java Version", System.getProperty("java.version"), true);
        eb.addField("JDA Version", JDA_VERSION, true);
        eb.addField("Author", "<@!212591138945630213> / zowee#0001", true);
        eb.addField("Source Code", "https://github.com/zoemartin01/Pirates-Bot", false);
        eb.setThumbnail(Bot.getJDA().getSelfUser().getAvatarUrl());
        eb.setFooter("Made with JDA",
            "https://raw.githubusercontent.com/DV8FromTheWorld/JDA/assets/assets/readme/logo.png");

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public @NotNull String description() {
        return "Shows info about the bot";
    }

    private void findVersion() {
        Enumeration<URL> resources;
        try {
            resources = getClass().getClassLoader()
                                             .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                JDA_VERSION = manifest.getMainAttributes().getValue("jda-version");
            }
        } catch (IOException e) {
            JDA_VERSION = "UNKNOWN";
        }
    }
}
