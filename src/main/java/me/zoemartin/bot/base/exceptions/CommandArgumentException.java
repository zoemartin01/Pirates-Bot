package me.zoemartin.bot.base.exceptions;

public class CommandArgumentException extends ReplyError {
    public CommandArgumentException() {
        super();
    }

    public CommandArgumentException(String message) {
        super(message);
    }
}
