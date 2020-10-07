package me.zoemartin.piratesBot.modules.misc;

import de.androidpit.colorthief.ColorThief;
import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Avatar implements GuildCommand {
    @Override
    public String name() {
        return "avatar";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.isEmpty() || args.size() == 1, CommandArgumentException::new);

        User u;
        if (args.isEmpty()) u = user;
        else u = Parser.User.isParsable(args.get(0)) ? CacheUtils.getUser(args.get(0))
                     : Bot.getJDA().getUserByTag(args.get(0));
        if (u == null) u = user;

        String avatarId = u.getAvatarId();
        String id = u.getId();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Avatar for " + u.getAsTag());
        eb.setImage(u.getEffectiveAvatarUrl() + "?size=1024");

        if (avatarId == null) {
            eb.setDescription("[**Link**](" + u.getEffectiveAvatarUrl() + ")");
        } else {
            eb.setDescription(String.format("**Link as**\n" +
                                                "[png](https://cdn.discordapp.com/avatars/%s/%s.png?size=1024) | " +
                                                "[jpg](https://cdn.discordapp.com/avatars/%s/%s.jpg?size=1024) | " +
                                                "[webp](https://cdn.discordapp.com/avatars/%s/%s.webp?size=1024)",
                id, avatarId, id, avatarId, id, avatarId));
        }

        try {
            int[] color = ColorThief.getColor(ImageIO.read(new URL(u.getEffectiveAvatarUrl())));
            eb.setColor(new Color(color[0], color[1], color[2]));
        } catch (IOException ignored) {
        }

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String usage() {
        return "[user]";
    }

    @Override
    public String description() {
        return "Shows the avatar for a user";
    }
}
