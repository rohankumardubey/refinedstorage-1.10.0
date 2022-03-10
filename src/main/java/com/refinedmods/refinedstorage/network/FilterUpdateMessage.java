package com.refinedmods.refinedstorage.network;

import com.refinedmods.refinedstorage.container.FilterContainerMenu;
import com.refinedmods.refinedstorage.item.FilterItem;
import com.refinedmods.refinedstorage.util.PacketBufferUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FilterUpdateMessage {
    private final int compare;
    private final int mode;
    private final boolean modFilter;
    private final String name;
    private final int type;

    public FilterUpdateMessage(int compare, int mode, boolean modFilter, String name, int type) {
        this.compare = compare;
        this.mode = mode;
        this.modFilter = modFilter;
        this.name = name;
        this.type = type;
    }

    public static FilterUpdateMessage decode(FriendlyByteBuf buf) {
        return new FilterUpdateMessage(
            buf.readInt(),
            buf.readInt(),
            buf.readBoolean(),
            PacketBufferUtils.readString(buf),
            buf.readInt()
        );
    }

    public static void encode(FilterUpdateMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.compare);
        buf.writeInt(message.mode);
        buf.writeBoolean(message.modFilter);
        buf.writeUtf(message.name);
        buf.writeInt(message.type);
    }

    public static void handle(FilterUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        Player player = ctx.get().getSender();

        if (player != null && player.containerMenu instanceof FilterContainerMenu) {
            ctx.get().enqueueWork(() -> {
                FilterItem.setCompare(((FilterContainerMenu) player.containerMenu).getFilterItem(), message.compare);
                FilterItem.setMode(((FilterContainerMenu) player.containerMenu).getFilterItem(), message.mode);
                FilterItem.setModFilter(((FilterContainerMenu) player.containerMenu).getFilterItem(), message.modFilter);
                FilterItem.setName(((FilterContainerMenu) player.containerMenu).getFilterItem(), message.name);
                FilterItem.setType(((FilterContainerMenu) player.containerMenu).getFilterItem(), message.type);
            });
        }

        ctx.get().setPacketHandled(true);
    }
}
