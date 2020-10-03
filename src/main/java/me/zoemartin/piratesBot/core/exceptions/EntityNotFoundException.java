package me.zoemartin.piratesBot.core.exceptions;

import net.dv8tion.jda.api.entities.ISnowflake;

public class EntityNotFoundException extends ReplyError {
    public EntityNotFoundException() {
        super("Error, entity not found!");
    }

    public <T extends ISnowflake> EntityNotFoundException(Class<T> type) {
        super("Error, " + type.getSimpleName() + " not found!");
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String format, Object... args) {
        super(format, args);
    }
}
