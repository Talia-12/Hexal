package ram.talia.hexal.forge.mixin;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ram.talia.hexal.forge.client.items.IRenderPropertiesSetter;

@Mixin(Item.class)
public abstract class MixinItem implements IRenderPropertiesSetter {
    @Shadow
    private Object renderProperties;

    public void setRenderProperties(IClientItemExtensions properties) {
        renderProperties = properties;
    }
}
