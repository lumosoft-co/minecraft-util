package com.agoramp.minecraft.util.api.ui;

public interface InterfaceClickListener{

    //The event is by default cancelled. Can be renewed via the event
    void onClick(PlayerClickData event);
}
