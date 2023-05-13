package com.agoramp.minecraft.util.data.packets.models;

import lombok.Data;

@Data
public class ClickWindowPacket implements ModelledPacket {
    private final int containerId, slotNum, buttonNum;
    private final short actionNum;
    private final Object item;
    private final InventoryClickType clickType;

    public enum InventoryClickType {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;
    }
}
