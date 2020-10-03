package me.zoemartin.piratesBot.modules.baseCommands;

import de.androidpit.colorthief.ColorThief;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

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

        EmbedBuilder eb = new EmbedBuilder()
                              .setAuthor(guild.getName(), null, guild.getIconUrl())
                              .setThumbnail(guild.getIconUrl())
                              .setTitle("Server Info");
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
                guild.getTextChannelCache().size(), textLocked > 0 ? "(" + textLocked + " locked)" : ""), true)

            .addField("Members", String.format("Total: %d\nHumans: %d\nBots: %d",
                guild.getMemberCount(),
                guild.getMembers().stream().filter(member -> !member.getUser().isBot()).count(),
                guild.getMembers().stream().filter(member -> member.getUser().isBot()).count()), true)

            .addField("Roles", guild.getRoles().size() + "", true)
            .addField("Region", guild.getRegionRaw(), true)
            .addField("Categories", guild.getCategories().size() + "", true)

            .addField("Features", String.join("\n", guild.getFeatures()), true)

            .setFooter("ID: " + guild.getId() + " | Created")
            .setTimestamp(guild.getTimeCreated());

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
