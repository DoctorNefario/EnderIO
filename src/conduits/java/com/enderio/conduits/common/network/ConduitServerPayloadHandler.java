package com.enderio.conduits.common.network;

import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.conduits.common.conduit.block.ConduitBlockEntity;
import com.enderio.conduits.common.redstone.DoubleRedstoneChannel;
import com.enderio.conduits.common.redstone.RedstoneCountFilter;
import com.enderio.conduits.common.redstone.RedstoneTimerFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConduitServerPayloadHandler {
    private static final ConduitServerPayloadHandler INSTANCE = new ConduitServerPayloadHandler();

    public static ConduitServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleConduitConnectionState(final C2SSetConduitConnectionState packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof ConduitBlockEntity conduitBlockEntity) {
                conduitBlockEntity.handleConnectionStateUpdate(packet.direction(), packet.conduitType(), packet.connectionState());
            }
        });
    }

    public void handleConduitExtendedData(final C2SSetConduitExtendedData packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof ConduitBlockEntity conduitBlockEntity) {
                conduitBlockEntity.handleExtendedDataUpdate(packet.conduitType(), packet.extendedConduitData());
            }
        });
    }

    public void handleDoubleChannelFilter(DoubleChannelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack mainHandItem = context.player().getMainHandItem();
            var channels = mainHandItem.getCapability(EIOCapabilities.Filter.ITEM);
            if (channels instanceof DoubleRedstoneChannel doubleRedstoneChannel) {
                doubleRedstoneChannel.setChannels(packet.channel1(), packet.channel2());
            }
        });
    }

    public void handleTimerFilter(TimerFilterPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack mainHandItem = context.player().getMainHandItem();
            var channels = mainHandItem.getCapability(EIOCapabilities.Filter.ITEM);
            if (channels instanceof RedstoneTimerFilter timer) {
                timer.setTimer(packet.ticks(), packet.maxTicks());
            }
        });
    }

    public void handleCountFilter(CountFilterPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack mainHandItem = context.player().getMainHandItem();
            var channels = mainHandItem.getCapability(EIOCapabilities.Filter.ITEM);
            if (channels instanceof RedstoneCountFilter count) {
                count.setState(packet);
            }
        });
    }
}
