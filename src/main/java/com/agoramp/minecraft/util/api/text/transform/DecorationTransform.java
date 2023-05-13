package com.agoramp.minecraft.util.api.text.transform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Locale;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DecorationTransform implements TranslationTransform {
    private final TextDecoration decoration;
    private boolean state = true;

    public DecorationTransform() {
        decoration = null;
    }

    @Override
    public Component apply(Component component, Locale locale) {
        return component.decoration(decoration, state);
    }
}
