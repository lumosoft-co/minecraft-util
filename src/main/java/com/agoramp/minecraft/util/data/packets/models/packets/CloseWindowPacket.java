package com.agoramp.minecraft.util.data.packets.models.packets;

import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;
import lombok.Data;

@Data
public class CloseWindowPacket implements ModelledPacket {
    private final int windowId;
}
