package com.agoramp.minecraft.util.controller;

import com.agoramp.minecraft.util.MinecraftUtil;
import com.agoramp.minecraft.util.api.annotations.Service;
import com.agoramp.minecraft.util.api.text.MiniMessageUtil;
import com.agoramp.minecraft.util.api.text.Translatable;
import com.agoramp.minecraft.util.api.text.Translation;
import com.agoramp.minecraft.util.api.text.color.Gradient;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public enum TranslationController implements GameController {
    INSTANCE;
    private static final Pattern LANG_PATTERN = Pattern.compile(".*assets/([a-zA-Z_-]+)/lang/(.+)\\.json");
    private static final Pattern PRE_VARIABLE_SCANNER = Pattern.compile("\\{([^?|]+)([?|]?)([^}]*)}");
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public static final LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .character('&')
            .build();

    public static final LegacyComponentSerializer URL_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .extractUrls()
            .character('&')
            .build();

    private final Map<Locale, Map<String, String>> bundles = new HashMap<>();

    @Override
    @SneakyThrows
    public void load() {
        // Register our translations
        ResourceBundle bundle = ResourceBundle.getBundle("translations", Locale.ENGLISH);
        Map<String, String> map = new HashMap<>();
        for (String s : bundle.keySet()) {
            String out = MiniMessageUtil.convertFromMinimessage(bundle.getString(s));
            map.put(s, out);
        }
        bundles.put(Locale.ENGLISH, map);
        // Register mod translations
        TranslationRegistry registry = TranslationRegistry.create(Key.key("mods"));
        File modsFolder = new File("./mods");
        Map<Locale, Map<String, MessageFormat>> translations = new HashMap<>();
        File[] children = modsFolder.listFiles();
        if (children != null) {
            for (File file : children) {
                if (file.getName().endsWith(".jar")) {
                    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                        ZipEntry zipEntry = zis.getNextEntry();
                        while (zipEntry != null) {
                            if (!zipEntry.isDirectory()) {
                                Matcher match = LANG_PATTERN.matcher(zipEntry.getName());
                                if (match.find()) {
                                    String namespace = match.group(1);
                                    String language = match.group(2);
                                    //auto locale = LocaleUtil.getLocale(language);
                                    //try {
                                    //    auto json = new JsonParser().parse(new InputStreamReader(zis, StandardCharsets.UTF_8));
                                    //    auto obj = json.getAsJsonObject();
                                    //    auto formats = new HashMap<String, MessageFormat>();
                                    //    for (auto entry : obj.entrySet()) {
                                    //        formats.put(entry.getKey(), MessageFormatUtil.format(entry.getValue().getAsString()));
                                    //    }
                                    //    translations.computeIfAbsent(locale, k -> new HashMap<>()).putAll(formats);
                                    //} catch (Throwable t) {
                                    //    System.out.println("Could not register translations from $namespace:$locale");
                                    //}
                                }
                            }
                            zipEntry = zis.getNextEntry();
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
        translations.forEach(((locale, formats) -> {
            if (locale == null) return;
            try {
                registry.registerAll(locale, formats);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }));
        GlobalTranslator.translator().addSource(registry);
    }

    public Component translate(Locale locale, @PropertyKey(resourceBundle = "translations") String key, Map<String, Object> translations) {
        Map<String, String> bundle = bundles.get(locale);
        if (bundle == null) bundle = bundles.get(DEFAULT_LOCALE);
        String s = bundle.getOrDefault(key, key);
        int offset = 0;
        StringBuilder translated = new StringBuilder(s);
        Matcher match = PRE_VARIABLE_SCANNER.matcher(s);
        while (match.find()) {
            String variable = match.group(1);
            String control = match.group(2);
            String content = match.group(3);
            String value;
            if (!control.isEmpty()) {
                if (control.charAt(0) == '?') {
                    String ifTrue;
                    String ifFalse = "";
                    int i = content.lastIndexOf(':');
                    if (i != -1) {
                        ifTrue = content.substring(0, i);
                        ifFalse = content.substring(i + 1);
                    } else {
                        ifTrue = content;
                    }
                    if (!checkArgument(variable, translations)) {
                        value = ifFalse;
                    } else {
                        value = ifTrue;
                    }
                } else if (control.charAt(0) == '|') {
                    if (!checkArgument(variable, translations)) {
                        value = content;
                    } else {
                        value = "<" + variable + ">";
                    }
                } else {
                    value = "<" + variable + ">";
                }
            } else {
                value = String.valueOf(translations.get(variable));
            }
            translated.replace(match.start() + offset, match.end() + offset, value);
            offset += value.length() - (match.end() - match.start());
        }
        String out = translated.toString();
        boolean mayHaveSpecial = out.contains("<gradient");
        Component formatted = format(out);
        if (!translations.isEmpty()) formatted = doReplacement(formatted, locale, translations);
        if (mayHaveSpecial) {
            formatted = doSpecialFormatting(formatted, null).getKey();
        }
        return formatted;
    }

    private boolean checkArgument(String argument, Map<String, Object> parameters) {
        if (argument.charAt(0) == '!') {
            return !checkArgument(argument.substring(1), parameters);
        }
        String condition = "";
        int start = -1;
        int end = -1;
        for (int i = 0; i < argument.length(); i++) {
            char c = argument.charAt(i);
            boolean special = new HashSet<>(Arrays.asList('<', '>', '!', '=')).contains(c);
            if (start == -1 && special) start = i;
            else if (start != -1 && (!special || i - start >= 2)) {
                condition = argument.substring(start, i);
                end = i;
                break;
            }
        }
        if (condition.isEmpty() && start != -1) condition = argument.substring(start);
        if (condition.isEmpty()) {
            Object replacement = parameters.get(argument);
            return replacement instanceof Boolean ? replacement == Boolean.TRUE : replacement != null;
        } else {
            String left = argument.substring(0, start).trim();
            String right = argument.substring(end).trim();
            Object leftVal, rightVal;
            leftVal = getValue(parameters, left);
            rightVal = getValue(parameters, right);
            if (leftVal == null) leftVal = BigDecimal.ZERO;
            if (rightVal == null) rightVal = BigDecimal.ZERO;

            int comparison;

            if (leftVal.getClass() == rightVal.getClass()) {
                if (leftVal instanceof Comparable) {
                    Class<? extends Comparable> type = leftVal.getClass().asSubclass(Comparable.class);
                    comparison = type.cast(leftVal).compareTo(type.cast(rightVal));
                } else {
                    return false;
                }
            } else {
                return false;
            }

            switch (condition) {
                case ">":
                    return comparison > 0;
                case "<":
                    return comparison < 0;
                case ">=":
                    return comparison >= 0;
                case "<=":
                    return comparison <= 0;
                case "==":
                    return comparison == 0;
                case "!=":
                    return comparison != 0;
            }
        }
        return false;
    }

    @Nullable
    private Object getValue(Map<String, Object> parameters, String input) {
        Object value;
        if (parameters.containsKey(input)) {
            value = parameters.get(input);
            if (value instanceof Number) {
                if (value instanceof Long) value = new BigDecimal((Long) value);
                else value = BigDecimal.valueOf(((Number) value).doubleValue());
            }
        } else {
            try {
                value = new BigDecimal(input);
            } catch (Throwable t) {
                value = input;
            }
        }
        return value;
    }

    private Component doReplacement(Component main, Locale locale, Map<String, Object> translations) {
        Component out = main;
        List<Component> children = new LinkedList<>();
        if (main instanceof TextComponent) {
            String s = ((TextComponent) main).content();
            int lastEnd = 0;
            int start = -1;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '<') {
                    start = i;
                } else if (c == '>' && start != -1 && start + 1 < i) {
                    String variable = s.substring(start + 1, i);
                    Object replacement = translations.get(variable);
                    if (replacement == null) {
                        start = -1;
                        continue;
                    }
                    if (lastEnd < start) {
                        children.add(Component.text(s.substring(lastEnd, start)));
                    }
                    Component parsed = parse(replacement, locale);
                    children.add(parsed);
                    lastEnd = i + 1;
                    start = -1;
                }
            }
            if (lastEnd != 0) {
                if (lastEnd < s.length()) {
                    children.add(Component.text(s.substring(lastEnd)));
                }
                out = Component.empty().style(main.style());
            }
        }
        for (Component child : main.children()) {
            children.add(doReplacement(child, locale, translations));
        }
        return out.children(children);
    }

    private Component applySpecialTag(Component component, String tag) {
        if (tag == null) return component;
        // Format from lastEnd to start
        if (tag.startsWith("gradient")) {
            List<TextColor> colors = new LinkedList<TextColor>();
            String[] args = tag.split(":");
            for (int j = 1; j < args.length; j++) {
                String arg = args[j];
                if (arg.startsWith("#")) colors.add(TextColor.fromHexString(arg));
                else colors.add(NamedTextColor.NAMES.value(arg.toLowerCase()));
            }
            colors.removeIf(Objects::isNull);
            return Gradient.of(colors.toArray(new TextColor[0])).apply(component);
        } else if (tag.startsWith("rainbow")) {
            return Gradient.of(
                    NamedTextColor.GOLD,
                    NamedTextColor.YELLOW,
                    NamedTextColor.GREEN,
                    NamedTextColor.BLUE,
                    NamedTextColor.DARK_PURPLE,
                    NamedTextColor.RED
            ).apply(component);
        }
        return component;
    }

    private Map.Entry<Component, String> doSpecialFormatting(Component main, String startTag) {
        Set<String> valid = new HashSet<>(Arrays.asList("gradient", "rainbow"));
        Component out = main;
        List<Component> children = new LinkedList<>();
        String finalTag = startTag;
        if (main instanceof TextComponent) {
            String s = ((TextComponent) main).content();
            int lastEnd = 0;
            int start = -1;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '<') {
                    start = i;
                } else if (c == '>' && start != -1 && start + 1 < i) {
                    String tag = s.substring(start + 1, i);
                    String key = tag.split(":")[0];
                    if (valid.contains(key)) {
                        if (finalTag != null) {
                            if (lastEnd < start) {
                                children.add(applySpecialTag(Component.text(s.substring(lastEnd, start)), finalTag));
                            }
                        }
                        finalTag = tag;
                    } else {
                        start = -1;
                        continue;
                    }
                    lastEnd = i + 1;
                    start = -1;
                }
            }
            if (finalTag != null) {
                if (lastEnd < s.length()) {
                    children.add(applySpecialTag(Component.text(s.substring(lastEnd)), finalTag));
                }
                out = Component.empty().style(main.style());
            }
        }
        for (Component child : main.children()) {
            Map.Entry<Component, String> result = doSpecialFormatting(child, finalTag);
            children.add(result.getKey());
            finalTag = result.getValue();
        }
        return new AbstractMap.SimpleImmutableEntry<>(out.children(children), finalTag);
    }

    public Component format(String text) {
        try {
            if (text.contains("http")) {
                return URL_COMPONENT_SERIALIZER.deserialize(text);
            } else {
                return COMPONENT_SERIALIZER.deserialize(text);
            }
            //return Component.empty().append(MINI_MESSAGE.deserialize(text, placeholders));
        } catch (Throwable t) {
            t.printStackTrace();
            return Component.text(text);
        }
    }

    public Component fullFormat(String text) {
        Component component;
        try {
            if (text.contains("http")) {
                component = URL_COMPONENT_SERIALIZER.deserialize(text);
            } else {
                component = COMPONENT_SERIALIZER.deserialize(text);
            }
            //return Component.empty().append(MINI_MESSAGE.deserialize(text, placeholders));
        } catch (Throwable t) {
            t.printStackTrace();
            component = Component.text(text);
        }
        return doSpecialFormatting(component, null).getKey();
    }

    public void sendMessage(UUID player, Translatable translation) {
        MinecraftUtil.PLATFORM.sendMessage(player, translation);
    }

    public Component parse(Object val, Locale locale) {
        Component value;
        if (val instanceof Translation)
            value = ((Translation) val).translate(locale);
        else if (val instanceof ComponentLike)
            value = ((ComponentLike) val).asComponent();
        else if (val instanceof Float || val instanceof Double)
            value = Component.text((((Number) (val)).doubleValue() * 100) / 100d);
        else
            value = MinecraftUtil.PLATFORM.parse(val, locale);
        if (value instanceof TranslatableComponent) value = GlobalTranslator.render(value, locale);
        return value;
    }
}
