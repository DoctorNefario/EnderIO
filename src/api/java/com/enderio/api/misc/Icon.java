package com.enderio.api.misc;

import com.enderio.api.UseOnly;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

public interface Icon {
    Vector2i DEFAULT_TEXTURE_SIZE = new Vector2i(256, 256);

    /**
     * @return The texture that needs to be bound to the texturemanager to be rendered
     */
    @UseOnly(LogicalSide.CLIENT)
    ResourceLocation getTextureLocation();

    /**
     * @return the size of the area on the texture you want to render
     */
    @UseOnly(LogicalSide.CLIENT)
    Vector2i getIconSize();

    /**
     * @return the size you want to render the texturearea at
     */
    @UseOnly(LogicalSide.CLIENT)
    default Vector2i getRenderSize() {
        return getIconSize();
    }

    /**
     * @return the position your icon is on the texture
     */
    @UseOnly(LogicalSide.CLIENT)
    Vector2i getTexturePosition();

    /**
     * @return a Component that is rendered on hover, if this icon is rendered on a gui
     */
    @UseOnly(LogicalSide.CLIENT)
    default Component getTooltip() {
        return Component.empty();
    }

    /**
     * @return the texture size
     */
    @UseOnly(LogicalSide.CLIENT)
    default Vector2i getTextureSize() {
        return DEFAULT_TEXTURE_SIZE;
    }

    /**
     * @return if this icon should render
     */
    @UseOnly(LogicalSide.CLIENT)
    default boolean shouldRender() {
        return true;
    }

    record Simple(ResourceLocation textureLocation, Vector2i texturePosition, Vector2i iconSize, Vector2i renderSize) implements Icon {

        public Simple(ResourceLocation textureLocation, Vector2i texturePosition, Vector2i iconSize) {
            this(textureLocation, texturePosition, iconSize, iconSize);
        }

        @Override
        public ResourceLocation getTextureLocation() {
            return textureLocation;
        }

        @Override
        public Vector2i getTexturePosition() {
            return texturePosition;
        }

        @Override
        public Vector2i getIconSize() {
            return iconSize;
        }

        @Override
        public Vector2i getRenderSize() {
            return renderSize;
        }
    }
}
