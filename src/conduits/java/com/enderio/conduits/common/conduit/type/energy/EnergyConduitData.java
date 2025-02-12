package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.api.conduit.ConduitDataSerializer;
import com.enderio.api.conduit.ConduitType;
import com.enderio.api.conduit.ConduitData;
import com.enderio.api.network.DumbStreamCodec;
import com.enderio.conduits.common.init.EIOConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.HashMap;
import java.util.Map;

public class EnergyConduitData implements ConduitData<EnergyConduitData> {

    private final Map<Direction, EnergySidedData> energySidedData;

    private int capacity = 500;
    private int stored = 0;

    public EnergyConduitData() {
        this.energySidedData = new HashMap<>();
    }

    private EnergyConduitData(Map<Direction, EnergySidedData> energySidedData, int capacity, int stored) {
        this.energySidedData = new HashMap<>(energySidedData);
        this.capacity = capacity;
        this.stored = stored;
    }

    private IEnergyStorage selfCap = new EnergyConduitData.ConduitEnergyStorage(this);

    @Override
    public void applyClientChanges(EnergyConduitData guiData) {
    }

    @Override
    public ConduitDataSerializer<EnergyConduitData> serializer() {
        return EIOConduitTypes.Serializers.ENERGY.get();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getStored() {
        return stored;
    }

    public void setStored(int stored) {
        this.stored = stored;
    }

    @Override
    public void onRemoved(ConduitType<EnergyConduitData> type, Level level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    public EnergySidedData compute(Direction direction) {
        return energySidedData.computeIfAbsent(direction, dir -> new EnergySidedData(0));
    }

    IEnergyStorage getSelfCap() {
        if (selfCap == null) {
            selfCap = new EnergyConduitData.ConduitEnergyStorage(this);
        }

        return selfCap;
    }

    public static class EnergySidedData {

        public static Codec<EnergySidedData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.INT.fieldOf("rotating_index").forGetter(i -> i.rotatingIndex)
            ).apply(instance, EnergySidedData::new)
        );

        public int rotatingIndex;

        public EnergySidedData(int rotatingIndex) {
            this.rotatingIndex = rotatingIndex;
        }
    }

    private record ConduitEnergyStorage(EnergyConduitData data) implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int receivable = Math.min(data.getCapacity() - data().getStored(), maxReceive);
            if (!simulate) {
                data.setStored(data.getStored()+receivable);
            }
            return receivable;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extractable = Math.min(data().getStored(), maxExtract);
            if (!simulate) {
                data.setStored(data.getStored() - extractable);
            }
            return extractable;
        }

        @Override
        public int getEnergyStored() {
            return data.getStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return data().getCapacity();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    public static class Serializer implements ConduitDataSerializer<EnergyConduitData> {

        public static MapCodec<EnergyConduitData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Codec.unboundedMap(Direction.CODEC, EnergySidedData.CODEC)
                    .fieldOf("energy_sided_data")
                    .forGetter(e -> e.energySidedData),
                Codec.INT.fieldOf("capacity").forGetter(EnergyConduitData::getCapacity),
                Codec.INT.fieldOf("stored").forGetter(EnergyConduitData::getStored)
            ).apply(instance, EnergyConduitData::new)
        );

        public static final StreamCodec<ByteBuf, EnergyConduitData> STREAM_CODEC = DumbStreamCodec.of(EnergyConduitData::new);

        @Override
        public MapCodec<EnergyConduitData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EnergyConduitData> streamCodec() {
            return STREAM_CODEC.cast();
        }
    }
}
