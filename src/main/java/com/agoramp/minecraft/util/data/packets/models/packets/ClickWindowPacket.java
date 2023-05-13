package com.agoramp.minecraft.util.data.packets.models.packets;

import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;
import com.agoramp.minecraft.util.data.packets.models.common.ClickType;
import lombok.Data;

@Data
public class ClickWindowPacket implements ModelledPacket {
    private final int containerId, slotNum, buttonNum;
    private final short actionNum;
    private final Object item;
    private final ClickType clickType;
}
