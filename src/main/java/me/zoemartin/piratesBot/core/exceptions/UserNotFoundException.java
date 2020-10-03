package me.zoemartin.piratesBot.core.exceptions;

import net.dv8tion.jda.api.entities.User;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException() {
        super(User.class);
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String format, Object... args) {
        super(format, args);
    }
}
