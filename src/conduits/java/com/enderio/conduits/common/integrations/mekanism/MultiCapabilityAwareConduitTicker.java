package com.enderio.conduits.common.integrations.mekanism;

import com.enderio.api.conduit.ColoredRedstoneProvider;
import com.enderio.api.conduit.ConduitData;
import com.enderio.api.conduit.ConduitType;
import com.enderio.api.conduit.ConduitGraph;
import com.enderio.api.conduit.ticker.IOAwareConduitTicker;
import com.enderio.api.misc.ColorControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiCapabilityAwareConduitTicker<TType extends ConduitData<TType>, TCap> implements IOAwareConduitTicker<TType> {

    private final BlockCapability<? extends TCap, Direction>[] capabilities;

    public MultiCapabilityAwareConduitTicker(BlockCapability<? extends TCap, Direction>[] capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public boolean canConnectTo(Level level, BlockPos conduitPos, Direction direction) {
        for (BlockCapability<? extends TCap, Direction> cap : capabilities) {
            TCap capability = level.getCapability(cap, conduitPos.relative(direction), direction.getOpposite());
            if (capability != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tickColoredGraph(
        ServerLevel level,
        ConduitType<TType> type,
        List<Connection<TType>> inserts,
        List<Connection<TType>> extracts,
        ColorControl color,
        ConduitGraph<TType> graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        List<CapabilityConnection<TType, TCap>> insertCaps = new ArrayList<>();
        for (Connection<TType> insert : inserts) {
            for (BlockCapability<? extends TCap, Direction> cap : capabilities) {
                TCap capability = level.getCapability(cap, insert.move(), insert.dir().getOpposite());
                if (capability != null) {
                    insertCaps.add(new CapabilityConnection<>(capability, insert.data(), insert.dir()));
                }
            }

        }
        if (!insertCaps.isEmpty()) {
            List<CapabilityConnection<TType, TCap>> extractCaps = new ArrayList<>();

            for (Connection<TType> extract : extracts) {
                for (BlockCapability<? extends TCap, Direction> cap : capabilities) {
                    TCap capability = level.getCapability(cap, extract.move(), extract.dir().getOpposite());
                    if (capability != null) {
                        extractCaps.add(new CapabilityConnection<>(capability, extract.data(), extract.dir()));
                    }
                }
            }
            if (!extractCaps.isEmpty()) {
                tickCapabilityGraph(type, insertCaps, extractCaps, level, graph, coloredRedstoneProvider);
            }
        }
    }

    protected abstract void tickCapabilityGraph(
        ConduitType<TType> type,
        List<CapabilityConnection<TType, TCap>> insertCaps,
        List<CapabilityConnection<TType, TCap>> extractCaps,
        ServerLevel level,
        ConduitGraph<TType> graph,
        ColoredRedstoneProvider coloredRedstoneProvider);

    public record CapabilityConnection<TType extends ConduitData<TType>, TCap>(
        TCap capability,
        TType data,
        Direction direction) {
    }
}
