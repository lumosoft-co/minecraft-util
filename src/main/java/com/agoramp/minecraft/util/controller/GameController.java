package com.agoramp.minecraft.util.controller;

public interface GameController {

    default void preload() {
        PacketController.INSTANCE.register(this);
    }

    default void load() {}

    default void unload() {}
}
