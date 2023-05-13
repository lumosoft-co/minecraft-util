package com.agoramp.minecraft.util;

import com.agoramp.minecraft.util.controller.PacketController;
import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.Locale;
import java.util.UUID;

public interface Platform {

    void schedule(Runnable action, int tickDelay);

    default void schedule(Runnable action) {
        schedule(action, 0);
    }

    int openInventory(UUID player, int size, ComponentLike title);

    Locale getLocale(UUID player);

    void sendMessage(UUID player, ComponentLike message);

    default Component parse(Object value, Locale locale) {
        return Component.text(String.valueOf(value));
    }

    void sendPacket(UUID player, ModelledPacket packet);

    default ModelledPacket handlePacket(UUID player, ModelledPacket packet) {
        return PacketController.INSTANCE.handle(player, packet);
    }
}
