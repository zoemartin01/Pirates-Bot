package me.zoemartin.piratesBot.core.exceptions;

public class UnexpectedError extends RuntimeException {
    public UnexpectedError() {
        super("An unexpected Error has occurred!");
    }

    public UnexpectedError(String message) {
        super(message);
    }

    public UnexpectedError(Exception e) {
        super("An unexpected Error has occurred!");
        setStackTrace(e.getStackTrace());
    }

    public UnexpectedError(String format, Object... args) {
        super(String.format(format, args));
    }
}
