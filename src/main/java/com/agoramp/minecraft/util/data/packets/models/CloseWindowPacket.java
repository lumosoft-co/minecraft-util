package com.agoramp.minecraft.util.data.packets.models;

import lombok.Data;

@Data
public class CloseWindowPacket implements ModelledPacket {
    private final int windowId;
}
