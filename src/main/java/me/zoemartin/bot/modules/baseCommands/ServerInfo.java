package me.zoemartin.bot.modules.baseCommands;

import de.androidpit.colorthief.ColorThief;
import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.interfaces.GuildCommand;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;

public class ServerInfo implements GuildCommand {
    @Override
    public String name() {
        return "serverinfo";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.isEmpty(), CommandArgumentException::new);

        Guild guild = original.getGuild();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(guild.getName(), null, guild.getIconUrl());
        eb.setThumbnail(guild.getIconUrl());
        eb.setTitle("Server Info");
        try {
            int[] color = ColorThief.getColor(ImageIO.read(new URL(guild.getIconUrl())));
            eb.setColor(new Color(color[0], color[1], color[2]));
        } catch (IOException ignored) {
        }

        guild.loadMembers().get();
        eb.addField("Owner", guild.getOwner().getAsMention(), true);

        long vcLocked = guild.getVoiceChannelCache().stream().filter(c -> {
            if (c.getPermissionOverride(guild.getPublicRole()) == null) return false;

            EnumSet<Permission> overrides = c.getPermissionOverride(guild.getPublicRole()).getDenied();
            return overrides.contains(Permission.MESSAGE_READ) || overrides.contains(Permission.VOICE_CONNECT);
        }).count();
        long textLocked = guild.getTextChannelCache().stream().filter(c -> {
            if (c.getPermissionOverride(guild.getPublicRole()) == null) return false;

            EnumSet<Permission> overrides = c.getPermissionOverride(guild.getPublicRole()).getDenied();
            return overrides.contains(Permission.MESSAGE_READ) || overrides.contains(Permission.MESSAGE_WRITE);
        }).count();

        eb.addField("Channels",
            String.format("<:voice_channel:758143690845454346> %d %s\n<:text_channel:758143729902288926> %d %s",
                guild.getVoiceChannelCache().size(), vcLocked > 0 ? "(" + vcLocked + " locked)" : "",
                guild.getTextChannelCache().size(), textLocked > 0 ? "(" + textLocked + " locked)" : ""), true);

        eb.addField("Members", String.format("Total: %d\nHumans: %d\nBots: %d",
            guild.getMemberCount(),
            guild.getMembers().stream().filter(member -> member.getUser().isBot()).count(),
            guild.getMembers().stream().filter(member -> !member.getUser().isBot()).count()), true);

        eb.addField("Roles", guild.getRoles().size() + "", true);
        eb.addField("Region", guild.getRegionRaw(), true);
        eb.addField("Categories", guild.getCategories().size() + "", true);

        eb.addField("Features", MessageUtils.mergeNewLine(guild.getFeatures()), true);

        eb.setFooter("ID: " + guild.getId() + " | Created");
        eb.setTimestamp(guild.getTimeCreated());

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String usage() {
        return "serverinfo";
    }

    @Override
    public String description() {
        return "Shows info about the server";
    }
}
