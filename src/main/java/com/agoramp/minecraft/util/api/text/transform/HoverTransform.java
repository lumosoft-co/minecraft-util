package com.agoramp.minecraft.util.api.text.transform;

import com.agoramp.minecraft.util.api.text.Translation;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;

@Data
@AllArgsConstructor
public class HoverTransform implements TranslationTransform {
    private final Translation translation;

    protected HoverTransform() {
        translation = Translation.create("");
    }

    public static HoverTransform of(@PropertyKey(resourceBundle = "translations") String key, Object... args) {
        return new HoverTransform(Translation.create(key, args));
    }

    @Override
    public Component apply(Component component, Locale locale) {
        HoverEvent<?> existing = component.hoverEvent();
        Component ourHover = translation.translate(locale);
        if (existing != null) {
            if (existing.action() == HoverEvent.Action.SHOW_TEXT) {
                ourHover = ((Component) existing.value()).append(ourHover);
            } else {
                return component;
            }
        }
        return component.hoverEvent(ourHover);
    }

    public HoverTransform withTransform(TranslationTransform transform) {
        translation.withTransform(transform);
        return this;
    }
}
