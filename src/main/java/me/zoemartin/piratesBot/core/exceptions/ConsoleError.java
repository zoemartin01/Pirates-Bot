package me.zoemartin.piratesBot.core.exceptions;

public class ConsoleError extends RuntimeException {
    public ConsoleError() {
        super();
    }

    public ConsoleError(String message) {
        super(message);
    }

    public ConsoleError(String format, Object... args) {
        super(String.format(format, args));
    }
}
