package com.enderio.core.common.network.slot;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanNetworkDataSlot extends NetworkDataSlot<Boolean> {

    public BooleanNetworkDataSlot(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(getter, setter);
    }

    @Override
    public Tag serializeValueNBT(HolderLookup.Provider lookupProvider, Boolean value) {
        return ByteTag.valueOf(value);
    }

    @Override
    protected Boolean valueFromNBT(HolderLookup.Provider lookupProvider, Tag nbt) {
        if (nbt instanceof ByteTag byteTag) {
            return byteTag.getAsByte() == 1;
        } else {
            throw new IllegalStateException("Invalid boolean tag was passed over the network.");
        }
    }

    @Override
    public void toBuffer(RegistryFriendlyByteBuf buf, Boolean value) {
        buf.writeBoolean(value);
    }

    @Override
    public Boolean valueFromBuffer(RegistryFriendlyByteBuf buf) {
        try {
            return buf.readBoolean();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid boolean buffer was passed over the network.");
        }
    }
}
