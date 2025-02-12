package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.api.conduit.screen.ConduitScreenExtension;
import com.enderio.api.conduit.ConduitMenuData;
import com.enderio.conduits.common.conduit.ConduitGraphObject;
import com.enderio.api.misc.RedstoneControl;
import com.enderio.conduits.common.tag.ConduitTags;
import com.enderio.conduits.common.conduit.type.SimpleConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EnergyConduitType extends SimpleConduitType<EnergyConduitData> {
    private static final ConduitMenuData MENU_DATA = new ConduitMenuData.Simple(false, false, false, false, false, true);

    public EnergyConduitType() {
        super(new EnergyConduitTicker(), EnergyConduitData::new, MENU_DATA);
    }

    @Override
    public ConduitConnectionData getDefaultConnection(Level level, BlockPos pos, Direction direction) {
        IEnergyStorage capability = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());
        if (capability != null) {
            return new ConduitConnectionData(capability.canReceive(), capability.canExtract(), RedstoneControl.ALWAYS_ACTIVE);
        }

        return super.getDefaultConnection(level, pos, direction);
    }

    @Override
    public <K> Optional<K> proxyCapability(BlockCapability<K, Direction> cap, EnergyConduitData extendedConduitData, Level level, BlockPos pos, @Nullable Direction direction, @Nullable ConduitGraphObject.IOState state) {
        if (Capabilities.EnergyStorage.BLOCK == cap
            && (state == null || state.isExtract())
            && (direction == null || !level.getBlockState(pos.relative(direction)).is(ConduitTags.Blocks.ENERGY_CABLE))) {
                return (Optional<K>) Optional.of(extendedConduitData.getSelfCap());

        }
        return Optional.empty();
    }

}
