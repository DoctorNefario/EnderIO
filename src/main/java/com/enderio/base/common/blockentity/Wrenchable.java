package com.enderio.base.common.blockentity;

import com.enderio.api.UseOnly;
import net.minecraft.core.Direction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

// TODO: Move to API.

/**
 * An interface that block entities may implement in order to implement special behaviours(other than to rotate the block) when right-clicked with the Yeta wrench.
 */
public interface Wrenchable {
    @UseOnly(LogicalSide.SERVER)
    ItemInteractionResult onWrenched(@Nullable Player player, @Nullable Direction side);
}
