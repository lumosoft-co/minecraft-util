package com.agoramp.minecraft.util.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;
import java.util.regex.Pattern;

public class MiniMessageUtil {

    public static List<Component> split(Component initial, char split) {
        Stack<Component> waiting = new Stack<>();
        waiting.add(initial);

        LinkedList<Component> components = new LinkedList<>();
        LinkedList<Component> last = null;
        while (!waiting.isEmpty()) {
            Component component = waiting.pop();
            Style style = component.style();
            LinkedList<Component> subdivisions = new LinkedList<>();
            if (component instanceof TextComponent) {
                String content = ((TextComponent) component).content();
                List<String> splits = new LinkedList<>();
                for (int i = 0; i < content.length(); i++) {
                    if (content.charAt(i) == split) {
                        splits.add(content.substring(0, i));
                        if (i >= content.length() - 1) content = "";
                        else content = content.substring(i + 1);
                        i = -1;
                    }
                }
                if (splits.isEmpty()) {
                    subdivisions.add(component.children(Collections.emptyList()));
                } else {
                    splits.add(content);
                    for (String text : splits) {
                        subdivisions.add(Component.text(text).style(style));
                    }
                }
            } else {
                subdivisions.add(component.children(Collections.emptyList()));
            }
            if (last != null) {
                Component first = subdivisions.removeFirst();
                last.add(first);
            }
            if (!subdivisions.isEmpty()) {
                if (last != null) components.add(Component.empty().children(last));
                last = new LinkedList<>();
                last.add(subdivisions.removeLast());
            }
            components.addAll(subdivisions);
            List<Component> children = component.children();
            if (children.isEmpty()) continue;
            ListIterator<Component> iter = children.listIterator(children.size());

            Map<TextDecoration, TextDecoration.State> decorations = new HashMap<>(style.decorations());
            decorations.values().removeIf(s -> s == TextDecoration.State.NOT_SET);
            TextColor color = style.color();
            while (iter.hasPrevious()) {
                Component child = iter.previous();
                waiting.push(child.style(child.style().colorIfAbsent(color).decorations(decorations)));
            }
        }
        if (last != null) components.add(Component.empty().children(last));

        return components;
    }

    public static String convertFromMinimessage(String text) {
        StringBuilder builder = new StringBuilder(text);
        int nextStart = -1;
        while ((nextStart = builder.indexOf("<", nextStart + 1)) != -1 && nextStart + 1 < builder.indexOf(">")) {
            int end = builder.indexOf(">");
            String style = builder.substring(nextStart + 1, end).replace("underlined", "underline");
            if (style.startsWith("/")) {
                builder.replace(nextStart, end, "&r");
            } else {
                if (style.startsWith("#")) {
                    builder.replace(nextStart, end, "&" + style);
                } else {
                    try {
                        try {
                            // TODO
                            //ChatColor color = ChatColor.valueOf(style.toUpperCase());
                            //builder.replace(nextStart, end, "&" + color.toString().substring(1));
                        } catch (Throwable ignored) {}
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return builder.toString();
    }
}
