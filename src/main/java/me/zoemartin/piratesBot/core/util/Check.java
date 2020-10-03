package me.zoemartin.piratesBot.core.util;

import me.zoemartin.piratesBot.core.exceptions.EntityNotFoundException;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.util.Collection;
import java.util.function.Supplier;

public class Check {
    public static void notNull(Object o, String message) {
        if (o == null) throw new IllegalStateException(message);
    }

    public static void notNull(Object o, Supplier<? extends RuntimeException> supplier) {
        if (o == null) throw supplier.get();
    }

    public static void notNull(Object o) {
        if (o == null) throw new IllegalStateException("Object may not be null");
    }

    public static void notEmpty(Object[] o, String message) {
        notNull(o);
        if (o.length == 0) throw new IllegalStateException(message);
    }

    public static void notEmpty(Collection<?> c, String message) {
        notNull(c);
        if (c.isEmpty()) throw new IllegalStateException(message);
    }

    public static void check(boolean expected, Supplier<? extends RuntimeException> supplier) {
        if (!expected) throw supplier.get();
    }

    public static <T extends ISnowflake> void entityNotNull(T t, Class<T> tClass) {
        if (t == null) throw new EntityNotFoundException(tClass);
    }
}
