package me.zoemartin.piratesBot.core;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collection;
import java.util.Set;

@Converter
public enum CommandPerm  {
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

    public static CommandPerm fromNum(Integer num) {
        if (num == null) return null;

        return Set.of(CommandPerm.values()).stream().filter(perm -> num.equals(perm.raw())).findAny()
                   .orElse(null);
    }

    @Override
    public String toString() {
        return name;
    }

    public static class Converter implements AttributeConverter<CommandPerm, Integer> {
        @Override
        public Integer convertToDatabaseColumn(CommandPerm attribute) {
            return attribute.raw();
        }

        @Override
        public CommandPerm convertToEntityAttribute(Integer dbData) {
            return CommandPerm.fromNum(dbData);
        }
    }
}
