package ram.talia.hexal.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.client.blocks.BlockEntityMediafiedStorageRenderer;
import ram.talia.hexal.client.blocks.BlockEntityRelayRenderer;
import ram.talia.hexal.client.entity.WispRenderer;
import ram.talia.hexal.common.lib.HexalBlockEntities;
import ram.talia.hexal.common.lib.HexalEntities;
import ram.talia.hexal.xplat.IClientXplatAbstractions;

public class RegisterClientStuff {
    public static void init() {
        var x = IClientXplatAbstractions.INSTANCE;

        x.registerEntityRenderer(HexalEntities.PROJECTILE_WISP, WispRenderer::new);
        x.registerEntityRenderer(HexalEntities.TICKING_WISP, WispRenderer::new);
        x.registerEntityRenderer(HexalEntities.WANDERING_WISP, WispRenderer::new);
    }

    public static void registerBlockEntityRenderers(@NotNull BlockEntityRendererRegisterer registerer) {
        registerer.registerBlockEntityRenderer(HexalBlockEntities.MEDIAFIED_STORAGE, ctx -> new BlockEntityMediafiedStorageRenderer());
        registerer.registerBlockEntityRenderer(HexalBlockEntities.RELAY, ctx -> new BlockEntityRelayRenderer());
    }

    @FunctionalInterface
    public interface BlockEntityRendererRegisterer {
        <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type, BlockEntityRendererProvider<? super T> berp);
    }
}
