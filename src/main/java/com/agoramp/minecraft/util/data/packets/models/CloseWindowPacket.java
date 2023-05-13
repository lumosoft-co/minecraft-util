package com.agoramp.minecraft.util.data.packets.models;

import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;
import lombok.Data;

@Data
public class CloseWindowPacket implements ModelledPacket {
    private final int windowId;
}
