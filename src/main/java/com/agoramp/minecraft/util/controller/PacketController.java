package com.agoramp.minecraft.util.controller;

import com.agoramp.minecraft.util.api.annotations.Service;
import com.agoramp.minecraft.util.api.annotations.PacketHandler;
import com.agoramp.minecraft.util.data.packets.Handler;
import com.agoramp.minecraft.util.data.packets.MethodHandler;
import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public enum PacketController implements GameController {
    INSTANCE;

    private final Map<Class<? extends ModelledPacket>, SortedSet<Handler>> HANDLERS = new ConcurrentHashMap<>();

    public boolean handle(UUID player, ModelledPacket packet) {
        Class<?> type = packet.getClass();
        SortedSet<Handler> handles = HANDLERS.getOrDefault(type, Collections.emptySortedSet());
        for (Handler handle : handles) {
            packet = handle.handle(player, packet);
            if (packet == null) return true;
        }
        return false;
    }

    public void register(Object instance) {
        scan(instance.getClass()).stream()
                .filter(t -> !Modifier.isStatic(t.getKey().getMethod().getModifiers()))
                .peek(t -> t.getKey().setInstance(instance))
                .forEach(t -> insert(t.getKey(), t.getValue()));
    }

    private List<Map.Entry<MethodHandler, Class<? extends ModelledPacket>>> scan(Class<?> type) {
        ArrayList<Map.Entry<MethodHandler, Class<? extends ModelledPacket>>> out = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            PacketHandler annotation = method.getAnnotation(PacketHandler.class);
            if (annotation == null) continue;
            MethodHandler handler = new MethodHandler(annotation.weight(), method);
            Class<? extends ModelledPacket> packetType = null;
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (ModelledPacket.class.isAssignableFrom(parameterType)) {
                    packetType = parameterType.asSubclass(ModelledPacket.class);
                    break;
                }
            }
            if (packetType == null) continue;
            out.add(new AbstractMap.SimpleImmutableEntry<>(handler, packetType));
        }
        return out;
    }

    private void insert(Handler handler, Class<? extends ModelledPacket> packetType) {
        HANDLERS.computeIfAbsent(packetType, k -> new TreeSet<>(Comparator.comparingInt(Handler::weight))).add(handler);
    }
}
