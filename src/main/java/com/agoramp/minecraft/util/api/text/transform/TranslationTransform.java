package com.agoramp.minecraft.util.api.text.transform;

import net.kyori.adventure.text.Component;

import java.util.Locale;

public interface TranslationTransform {
    Component apply(Component component, Locale locale);
}
