package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.api.conduit.ConduitDataSerializer;
import com.enderio.api.conduit.ConduitData;
import com.enderio.api.misc.ColorControl;
import com.enderio.conduits.common.init.EIOConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import io.netty.buffer.ByteBuf;
import me.liliandev.ensure.ensures.EnsureSide;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RedstoneConduitData implements ConduitData<RedstoneConduitData> {

    private boolean isActive = false;
    private final EnumMap<ColorControl, Integer> activeColors = new EnumMap<>(ColorControl.class);

    public RedstoneConduitData() {
    }

    private RedstoneConduitData(boolean isActive, Map<ColorControl, Integer> activeColors) {
        this.isActive = isActive;
        this.activeColors.putAll(activeColors);
    }

    @Override
    public void applyClientChanges(RedstoneConduitData guiData) {
    }

    @Override
    public ConduitDataSerializer<RedstoneConduitData> serializer() {
        return EIOConduitTypes.Serializers.REDSTONE.get();
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isActive(ColorControl color) {
        return activeColors.containsKey(color);
    }

    public int getSignal(ColorControl color) {
        return activeColors.getOrDefault(color, 0);
    }

    public void clearActive() {
        activeColors.clear();
        isActive = false;
    }

    public void setActiveColor(ColorControl color, int signal) {
        if (activeColors.containsKey(color)) {
            return;
        }

        isActive = true;
        activeColors.put(color, signal);
    }

    @EnsureSide(EnsureSide.Side.CLIENT)
    @Override
    public RedstoneConduitData deepCopy() {
        return new RedstoneConduitData(isActive, new EnumMap<>(activeColors));
    }

    @Override
    public int hashCode() {
        return Objects.hash(isActive, activeColors);
    }

    public static class Serializer implements ConduitDataSerializer<RedstoneConduitData> {
        public static MapCodec<RedstoneConduitData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Codec.BOOL.fieldOf("is_active").forGetter(i -> i.isActive),
                Codec.unboundedMap(ColorControl.CODEC, Codec.INT).fieldOf("active_colors").forGetter(i -> i.activeColors)
            ).apply(instance, RedstoneConduitData::new)
        );

        public static StreamCodec<ByteBuf, RedstoneConduitData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            r -> r.isActive,
            ByteBufCodecs.map(HashMap::new, ColorControl.STREAM_CODEC, ByteBufCodecs.INT),
            r -> r.activeColors,
            RedstoneConduitData::new
        );

        @Override
        public MapCodec<RedstoneConduitData> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RedstoneConduitData> streamCodec() {
            return STREAM_CODEC.cast();
        }
    }
}
