package com.agoramp.minecraft.util.data.packets.models;

import lombok.Data;

@Data
public class SetSlotPacket<ItemStack> implements ModelledPacket {
    private final int windowId;
    private final int slot;
    private final ItemStack item;
}
