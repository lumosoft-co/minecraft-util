package com.agoramp.minecraft.util.data.packets;

import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;

import java.util.UUID;

public interface Handler {
    int weight();

    <T extends ModelledPacket> T handle(UUID playerId, T packet);
}
