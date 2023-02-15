package ram.talia.hexal.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import ram.talia.hexal.client.LinkablePacketHolder
import ram.talia.hexal.client.RegisterClientStuff
import ram.talia.hexal.fabric.network.FabricPacketHandler

object FabricHexalClientInitializer : ClientModInitializer {
    override fun onInitializeClient() {
        FabricPacketHandler.initClientBound()

        // reattempt link render packets that failed to apply properly once every 20 ticks.
        ClientTickEvents.START_CLIENT_TICK.register { LinkablePacketHolder.maybeRetry() }

        RegisterClientStuff.init()

        RegisterClientStuff.registerBlockEntityRenderers(object : RegisterClientStuff.BlockEntityRendererRegisterer {
            override fun <T : BlockEntity> registerBlockEntityRenderer(type: BlockEntityType<T>, berp: BlockEntityRendererProvider<in T>) {
                BlockEntityRendererRegistry.register(type, berp)
            }
        })
    }
}