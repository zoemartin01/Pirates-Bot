package me.zoemartin.piratesBot.modules.debug;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.ReplyError;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import me.zoemartin.piratesBot.modules.commandProcessing.LoggedError;
import me.zoemartin.piratesBot.modules.moderation.WarnEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.UUID;

public class ReadError implements GuildCommand {
    @Override
    public @NotNull String name() {
        return "readerror";
    }

    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() == 1, CommandArgumentException::new);

        UUID uuid = UUID.fromString(args.get(0));

        Session s = DatabaseUtil.getSessionFactory().openSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();

        CriteriaQuery<LoggedError> q = cb.createQuery(LoggedError.class);
        Root<LoggedError> r = q.from(LoggedError.class);
        List<LoggedError> errors = s.createQuery(q.select(r).where(
            cb.equal(r.get("uuid"), uuid))).getResultList();

        LoggedError error = errors.isEmpty() ? null : errors.get(0);
        Check.notNull(error, () -> new ReplyError("No error with the ID `%s`", uuid));

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("Error Debug")
            .setDescription("```" + error.getError_message() + "\n\n" +
                                error.getError_stacktrace().substring(1, error.getError_stacktrace().length() - 1)
                                + "```")
            .addField("Guild", Bot.getJDA().getGuildById(error.getGuild_id()).toString(), true)
            .addField("Invoked by", error.getInvoked_message(), true);

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.OWNER;
    }

    @Override
    public @NotNull String usage() {
        return "<uuid>";
    }

    @Override
    public @NotNull String description() {
        return "read an error";
    }
}
