package me.zoemartin.bot.base;

import java.util.Collection;
import java.util.Set;

public enum CommandPerm {
    EVERYONE(0, "ALL"),
    BOT_USER(1, "USER"),
    BOT_MODERATOR(2, "MODERATOR"),
    BOT_MANAGER(3, "MANAGER"),
    BOT_ADMIN(4, "ADMIN"),
    OWNER(5, "BOT OWNER");

    private final int num;
    private final String name;

    CommandPerm(int num, String name) {
        this.num = num;
        this.name = name;
    }

    public int raw() {
        return num;
    }

    public static Collection<CommandPerm> valueCollection() {
        return Set.of(CommandPerm.values());
    }

    @Override
    public String toString() {
        return name;
    }
}
