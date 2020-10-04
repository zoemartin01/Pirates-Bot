package me.zoemartin.piratesBot.core.util;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static class User {
        public static boolean isParsable(String s) {
            return (s.matches("<@!?\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        @NotNull
        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("User not parsable!");
        }
    }

    public static class Channel {
        public static boolean isParsable(String s) {
            return (s.matches("<#\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        @NotNull
        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("Channel not parsable!");
        }

        @Nullable
        public static VoiceChannel getVoiceChannel(Guild g, String s) {
            VoiceChannel vc;
            if (isParsable(s)) vc = g.getVoiceChannelById(parse(s));
            else {
                List<VoiceChannel> vcs = g.getVoiceChannelsByName(s, true);
                vc = vcs.isEmpty() ? null : vcs.get(0);
            }
            return vc;
        }

        @Nullable
        public static TextChannel getTextChannel(Guild g, String s) {
            TextChannel c;
            if (isParsable(s)) c = g.getTextChannelById(parse(s));
            else {
                List<TextChannel> cs = g.getTextChannelsByName(s, true);
                c = cs.isEmpty() ? null : cs.get(0);
            }
            return c;
        }
    }

    public static class Role {
        public static boolean isParsable(String s) {
            return (s.matches("<@&\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        @NotNull
        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("Role not parsable!");
        }

        @Nullable
        public static net.dv8tion.jda.api.entities.Role getRole(Guild g, String s) {
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
     * Message ID/Link Parser
     * Input Format: {channel_id, message_id} or {message_link}
     */
    public static class Message {
        public static boolean isParsable(String... s) {
            if (s.length == 1)
                return s[0].matches("^((?:https?://)?discordapp.com/channels/\\d{17,19}/(\\d{17,19})/(\\d{17,19}))$");
            if (s.length == 2)
                return Channel.isParsable(s[0]) && s[1].matches("^\\d{17,19}$");
            else return false;
        }

        /**
         * @param s the input string
         * @return A Pair with left the channel id and right the message id
         */
        @NotNull
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public static Pair<String, String> parse(String... s) {
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

    public static class Emote {
        public static boolean isParsable(String s) {
            return (s.matches("<a:\\w+:\\d{17,19}>") || s.matches("<:\\w+:\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        @NotNull
        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            else throw new IllegalArgumentException("Emote not parsable!");
        }

        @Nullable
        public static Pair<String, Type> parseWithType(String s) {
            return isParsable(s) ? (s.matches("<a:\\w+:\\d{17,19}>") ? Pair.of(parse(s), Type.ANIMATED) : Pair.of(parse(s), Type.STATIC)) : null;
        }

        public enum Type {
            STATIC, ANIMATED
        }
    }

    public static class Int {
        public static boolean isParsable(String s) {
            return (s.matches("[+-]?\\d+"));
        }

        public static int parse(String s) {
            if (isParsable(s))
                return Integer.parseInt(s.replaceAll("\\D", ""));
            return 0;
        }
    }
}