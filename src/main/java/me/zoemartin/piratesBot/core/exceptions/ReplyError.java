package me.zoemartin.piratesBot.core.exceptions;

public class ReplyError extends RuntimeException {
    public ReplyError() {
        super();
    }

    public ReplyError(String message) {
        super(message);
    }

    public ReplyError(String format, Object... args) {
        super(String.format(format, args));
    }
}
