package com.agoramp.minecraft.util.api.text;

import com.agoramp.minecraft.util.api.text.transform.TranslationTransform;
import com.agoramp.minecraft.util.controller.TranslationController;
import lombok.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.*;

@Getter
public class Translation implements Translatable {
    public static Translation NEWLINE = Translation.create("newline");

    protected @NonNull String key;
    private final Map<String, Object> args;
    private final List<TranslationTransform> transforms = new ArrayList<>();

    public Translation(@NotNull String key, Map<String, Object> args) {
        //this.args = args instanceof HashMap ? args : new HashMap<>(args);
        //this.args.replaceAll((k, v) -> {
        //    if (v instanceof ITextComponent) {
        //        return ConversionUtil.convert((ITextComponent) v);
        //    }
        //    return v;
        //});
        this.args = args;
        this.key = key;
    }

    protected Translation() {
        key = "";
        args = Collections.emptyMap();
    }

    public static Translation create(@PropertyKey(resourceBundle = "translations") String key, Map<String, Object> params) {
        return new Translation(key, params);
    }

    public static Translation create(@PropertyKey(resourceBundle = "translations") String key, Object... params) {
        Map<String, Object> vals = new HashMap<>();
        for (int i = 0; i + 1 < params.length; i += 2) {
            vals.put(params[i].toString(), params[i + 1]);
        }
        return new Translation(key, vals);
    }

    public static Translation wrap(Object val) {
        Map<String, Object> vals = new HashMap<>();
        vals.put("val", val);
        return new Translation("wrap", vals);
    }

    public Component translate(Locale locale) {
        Component rendered = TranslationController.INSTANCE.translate(locale, key, args);
        for (TranslationTransform transform : transforms) {
            rendered = transform.apply(rendered, locale);
        }
        return rendered;
    }

    public Translation withTransform(TranslationTransform transform) {
        transforms.add(transform);
        return this;
    }

    @Override
    public String toString() {
        return toFormattedString();
    }

    public String toFormattedString() {
        return LegacyComponentSerializer.legacySection().serialize(asComponent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Translation that = (Translation) o;
        return key.equals(that.key) && (args.equals(that.args) && transforms.equals(that.transforms));
    }
}
