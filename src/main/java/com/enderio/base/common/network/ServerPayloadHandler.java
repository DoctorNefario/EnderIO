package com.enderio.base.common.network;

import com.enderio.api.travel.TravelTarget;
import com.enderio.api.travel.TravelTargetApi;
import com.enderio.base.common.handler.TravelHandler;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.core.common.capability.IFilterCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public class ServerPayloadHandler {
    private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

    public static ServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleCoordinateSelectionName(UpdateCoordinateSelectionNameMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                packet.getMenu(context).updateName(packet.name(), serverPlayer);
            }
        });
    }

    public void handleTravelRequest(RequestTravelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();

            Optional<TravelTarget> target = TravelTargetApi.INSTANCE.get(player.level(), packet.pos());

            // These errors should only ever be triggered if there's some form of desync
            if (!TravelHandler.canBlockTeleport(player)) {
                player.displayClientMessage(Component.nullToEmpty("ERROR: Cannot teleport"), true);
                return;
            }
            if (target.isEmpty()) {
                player.displayClientMessage(Component.nullToEmpty("ERROR: Destination not a valid target"), true);
                return;
            }
            // Eventually change the packet structure to include what teleport method was used so this range can be selected correctly
            int range = Math.max(target.get().block2BlockRange(), target.get().item2BlockRange());
            if (packet.pos().distSqr(player.getOnPos()) > range * range) {
                player.displayClientMessage(Component.nullToEmpty("ERROR: Too far"), true);
                return;
            }

            TravelHandler.blockTeleportTo(player.level(), player, target.get(), false);
        });
    }

    public void handleFilterUpdate(FilterUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var resourceFilter = context.player().getMainHandItem().getCapability(EIOCapabilities.Filter.ITEM);
            if (resourceFilter instanceof IFilterCapability<?> capability) {
                capability.setNbt(packet.nbt());
                capability.setInverted(packet.inverted());
            }
        });
    }
}
