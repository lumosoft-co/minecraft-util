package com.agoramp.minecraft.util.api.text.transform;

import com.agoramp.minecraft.util.api.text.Translation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;

@Data
@EqualsAndHashCode
public class SuffixTransform implements TranslationTransform {
    private Translation translation;
    private Component component;

    protected SuffixTransform() {
        translation = null;
    }

    public SuffixTransform(Translation translation) {
        this.translation = translation;
    }

    public SuffixTransform(Component component) {
        this.component = component;
    }

    public static SuffixTransform of(@PropertyKey(resourceBundle = "translations") String key, Object... args) {
        return new SuffixTransform(Translation.create(key, args));
    }

    @Override
    public Component apply(Component component, Locale locale) {
        component = Component.empty().append(component);
        if (this.translation != null) component = component.append(translation.translate(locale));
        if (this.component != null) component = component.append(this.component);
        return component;
    }
}
