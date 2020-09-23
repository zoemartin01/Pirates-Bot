package me.zoemartin.bot.base.util;

import net.dv8tion.jda.internal.utils.tuple.Pair;

public class Parser {
    public static class User {
        public static boolean isParsable(String s) {
            return (s.matches("<@!?\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            return "null";
        }
    }

    public static class Channel {
        public static boolean isParsable(String s) {
            return (s.matches("<#\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            return "null";
        }
    }

    public static class Role {
        public static boolean isParsable(String s) {
            return (s.matches("<@&\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            return "null";
        }
    }

    public static class Emote {
        public static boolean isParsable(String s) {
            return (s.matches("<a:\\w+:\\d{17,19}>") || s.matches("<:\\w+:\\d{17,19}>") || s.matches("\\d{17,19}"));
        }

        public static String parse(String s) {
            if (isParsable(s))
                return s.replaceAll("\\D", "");
            return "null";
        }

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