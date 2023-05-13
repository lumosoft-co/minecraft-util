package com.agoramp.minecraft.util.api.text.transform;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Locale;

@Data
@AllArgsConstructor
public class ClickTransform implements TranslationTransform {
    private final ClickEvent action;

    protected ClickTransform() {
        action = null;
    }

    @Override
    public Component apply(Component component, Locale locale) {
        return component.clickEvent(action);
    }
}
