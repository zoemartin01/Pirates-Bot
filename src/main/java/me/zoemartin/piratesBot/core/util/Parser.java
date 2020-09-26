package me.zoemartin.piratesBot.core.util;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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