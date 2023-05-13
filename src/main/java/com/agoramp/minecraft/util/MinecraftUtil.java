package com.agoramp.minecraft.util;

import com.agoramp.minecraft.util.controller.InterfaceController;
import com.agoramp.minecraft.util.controller.PacketController;
import com.agoramp.minecraft.util.controller.TranslationController;

public class MinecraftUtil {
    public static Platform PLATFORM;

    public static void initialize(Platform platform) {
        PLATFORM = platform;
        InterfaceController.INSTANCE.preload();
        PacketController.INSTANCE.preload();
        TranslationController.INSTANCE.preload();
        InterfaceController.INSTANCE.load();
        PacketController.INSTANCE.load();
        TranslationController.INSTANCE.load();
    }
}
