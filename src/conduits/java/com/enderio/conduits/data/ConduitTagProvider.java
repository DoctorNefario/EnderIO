package com.enderio.conduits.data;

import com.enderio.EnderIO;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.tag.ConduitTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ConduitTagProvider extends BlockTagsProvider {
    public ConduitTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
        @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, EnderIO.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(ConduitTags.Blocks.REDSTONE_CONNECTABLE)
            .add(Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.REDSTONE_LAMP, Blocks.NOTE_BLOCK, Blocks.DISPENSER, Blocks.DROPPER, Blocks.POWERED_RAIL, Blocks.ACTIVATOR_RAIL, Blocks.MOVING_PISTON)
            .addTags(BlockTags.DOORS, BlockTags.TRAPDOORS, BlockTags.REDSTONE_ORES);
        tag(ConduitTags.Blocks.ENERGY_CABLE)
            .addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "basic_universal_cable"))
            .addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "advanced_universal_cable"))
            .addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "elite_universal_cable"))
            .addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_universal_cable"))
            .addOptional(ResourceLocation.fromNamespaceAndPath("pipez", "energy_pipe"))
            .addOptional(ResourceLocation.fromNamespaceAndPath("pipez", "universal_pipe"));

        tag(ConduitTags.Blocks.RELOCATION_NOT_SUPPORTED).add(ConduitBlocks.CONDUIT.get());
    }
}
