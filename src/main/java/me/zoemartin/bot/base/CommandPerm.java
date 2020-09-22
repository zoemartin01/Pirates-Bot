package me.zoemartin.bot.base;

import java.util.Collection;
import java.util.Set;

public enum CommandPerm {
    EVERYONE(0),
    BOT_USER(1),
    BOT_MODERATOR(2),
    BOT_MANAGER(3),
    BOT_ADMIN(4),
    OWNER(5);

    private final int num;

    CommandPerm(int num) {
        this.num = num;
    }

    public int raw() {
        return num;
    }

    public static Collection<CommandPerm> valueCollection() {
        return Set.of(CommandPerm.values());
    }
}
