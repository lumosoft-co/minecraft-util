package com.agoramp.minecraft.util.data.packets;

import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.UUID;

@RequiredArgsConstructor
public class MethodHandler implements Handler {
    private final int weight;
    @Getter
    private final Method method;

    @Setter
    private Object instance;

    public <T extends ModelledPacket> T handle(UUID player, T packet) {
        if (player == null) return packet;
        Class<T> type = (Class<T>) packet.getClass();
        Object[] params = new Object[method.getParameterCount()];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType.isAssignableFrom(packet.getClass())) {
                params[i] = packet;
            } else if (parameterType.isAssignableFrom(player.getClass())) {
                params[i] = player;
            }
        }
        try {
            Object out = method.invoke(instance, params);
            if (type.isInstance(out)) return type.cast(out);
            if (method.getReturnType() == Void.class) return packet;
            if (out == null) return null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return packet;
    }

    @Override
    public int weight() {
        return weight;
    }
}
