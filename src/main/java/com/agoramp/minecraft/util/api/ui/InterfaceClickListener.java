package com.agoramp.minecraft.util.api.ui;

import com.agoramp.minecraft.util.data.packets.models.ClickWindowPacket;

public interface InterfaceClickListener {

    // Return true to not resend inventory content after action
    boolean onClick(ClickWindowPacket event);
}
