package com.enderio.base.common.item.tool;

import com.enderio.base.common.init.EIOFluids;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.base.common.util.ExperienceUtil;
import com.enderio.base.common.util.ExperienceUtil.SimpleXpFluid;
import com.enderio.core.common.network.CoreNetwork;
import com.enderio.core.common.network.EmitParticlePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ExperienceRodItem extends Item {
    public ExperienceRodItem(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        boolean wasSuccess;
        if (player.isShiftKeyDown()) {
            wasSuccess = transferFromPlayerToBlock(player, level, pos);
        } else {
            wasSuccess = transferFromBlockToPlayer(player, level, pos);
        }

        if (wasSuccess) {
            CoreNetwork.sendToTracking(level.getChunkAt(pos), new EmitParticlePacket(ParticleTypes.ENTITY_EFFECT, pos, 0.2, 0.8, 0.2));
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1f,
                0.5F * ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.8F));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }

    private static boolean transferFromBlockToPlayer(Player player, Level level, BlockPos pos) {
        if (ExperienceUtil.getMbNeededForNextLevel(player.experienceLevel) > Integer.MAX_VALUE) {
            // should be level 11,930,483
            // would require too much XP to level up from here
            return false;
        }

        SimpleXpFluid playerXp = SimpleXpFluid.fromPlayer(player);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }

        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).map(fluidHandler -> {
            FluidStack availableFluid = fluidHandler.getFluidInTank(0);
            if (availableFluid.getFluid().is(EIOTags.Fluids.EXPERIENCE) && availableFluid.getAmount() > 0) {
                SimpleXpFluid requestedXp = SimpleXpFluid.fromLevel(playerXp.level() + 1).saturatingSub(playerXp.millibuckets());
                FluidStack drained = fluidHandler.drain(requestedXp.mbInt(), IFluidHandler.FluidAction.EXECUTE);

                if (!drained.isEmpty()) {
                    new SimpleXpFluid(drained.getAmount()).addToPlayer(player);
                    return true;
                }
            }

            return false;
        }).orElse(false);
    }

    private static boolean transferFromPlayerToBlock(Player player, Level level, BlockPos pos) {
        SimpleXpFluid playerXp = SimpleXpFluid.fromPlayer(player);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }

        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).map(fluidHandler -> {
            FluidStack fs = new FluidStack(EIOFluids.XP_JUICE.getSource(), playerXp.mbInt());
            int takenVolume = fluidHandler.fill(fs, IFluidHandler.FluidAction.EXECUTE);
            if (takenVolume > 0) {
                new SimpleXpFluid(takenVolume).subtractFromPlayer(player);
                return true;
            }

            return false;
        }).orElse(false);
    }
}
