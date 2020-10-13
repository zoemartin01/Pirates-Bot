package me.zoemartin.piratesBot.modules.misc;

import de.androidpit.colorthief.ColorThief;
import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.CacheUtils;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Avatar implements GuildCommand {
    @Override
    public @NotNull String name() {
        return "avatar";
    }

    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        User u = null;
        String arg;
        if (args.isEmpty()) u = user.getUser();
        else if (Parser.User.isParsable(arg = lastArg(0, args, original))) u = CacheUtils.getUser(arg);
        else if (Parser.User.tagIsParsable(arg)) u = Bot.getJDA().getUserByTag(arg);
        if (u == null) u = user.getUser();

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
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public @NotNull String usage() {
        return "[user]";
    }

    @Override
    public @NotNull String description() {
        return "Shows the avatar for a user";
    }
}
