package com.enderio.machines.common.blockentity;

import com.enderio.base.common.util.ExperienceTank;
import com.enderio.core.common.network.slot.LongNetworkDataSlot;
import com.enderio.machines.common.blockentity.base.MachineBlockEntity;
import com.enderio.machines.common.menu.XPObeliskMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class XPObeliskBlockEntity extends MachineBlockEntity {

    LongNetworkDataSlot xpTankDataSlot;

    public XPObeliskBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);

        this.xpTankDataSlot = new LongNetworkDataSlot(() -> getXpTank().getXpFluid().millibuckets(), amount -> getXpTank().setMbLong(amount));
        addDataSlot(xpTankDataSlot);
    }

    private ExperienceTank getXpTank() {
        if (getFluidTankNN() instanceof ExperienceTank xpTank) {
            return xpTank;
        }
        throw new Error("Incorrect XPObelisk tank type");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new XPObeliskMenu(this, playerInventory, containerId);
    }

    @Override
    protected @Nullable FluidTank createFluidTank() {
        /*return new FluidTank(Integer.MAX_VALUE, fluidStack -> fluidStack.getFluid().is(EIOTags.Fluids.EXPERIENCE)) {
            @Override
            public int fill(FluidStack resource, FluidAction action) {
                // Convert into XP Juice
                if (this.isFluidValid(resource)) {
                    var currentFluid = this.getFluid().getFluid();
                    if (currentFluid == Fluids.EMPTY || resource.getFluid().isSame(currentFluid)) {
                        return super.fill(resource, action);
                    } else {
                        return super.fill(new FluidStack(currentFluid, resource.getAmount()), action);
                    }
                }

                // Non-XP is not allowed.
                return 0;
            }
        };*/
        return new ExperienceTank() {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                setChanged();
            }
        };
    }

    public void adjustPlayerLevel(Player player, int levelCount) {
        if (getFluidTankNN() instanceof ExperienceTank xpTank) {
            xpTank.runOnXpFluid(simpleXp -> simpleXp.useToAdjustPlayerLevel(player, levelCount));
        }
    }

}
