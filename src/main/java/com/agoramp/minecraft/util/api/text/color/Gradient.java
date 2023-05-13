package com.agoramp.minecraft.util.api.text.color;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextColor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Gradient {

    @Getter
    private final TextColor[] colors;
    @Getter @Setter
    private float phase;

    protected Gradient(final float phase, final List<TextColor> colors) {
        this.phase = phase;

        if (colors.isEmpty()) {
            this.colors = new TextColor[]{TextColor.color(0xffffff), TextColor.color(0x000000)};
        } else {
            this.colors = colors.toArray(new TextColor[0]);
        }
    }

    public static Gradient of(TextColor... colors) {
        return new Gradient(0, Arrays.asList(colors));
    }

    public static Gradient of(float phase, TextColor... colors) {
        return new Gradient(phase, Arrays.asList(colors));
    }

    public TextColor getTextColor(float phase) {
        if (colors.length == 1) return colors[0];
        float step = phase / (1f / (colors.length - 1));
        int i = (int) step;
        step -= i;
        i %= colors.length;
        TextColor from = colors[i];
        TextColor to = colors[i == colors.length - 1 ? 0 : i + 1];
        return TextColor.lerp(step, from, to);
    }

    public TextColor getTextColor() {
        return getTextColor((float) Math.random());
    }

    public Component apply(Component component) {
        if (colors.length == 1) return component.color(colors[0]);
        AtomicInteger count = new AtomicInteger();
        ComponentFlattener.textOnly().flatten(component, s -> count.addAndGet(s.length()));
        List<Component> components = new LinkedList<>();
        recursiveApply(component, 1f / count.get(), new AtomicReference<>(phase), components);
        return Component.empty().children(components);
    }

    private void recursiveApply(Component component, float progressStep, AtomicReference<Float> progress, List<Component> out) {
        if (component instanceof TextComponent) {
            String text = ((TextComponent) component).content();
            for (int j = 0; j < text.length(); j++) {
                Component sub = Component.text(text.charAt(j))
                        .color(getTextColor(getAndAdd(progress, progressStep)))
                        .hoverEvent(component.hoverEvent())
                        .clickEvent(component.clickEvent())
                        .decorations(component.decorations());
                out.add(sub);
            }
        }
        for (Component child : component.children()) {
            recursiveApply(child, progressStep, progress, out);
        }
    }

    private float getAndAdd(AtomicReference<Float> ref, float inc) {
        float current;
        while (!ref.compareAndSet(current = ref.get(), current + inc));
        return current;
    }
}
