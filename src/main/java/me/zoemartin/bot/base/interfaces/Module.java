package me.zoemartin.bot.base.interfaces;

public interface Module {
    void init();
    default void initLate() {
    }
}
