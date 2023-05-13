package com.agoramp.minecraft.util.api.ui;

import com.agoramp.minecraft.util.MinecraftUtil;
import com.agoramp.minecraft.util.controller.InterfaceController;
import com.agoramp.minecraft.util.data.packets.models.ClickWindowPacket;
import com.agoramp.minecraft.util.data.packets.models.ModelledPacket;
import com.agoramp.minecraft.util.data.packets.models.CloseWindowPacket;
import com.agoramp.minecraft.util.data.packets.models.WindowItemsPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.ComponentLike;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class UserInterface<ItemStack> {
    @Getter
    private final int size;
    private final Class<ItemStack> itemType;
    @Getter
    private ComponentLike title;

    private ItemStack[] inventory;
    private Object[][] metadata;
    public ItemStack cursorItem;

    @Getter
    private int windowId = -1, contentId = 0;

    public UUID player;

    @Getter
    private final Map<Integer, InterfaceClickListener> slotListeners = new HashMap<>();

    @Setter @Getter
    private InterfaceClickListener playerInventoryListener;


    public UserInterface(int size, ComponentLike title) {
        this.size = size;
        this.title = title;
        Class<?> type = getClass();
        if (type.getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type.getGenericSuperclass();
            itemType = (Class<ItemStack>) p.getActualTypeArguments()[0];
        } else {
            itemType = (Class<ItemStack>) Object.class;
        }
    }

    public CompletionStage<Void> doDataPreload() {
        return CompletableFuture.completedFuture(null);
    }

    public void open(UUID player, boolean render) {
        this.player = player;
        try {
            doDataPreload().thenAccept(v -> {
                UserInterface<?> open = InterfaceController.INSTANCE.opened.get(player);
                if (open != null) open.close();
                try {
                    if (render) {
                        render();
                    }
                    openInventory(this.player);
                } catch (Throwable t) {
                    //player.sendMessage(new StringTextComponent("An error occurred while opening the UI"), new UUID(0,0));
                    t.printStackTrace();
                }
            });
        }catch(Exception e) {
            //player.sendMessage(new StringTextComponent("An error occurred while opening the UI"), new UUID(0,0));
            e.printStackTrace();
        }
    }

    public void render() {
        // Load items onto a fresh temporary inventory
        inventory = (ItemStack[]) Array.newInstance(itemType, size);
        metadata = new Object[size][];
        loadItems();

        // Reset slot listeners...
        slotListeners.clear();
        playerInventoryListener = null;
        loadListeners();

        contentId++;
        sendContents();
    }

    // Updates the contents of the User Interface (Doesn't close it)
    protected abstract void loadItems();

    protected void loadListeners() {

    }

    protected void setItem(int position, ItemStack stack, Object... metadata) {
        inventory[position] = stack;
        this.metadata[position] = metadata;
    }

    protected void addItem(ItemStack stack, Object... metadata) {
        for (int i = 0; i < size; i++) {
            if (inventory[i] == null) {
                inventory[i] = stack;
                this.metadata[i] = metadata;
                return;
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean close() {
        if (InterfaceController.INSTANCE.opened.containsKey(player) && !onClose()) {
            return false;
        }
        MinecraftUtil.PLATFORM.sendPacket(player, new CloseWindowPacket(windowId));
        InterfaceController.INSTANCE.opened.remove(player);
        return true;
    }

    //will be called upon inventory close event
    public boolean onClose() {
        return true;
    }

    public void unregister() {
        InterfaceController.INSTANCE.opened.remove(player);
    }

    public void rename(ComponentLike newName, boolean immediate) {
        this.title = newName;
        windowId = MinecraftUtil.PLATFORM.openInventory(player, size, getTitle());
        sendContents();
    }

    public void addSlotListener(int slot, Runnable listener) {
        addSlotListener(slot, v -> {
            listener.run();
            return false;
        });
    }

    public void addSlotListener(int slot, Consumer<ClickWindowPacket> listener) {
        addSlotListener(slot, v -> {
            listener.accept(v);
            return false;
        });
    }

    public void addSlotListener(int slot, InterfaceClickListener listener) {
        slotListeners.put(slot, listener);
    }

    @SneakyThrows
    private List<ModelledPacket> getContentUpdatePackets() {
        List<ModelledPacket> list = new ArrayList<>();
        if (windowId == -1) return list;
        //list.add(new SetSlotPacket())
        list.add(new WindowItemsPacket<>(windowId, inventory, metadata));
        return list;
    }

    public void sendContents() {
        if (windowId == -1) return;
        getContentUpdatePackets().forEach(p -> MinecraftUtil.PLATFORM.sendPacket(player, p));
    }

    private void openInventory(UUID player) {
        windowId = MinecraftUtil.PLATFORM.openInventory(player, size, getTitle());
        sendContents();
        InterfaceController.INSTANCE.opened.put(player, this);
    }

    protected static int slot(int x, int y) {
        return y * 9 + x;
    }
}
