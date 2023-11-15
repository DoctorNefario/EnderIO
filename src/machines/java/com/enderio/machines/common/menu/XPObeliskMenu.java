package com.enderio.machines.common.menu;

import com.enderio.base.common.util.ExperienceUtil;
import com.enderio.machines.common.blockentity.XPObeliskBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

public class XPObeliskMenu extends MachineMenu<XPObeliskBlockEntity> {

    public XPObeliskMenu(@Nullable XPObeliskBlockEntity blockEntity, Inventory inventory,  int pContainerId) {
        super(blockEntity, inventory, MachineMenus.XP_OBELISK.get(), pContainerId);
    }

    public static XPObeliskMenu factory(@Nullable MenuType<XPObeliskMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (entity instanceof XPObeliskBlockEntity castBlockEntity)
            return new XPObeliskMenu(castBlockEntity, inventory, pContainerId);
        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new XPObeliskMenu(null, inventory, pContainerId);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        XPObeliskBlockEntity blockEntity = getBlockEntity();
        if (blockEntity == null) {
            return false;
        }

        switch (id) {
        case 0 -> blockEntity.adjustPlayerLevel(player, 1);
        case 1 -> blockEntity.adjustPlayerLevel(player, -1);
        case 2 -> blockEntity.adjustPlayerLevel(player, 10);
        case 3 -> blockEntity.adjustPlayerLevel(player, -10);
        case 4 -> blockEntity.adjustPlayerLevel(player, ExperienceUtil.MAX_PLAYER_LEVEL_GIVE);
        case 5 -> blockEntity.adjustPlayerLevel(player, -ExperienceUtil.MAX_PLAYER_LEVEL_GIVE);
        default -> {}
        }
        return true;
    }

}
