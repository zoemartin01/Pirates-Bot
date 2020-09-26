package me.zoemartin.piratesBot.core.exceptions;

public class CommandArgumentException extends ReplyError {
    public CommandArgumentException() {
        super();
    }

    public CommandArgumentException(String message) {
        super(message);
    }
}
