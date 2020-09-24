package me.zoemartin.bot.base.util;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.exceptions.ReplyError;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CacheUtils {
    private CacheUtils() {
        throw new IllegalAccessError();
    }

    @NotNull
    public static User getUser(String id) {
        Check.notNull(id, () -> new IllegalArgumentException("id may not be null"));
        Check.check(Parser.User.isParsable(id), () -> new IllegalArgumentException("id not in correct format"));

        try {
            User u = Bot.getJDA().getUserById(id);
            return u == null ? Bot.getJDA().retrieveUserById(id).complete() : u;
        } catch (ErrorResponseException e) {
            throw new ReplyError("Error, user not found!");
        }
    }

    @NotNull
    public static Member getMemberExplicit(Guild guild, String id) {
        Check.notNull(guild, () -> new IllegalArgumentException("Guild may not be null"));
        Check.notNull(id, () -> new IllegalArgumentException("id may not be null"));
        Check.check(Parser.User.isParsable(id), () -> new IllegalArgumentException("id not in correct format"));

        User u = getUser(id);

        try {
            Member m = guild.getMember(u);
            return m == null ? guild.retrieveMember(u).complete() : m;
        } catch (ErrorResponseException e) {
            throw new ReplyError("Error, member not found!");
        }
    }

    @Nullable
    public static Member getMember(Guild guild, String id) {
        if (guild == null || id == null || !Parser.User.isParsable(id)) return null;

        User u;
        try {
            u = getUser(id);
        } catch (ReplyError e) {
            return null;
        }

        try {
            Member m = guild.getMember(u);
            return m == null ? guild.retrieveMember(u).complete() : m;
        } catch (ErrorResponseException e) {
            return null;
        }
    }
}
