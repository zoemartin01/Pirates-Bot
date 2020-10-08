package me.zoemartin.piratesBot.core.util;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of Parser utility classes with additional finder utilities added
 *
 * @param <T>
 *     The type of Object used as the parsers input
 * @param <K>
 *     The type of Object used as the parsers output
 *
 * @author zoemartin
 * @since 0.0.1
 */
public abstract class Parser<T, K> {
    public static final UserParser User = new UserParser();
    public static final ChannelParser Channel = new ChannelParser();
    public static final IntegerParser Int = new IntegerParser();
    public static final MessageParser Message = new MessageParser();
    public static final EmoteParser Emote = new EmoteParser();
    public static final RoleParser Role = new RoleParser();

    /**
     * Returns true if the input is in a valid format for the parser
     *
     * @param t
     *     The input string
     *
     * @return true if the input is parsable
     */
    public abstract boolean isParsable(T t);

    /**
     * Parses a input string in a valid format and returns a parsed version
     *
     * @param t
     *     The input string
     *
     * @return Never-null parsed object
     *
     * @see #isParsable(Object)
     */
    @Nonnull
    public abstract K parse(T t);

    /**
     * Parser Utility for {@link User}
     * <br>
     * The parser utility makes it possible to conveniently use user mentions and ids in an input.
     *
     * @see UserParser
     * @see IMentionable
     */
    public static class UserParser extends Parser<String, String> {
        private UserParser() {
        }

        /**
         * Returns true if the input is either in the discord user mention format or just an id with a valid length.
         * <br><br>
         * Valid formats:
         * <p>(user_id is a string of numbers that is either 17, 18 or 19 characters long)
         * <ul>
         *      <li>{@code user_id}</li>
         *      <li>{@code <@user_id>}</li>
         *      <li>{@code <@!user_id>}</li>
         * </ul>
         *
         * @return true if the input contains a valid id
         */
        @Override
        public boolean isParsable(String s) {
            return (s.matches("<@!?\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        /**
         * Returns true if the input in the discord user tag format.
         * <br><br>
         * Format: x#yyyy
         * <p>With x -> any string between the lengths 2 and 32, and y -> a number
         *
         * @param s
         *     The input string
         *
         * @return true if the input contains a valid id
         */
        public boolean tagIsParsable(String s) {
            return (s.matches("(.{2,32})#(\\d{4})"));
        }

        /**
         * @return Never-null user id
         *
         * @throws IllegalArgumentException
         *     If the provided input is not in a valid format
         * @see #isParsable(String)
         */
        @Override
        @NotNull
        public String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("User not parsable!");
        }
    }

    /**
     * Parser and Finder Utility for {@link MessageChannel} and some of its implementations
     * <br>
     * The parser utility makes it possible to conveniently use channel mentions and ids in an input.
     * <p> The finder utilities makes it convenient to find either a {@link TextChannel} or {@link VoiceChannel}
     * from an input string either containing the mention, the id or the role name
     *
     * @see MessageChannel
     * @see TextChannel
     * @see VoiceChannel
     * @see IMentionable
     */
    public static class ChannelParser extends Parser<String, String> {
        private ChannelParser() {
        }

        /**
         * Returns true if the input is either in the discord channel mention format or just an id with a valid length.
         * <br><br>
         * Valid formats:
         * <p>(channel_id is a string of numbers that is either 17, 18 or 19 characters long)
         * <ul>
         *      <li>{@code channel_id}</li>
         *      <li>{@code <#channel_id>}</li>
         * </ul>
         *
         * @return true if the input contains a valid id
         */
        @Override
        public boolean isParsable(@Nonnull String s) {
            return (s.matches("<#\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        /**
         * @return Never-null channel id
         *
         * @throws IllegalArgumentException
         *     If the provided input is not in a valid format
         * @see #isParsable(String)
         */
        @Nonnull
        @Override
        public String parse(@Nonnull String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("Channel not parsable!");
        }

        /**
         * Gets a {@link VoiceChannel} from the provided {@link Guild} that matches the provided id if the input string
         * contains the channel mention or the channel id, or the first voice channel found that has a matching name.
         * <br>
         * If there is no such {@link VoiceChannel} then this returns {@code null}.
         *
         * @param g
         *     The guild
         * @param s
         *     The input string
         *
         * @return Possibly-null {@link VoiceChannel} with matching id or name
         *
         * @see #isParsable(String)
         */
        @Nullable
        public VoiceChannel getVoiceChannel(@Nonnull Guild g, @Nonnull String s) {
            VoiceChannel vc;
            if (isParsable(s)) vc = g.getVoiceChannelById(parse(s));
            else {
                List<VoiceChannel> vcs = g.getVoiceChannelsByName(s, true);
                vc = vcs.isEmpty() ? null : vcs.get(0);
            }
            return vc;
        }

        /**
         * Gets a {@link TextChannel} from the provided {@link Guild} that matches the provided id if the input string
         * contains the channel mention or the channel id, or the first voice channel found that has a matching name.
         * <br>
         * If there is no such {@link TextChannel} then this returns {@code null}.
         *
         * @param g
         *     The guild
         * @param s
         *     The input string
         *
         * @return Possibly-null {@link TextChannel} with matching id or name
         */
        @Nullable
        public TextChannel getTextChannel(@Nonnull Guild g, @Nonnull String s) {
            TextChannel c;
            if (isParsable(s)) c = g.getTextChannelById(parse(s));
            else {
                List<TextChannel> cs = g.getTextChannelsByName(s, true);
                c = cs.isEmpty() ? null : cs.get(0);
            }
            return c;
        }
    }

    /**
     * Parser and Finder Utility for {@link Role}
     * <br>
     * The parser utility makes it possible to conveniently use role mentions and ids in an input.
     * <p> The finder utility makes it convenient to find the role from an input string either containing the mention,
     * the id or the role name
     *
     * @see Role
     * @see IMentionable
     */
    public static class RoleParser extends Parser<String, String> {
        private RoleParser() {
        }

        /**
         * Returns true if the input is either in the discord role mention format or just an id with a valid length.
         * <br><br>
         * Valid formats:
         * <p>(role_id is a string of numbers that is either 17, 18 or 19 characters long)
         * <ul>
         *      <li>{@code role_id}</li>
         *      <li>{@code <@&role_id>}</li>
         * </ul>
         *
         * @return true if the input contains a valid id
         */
        @Override
        public boolean isParsable(@Nonnull String s) {
            return (s.matches("<@&\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        /**
         * @return Never-null role id
         *
         * @throws IllegalArgumentException
         *     If the provided input is not in a valid format
         * @see #isParsable(String)
         */
        @Override
        @Nonnull
        public String parse(@Nonnull String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("Role not parsable!");
        }

        /**
         * Gets a {@link Role} from the provided {@link Guild} that matches the provided id if the input string contains
         * the role mention or the role id, or the first role that has a matching name.
         * <br>
         * If there is no such {@link Role} then this returns {@code null}.
         *
         * @param g
         *     The guild
         * @param s
         *     The input string
         *
         * @return Possibly-null {@link Role} with matching id or name
         *
         * @see #isParsable(String)
         */
        @Nullable
        public net.dv8tion.jda.api.entities.Role getRole(@Nonnull Guild g, @Nonnull String s) {
            net.dv8tion.jda.api.entities.Role r;
            if (isParsable(s)) r = g.getRoleById(parse(s));
            else {
                List<net.dv8tion.jda.api.entities.Role> roles = g.getRolesByName(s, true);
                r = roles.isEmpty() ? null : roles.get(0);
            }
            return r;
        }
    }

    /**
     * Parser for {@link Message}
     * <br>
     * The parser utility makes it easier to handle input referring to a {@link Message} in a {@link TextChannel}.
     *
     * @see Message
     * @see TextChannel
     */
    public static class MessageParser extends Parser<String[], Pair<String, String>> {
        private MessageParser() {
        }

        /**
         * Returns true if the input is either an array and contains the channel and message id or if it just contains
         * the message link
         * <br><br>
         * Valid formats:
         * <ul>
         *      <li>{@code {channel_id, message_id}}</li>
         *      <li>{@code message_link}</li>
         * </ul>
         *
         * @return true if the input is in a valid format
         */
        public boolean isParsable(String... s) {
            if (s.length == 1)
                return s[0].matches("^((?:https?://)?discordapp.com/channels/\\d{17,19}/(\\d{17,19})/(\\d{17,19}))$");
            if (s.length == 2)
                return Channel.isParsable(s[0]) && s[1].matches("^\\d{17,19}$");
            else return false;
        }

        /**
         * @return A Pair with left the channel id and right the message id
         */
        @NotNull
        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public Pair<String, String> parse(String... s) {
            if (!isParsable(s)) throw new IllegalArgumentException("Message not parsable!");

            if (s.length == 1) {
                Matcher matcher = Pattern.compile(
                    "^((?:https?://)?discordapp.com/channels/\\d{17,19}/(\\d{17,19})/(\\d{17,19}))$"
                ).matcher(s[0]);

                matcher.find();
                return Pair.of(matcher.group(2), matcher.group(3));
            }

            return Pair.of(Channel.parse(s[0]), s[1]);
        }
    }

    /**
     * Parser for {@link Emote}
     * <br>
     * The parser utility makes it possible to conveniently use emote mentions and ids in an input.
     *
     * @see Emote
     * @see IMentionable
     */
    public static class EmoteParser extends Parser<String, String> {
        private EmoteParser() {
        }

        /**
         * Returns true if the input is either in the discord emote mention format or just an id with a valid length.
         * <br><br>
         * Valid formats:
         * <p>(emote_id is a string of numbers that is either 17, 18 or 19 characters long)
         * <ul>
         *      <li>{@code emote_id}</li>
         *      <li>{@code <:name:emote_id>}</li>
         *      <li>{@code <a:name:emote_id>}</li>
         * </ul>
         *
         * @return true if the input contains a valid id
         */
        @Override
        public boolean isParsable(String s) {
            return (s.matches("<a:\\w+:\\d{17,19}>") || s.matches("<:\\w+:\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        /**
         * @return Never-null emote id
         *
         * @throws IllegalArgumentException
         *     If the provided input is not in a valid format
         */
        @Override
        @NotNull
        public String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("Emote not parsable!");
        }

        /**
         * Parses a input string in the valid format (see {@link #isParsable(String)}) and returns a pair with the emote
         * id and the {@link EmoteType}
         *
         * @param s
         *     The input string
         *
         * @return Possibly-Null pair containing the emote id and the EmoteType
         *
         * @see #isParsable(String)
         * @see EmoteType
         */
        @Nullable
        public Pair<String, EmoteType> parseWithType(String s) {
            return isParsable(s) ?
                       (s.matches("<a:\\w+:\\d{17,19}>") ? Pair.of(parse(s), EmoteType.ANIMATED)
                            : Pair.of(parse(s), EmoteType.STATIC))
                       : null;
        }

        /**
         * Represents if an Emote is animated or static
         */
        public enum EmoteType {
            STATIC, ANIMATED
        }
    }

    /**
     * Parser for {@link Integer}
     * <br>
     * The parser utility makes it possible to conveniently use {@link Integer}s from a {@link String} input.
     *
     * @see Integer
     */
    public static class IntegerParser extends Parser<String, Integer> {
        private IntegerParser() {
        }

        @Override
        public boolean isParsable(String s) {
            return (s.matches("[+-]?\\d+"));
        }

        /**
         * Parses a input string and returns it as a {@link Integer} if it is in the valid format. Returns 0 if input is
         * not in correct format
         *
         * @return Never-null integer
         */
        @Override
        @Nonnull
        public Integer parse(String s) {
            if (isParsable(s))
                return Integer.parseInt(s.replaceAll("\\D", ""));
            return 0;
        }
    }
}