package me.zoemartin.bot.base;

import me.zoemartin.bot.base.interfaces.Module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LoadModule {
    Class<? extends Module>[] loadAfter() default {};
}
