package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfo implements GuildCommand {
    @Override
    public String name() {
        return "userinfo";
    }

    @Override
    public String regex() {
        return "i|userinfo|info|profile";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.isEmpty() || args.size() == 1 && Parser.User.isParsable(args.get(0)),
            CommandArgumentException::new);

        String userId = args.isEmpty() ? user.getId() : Parser.User.parse(args.get(0));

        User u = CacheUtils.getUser(userId);
        Member member = CacheUtils.getMember(original.getGuild(), userId);

        boolean b = member != null;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(u.getAsTag(), null, u.getEffectiveAvatarUrl());
        if (b) eb.setColor(member.getColor());
        eb.setDescription(u.getAsMention());
        eb.setThumbnail(u.getAvatarUrl());
        eb.setFooter("ID: " + u.getId());
        eb.setTimestamp(Instant.now());
        eb.addField("Username", u.getAsTag(), true);

        if (b) eb.addField("Nickname", member.getEffectiveName(), true);

        eb.addField("Avatar", String.format("[Link](%s)", u.getEffectiveAvatarUrl()), true);

        if (b) eb.addField("Highest Rank",
            member.getRoles().isEmpty() ? "n/a" : member.getRoles().get(0).getName(), true);

        eb.addField("Registered at",
            Timestamp.valueOf(u.getTimeCreated().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()) + " UTC",
            true);
        eb.addField("Account Age", MessageUtils.dateAgo(u.getTimeCreated(), OffsetDateTime.now()), true);

        if (b) {
            eb.addField("Joined at",
                Timestamp.valueOf(member.getTimeJoined().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()) + " UTC",
                true);

            eb.addField("Joined Server Age", MessageUtils.dateAgo(member.getTimeJoined(), OffsetDateTime.now())
                                                 + "\n"
                                                 + ChronoUnit.DAYS.between(member.getTimeJoined(), OffsetDateTime.now())
                                                 + " days after the server was created", true);

            String roles = MessageUtils.mergeComma(member.getRoles().stream().map(Role::getAsMention).collect(Collectors.toList()));
            eb.addField(String.format("Roles (%s)", member.getRoles().size()),
                roles.length() <= 1024 ? roles : "Too many to list", false);
        }


        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String usage() {
        return "userinfo [user]";
    }

    @Override
    public String description() {
        return "Gives Information about a user";
    }
}
