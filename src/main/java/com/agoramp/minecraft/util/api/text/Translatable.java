package com.agoramp.minecraft.util.api.text;

import com.agoramp.minecraft.util.MinecraftUtil;
import com.agoramp.minecraft.util.controller.TranslationController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

public interface Translatable extends ComponentLike {
    Component translate(Locale locale);

    default Component translate(UUID player) {
        if (player == null) return asComponent();
        return this.translate(MinecraftUtil.PLATFORM.getLocale(player));
    }

    @Override
    default @NotNull Component asComponent() {
        return this.translate(TranslationController.DEFAULT_LOCALE);
    }
}
