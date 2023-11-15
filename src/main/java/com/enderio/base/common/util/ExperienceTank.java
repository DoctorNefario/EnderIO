package com.enderio.base.common.util;

import com.enderio.base.common.init.EIOFluids;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.base.common.util.ExperienceUtil.SimpleXpFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class ExperienceTank extends FluidTank {
    private static final String AMOUNT_KEY = "Amount";
    private SimpleXpFluid simpleXpFluid;

    public ExperienceTank() {
        super(Integer.MAX_VALUE);
        simpleXpFluid = SimpleXpFluid.fromXp(0);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putLong(AMOUNT_KEY, simpleXpFluid.millibuckets());
        return nbt;
    }

    @Override
    public FluidTank readFromNBT(CompoundTag nbt) {
        // For migrating legacy obelisks
        // Remove this after everyone's updated
        if (nbt.contains(AMOUNT_KEY, Tag.TAG_INT)) {
            simpleXpFluid = new SimpleXpFluid(nbt.getInt(AMOUNT_KEY));
            return this;
        }

        if (!nbt.contains(AMOUNT_KEY, Tag.TAG_LONG)) {
            simpleXpFluid = new SimpleXpFluid(0);
            return this;
        }

        simpleXpFluid = new SimpleXpFluid(nbt.getLong(AMOUNT_KEY));
        return this;
    }

    public SimpleXpFluid getXpFluid() {
        return simpleXpFluid;
    }

    public void setMbLong(long mb) {
        simpleXpFluid = new SimpleXpFluid(mb);
    }

    public void runOnXpFluid(Function<@NotNull SimpleXpFluid, @NotNull SimpleXpFluid> fn) {
        simpleXpFluid = Objects.requireNonNull(fn.apply(simpleXpFluid));
        onContentsChanged();
    }

    @Override
    public FluidStack getFluid() {
        return new FluidStack(EIOFluids.XP_JUICE.getSource(), simpleXpFluid.mbInt());
    }

    /**
     * @return Current amount of fluid in the tank.
     */
    @Override
    public int getFluidAmount() {
        return simpleXpFluid.mbInt();
    }

    /**
     * @return Capacity of this fluid tank.
     */
    @Override
    public int getCapacity() {
        return ExperienceUtil.MAX_TANK_MB_INT;
    }

    /**
     * @param stack Fluidstack holding the Fluid to be queried.
     * @return If the tank can hold the fluid (EVER, not at the time of query).
     */
    @Override
    public boolean isFluidValid(FluidStack stack) {
        return stack.getFluid().is(EIOTags.Fluids.EXPERIENCE);
    }

    /**
     * @param resource FluidStack attempting to fill the tank.
     * @param action   If SIMULATE, the fill will only be simulated.
     * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
     */
    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        SimpleXpFluid oldAmount = simpleXpFluid;
        SimpleXpFluid newAmount = simpleXpFluid.saturatingAdd(resource.getAmount());
        int amount = (int) (newAmount.millibuckets() - oldAmount.millibuckets());

        if (action.execute()) {
            simpleXpFluid = newAmount;
        }

        return amount;
    }

    /**
     * @param maxDrain Maximum amount of fluid to be removed from the container.
     * @param action   If SIMULATE, the drain will only be simulated.
     * @return Amount of fluid that was removed (or would be, if simulated) from the tank.
     */
    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        SimpleXpFluid oldAmount = simpleXpFluid;
        SimpleXpFluid newAmount = simpleXpFluid.saturatingSub(maxDrain);
        int amount = (int) (oldAmount.millibuckets() - newAmount.millibuckets());

        if (action.execute()) {
            simpleXpFluid = newAmount;
        }

        return new FluidStack(EIOFluids.XP_JUICE.getSource(), amount);
    }

    /**
     * @param resource Maximum amount of fluid to be removed from the container.
     * @param action   If SIMULATE, the drain will only be simulated.
     * @return FluidStack representing fluid that was removed (or would be, if simulated) from the tank.
     */
    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!resource.getFluid().is(EIOTags.Fluids.EXPERIENCE)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }
}
