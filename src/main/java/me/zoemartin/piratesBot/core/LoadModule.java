package me.zoemartin.piratesBot.core;

import me.zoemartin.piratesBot.core.interfaces.Module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LoadModule {
    Class<? extends Module>[] loadAfter() default {};
}
