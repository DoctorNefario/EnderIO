package com.enderio.base.common.init;

import com.enderio.EnderIO;
import com.enderio.api.capability.IDarkSteelUpgradable;
import com.enderio.api.filter.ItemStackFilter;
import com.enderio.api.filter.ResourceFilter;
import com.enderio.core.common.capability.IFilterCapability;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import java.util.function.Predicate;

public class EIOCapabilities {

    public static final class DarkSteelUpgradable {
        public static final ItemCapability<IDarkSteelUpgradable, Void> ITEM =
            ItemCapability.createVoid(
                EnderIO.loc("dark_steel_upgradable"),
                IDarkSteelUpgradable.class);
    }

    public static final class SideConfig {
        public static final BlockCapability<com.enderio.api.capability.SideConfig, Direction> BLOCK =
            BlockCapability.createSided(
                EnderIO.loc("side_config"),
                com.enderio.api.capability.SideConfig.class);
    }

    public static final class Filter {
        public static final ItemCapability<ResourceFilter, Void> ITEM =
            ItemCapability.createVoid(
                EnderIO.loc("item_filter"),
                ResourceFilter.class);
    }
}
