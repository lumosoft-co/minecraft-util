package com.agoramp.minecraft.util.api.text.transform;

import com.agoramp.minecraft.util.api.text.color.Gradient;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Locale;

@Data
@AllArgsConstructor
public class ColorTransform implements TranslationTransform {
    private final Gradient gradient;

    protected ColorTransform() {
        gradient = null;
    }

    public static ColorTransform of(TextColor... colors) {
        if (colors.length == 1 && colors[0] instanceof Gradient) return new ColorTransform((Gradient) colors[0]);
        return new ColorTransform(Gradient.of(colors));
    }

    public static ColorTransform of(Gradient gradient) {
        return new ColorTransform(gradient);
    }

    @Override
    public Component apply(Component component, Locale locale) {
        return gradient.apply(component);
    }
}
