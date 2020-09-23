package me.zoemartin.bot.base.exceptions;

public class UnexpectedError extends ReplyError {
    public UnexpectedError() {
        super("An unexpected Error has occurred!");
    }

    public UnexpectedError(String message) {
        super(message);
    }

    public UnexpectedError(String format, Object... args) {
        super(format, args);
    }
}
