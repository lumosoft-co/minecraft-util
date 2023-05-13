package com.agoramp.minecraft.util.data.packets.models.packets;

import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;
import lombok.Data;

@Data
public class WindowItemsPacket<ItemStack> implements ModelledPacket {
    private final int windowId;
    private final ItemStack[] items;
    private final Object[][] metadata;
}
