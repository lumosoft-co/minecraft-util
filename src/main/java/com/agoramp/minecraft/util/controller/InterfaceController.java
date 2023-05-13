package com.agoramp.minecraft.util.controller;

import com.agoramp.minecraft.util.MinecraftUtil;
import com.agoramp.minecraft.util.api.annotations.Service;
import com.agoramp.minecraft.util.api.annotations.PacketHandler;
import com.agoramp.minecraft.util.api.ui.InterfaceClickListener;
import com.agoramp.minecraft.util.api.ui.UserInterface;
import com.agoramp.minecraft.util.data.packets.models.ClickWindowPacket;
import com.agoramp.minecraft.util.data.packets.models.CloseWindowPacket;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public enum InterfaceController implements GameController {
    INSTANCE;

    public final Map<UUID, UserInterface<?>> opened = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void load() {
    }

    @PacketHandler
    public ClickWindowPacket onPlayerClickWindow(UUID player, ClickWindowPacket packet) {
        UserInterface<?> open = opened.get(player);
        if (open == null) return packet;
        if (packet.getContainerId() != open.getWindowId() && packet.getContainerId() != 0) return packet; // i guess they got out
        MinecraftUtil.PLATFORM.schedule(() -> {
            int contentId = open.getContentId();
            boolean cancelled = false;
            if (packet.getContainerId() == open.getWindowId() && packet.getSlotNum() < open.getSize()) {
                InterfaceClickListener listener = open.getSlotListeners().get(packet.getSlotNum());
                if (listener != null) cancelled |= listener.onClick(packet);
                listener = open.getSlotListeners().get(Integer.MIN_VALUE);
                if (listener != null) cancelled |= listener.onClick(packet);
            } else {
                InterfaceClickListener listener = open.getPlayerInventoryListener();
                int accurateSlotId = packet.getSlotNum() - open.getSize();
                if (accurateSlotId > 27) accurateSlotId -= 27;
                else accurateSlotId += 9;
                if (accurateSlotId == open.getSize()) accurateSlotId = 0;
                ClickWindowPacket next = new ClickWindowPacket(packet.getContainerId(), accurateSlotId, packet.getButtonNum(), packet.getActionNum(), packet.getItem(), packet.getClickType());
                if (listener != null) cancelled |= listener.onClick(next);
            }
            if (cancelled && open.getContentId() == contentId) open.sendContents();
        });
        return null;
    }

    @PacketHandler
    public CloseWindowPacket onCloseWindow(CloseWindowPacket packet, UUID player) {
        UserInterface<?> open = opened.get(player);
        if (open == null) return packet;
        if (!open.onClose()) {
            open.open(player, false);
        } else {
            opened.remove(player);
        }
        return null;
    }
}
