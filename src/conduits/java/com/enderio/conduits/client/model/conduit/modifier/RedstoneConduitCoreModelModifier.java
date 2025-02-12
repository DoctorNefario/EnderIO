package com.enderio.conduits.client.model.conduit.modifier;

import com.enderio.EnderIO;
import com.enderio.api.conduit.model.ConduitCoreModelModifier;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RedstoneConduitCoreModelModifier implements ConduitCoreModelModifier<RedstoneConduitData> {

    private static final ResourceLocation INACTIVE_TEXTURE = EnderIO.loc("block/conduit/redstone");
    private static final ResourceLocation ACTIVE_TEXTURE = EnderIO.loc("block/conduit/redstone_active");

    @Nullable
    @Override
    public ResourceLocation getSpriteLocation(RedstoneConduitData data) {
        if (data.isActive()) {
            return ACTIVE_TEXTURE;
        } else {
            return INACTIVE_TEXTURE;
        }
    }
}
