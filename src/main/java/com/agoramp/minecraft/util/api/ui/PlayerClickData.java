package com.agoramp.minecraft.util.api.ui;

import com.agoramp.minecraft.util.data.packets.models.common.ClickType;
import lombok.Data;


@Data
public class PlayerClickData {
    private final int slotId;
    /** Button used */
    private final int usedButton;
    /** A unique number for the action, used for transaction handling */
    private final short actionNumber;
    /** The item stack present in the slot */
    private final Object clickedItem;
    /** Inventory operation mode */
    private final ClickType mode;

    private boolean cancelled = true;
}
