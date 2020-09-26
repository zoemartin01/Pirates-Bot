package me.zoemartin.piratesBot.modules.debug;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.MessageUtils;
import net.dv8tion.jda.api.entities.*;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class Dump implements GuildCommand {
    @Override
    public String name() {
        return "dump";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        List<Member> members = original.getGuild().loadMembers().get();

        String dump = MessageUtils.mergeNewLine(
            members.stream().map(member -> String.format("%d. %s / %s / %s / Joined at: %s UTC",
                members.indexOf(member) + 1, member.getUser().getAsTag(), member.getEffectiveName(), member.getId(),
                Timestamp.valueOf(member.getTimeCreated().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()))
            ).collect(Collectors.toList()));

        if (dump.length() <= 1990) channel.sendMessage("```" + dump + "```").queue();
        else {
            channel.sendFile(dump.getBytes(), String.format("%s-dump-%s.txt",
                original.getGuild().getName(), System.currentTimeMillis())).queue();
        }
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @Override
    public String usage() {
        return "dump";
    }

    @Override
    public String description() {
        return "Refreshes the cache and dumps all users";
    }
}
