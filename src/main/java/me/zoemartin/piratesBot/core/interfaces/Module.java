package me.zoemartin.piratesBot.core.interfaces;

public interface Module {
    void init();
    default void initLate() {
    }
}
