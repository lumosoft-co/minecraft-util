package com.agoramp.minecraft.util.api.text.transform;

import com.agoramp.minecraft.util.api.text.Translation;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;

@Data
public class PrefixTransform implements TranslationTransform {
    private Translation translation;
    private Component component;

    protected PrefixTransform() {
        translation = null;
    }

    public PrefixTransform(Translation translation) {
        this.translation = translation;
    }

    public PrefixTransform(Component component) {
        this.component = component;
    }

    public static PrefixTransform of(@PropertyKey(resourceBundle = "translations") String key, Object... args) {
        return new PrefixTransform(Translation.create(key, args));
    }

    @Override
    public Component apply(Component component, Locale locale) {
        if (this.translation != null) component = translation.translate(locale).append(component);
        if (this.component != null) component = this.component.append(component);
        return component;
    }
}
