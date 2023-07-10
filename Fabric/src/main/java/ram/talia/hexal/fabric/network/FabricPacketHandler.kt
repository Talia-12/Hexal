package ram.talia.hexal.fabric.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.apache.logging.log4j.util.TriConsumer
import ram.talia.hexal.common.network.*
import java.util.function.Consumer
import java.util.function.Function

object FabricPacketHandler {
	/**
	 * For registering packets that are client -> server (called on the server)
	 */
	@Suppress("MoveLambdaOutsideParentheses")
	fun initServerBound() {
		ServerPlayNetworking.registerGlobalReceiver(MsgSendEverbookC2S.ID, makeServerBoundHandler(
			(MsgSendEverbookC2S)::deserialise, { msg: MsgSendEverbookC2S, server: MinecraftServer, sender: ServerPlayer -> msg.handle(server, sender) })
		)
	}

	/**
	 * For registering packets that are server -> client (called on the client)
	 */
	fun initClientBound() {
		// Everbook
		ClientPlayNetworking.registerGlobalReceiver(MsgSetEverbookS2C.ID, makeClientBoundHandler(MsgSetEverbookS2C::deserialise, MsgSetEverbookS2C::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgRemoveEverbookS2C.ID, makeClientBoundHandler(MsgRemoveEverbookS2C::deserialise, MsgRemoveEverbookS2C::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgToggleMacroS2C.ID, makeClientBoundHandler(MsgToggleMacroS2C::deserialise, MsgToggleMacroS2C::handle))
		// Cast Sound
		ClientPlayNetworking.registerGlobalReceiver(MsgWispCastSoundS2C.ID, makeClientBoundHandler(MsgWispCastSoundS2C::deserialise, MsgWispCastSoundS2C::handle))
		// Render links
		ClientPlayNetworking.registerGlobalReceiver(MsgAddRenderLinkS2C.ID, makeClientBoundHandler(MsgAddRenderLinkS2C::deserialise, MsgAddRenderLinkS2C::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgRemoveRenderLinkS2C.ID, makeClientBoundHandler(MsgRemoveRenderLinkS2C::deserialise, MsgRemoveRenderLinkS2C::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgSetRenderLinksAck.ID, makeClientBoundHandler(MsgSetRenderLinksAck::deserialise, MsgSetRenderLinksAck::handle))
		// Particles spell
		ClientPlayNetworking.registerGlobalReceiver(MsgSingleParticleAck.ID, makeClientBoundHandler(MsgSingleParticleAck::deserialise, MsgSingleParticleAck::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgParticleLinesAck.ID, makeClientBoundHandler(MsgParticleLinesAck::deserialise, MsgParticleLinesAck::handle))
		// Phase Block
		ClientPlayNetworking.registerGlobalReceiver(MsgPhaseBlockS2C.ID, makeClientBoundHandler(MsgPhaseBlockS2C::deserialise, MsgPhaseBlockS2C::handle))
	}

	private fun <T> makeServerBoundHandler(decoder: Function<FriendlyByteBuf, T>, handle: TriConsumer<T, MinecraftServer, ServerPlayer>):
					ServerPlayNetworking.PlayChannelHandler {
		return ServerPlayNetworking.PlayChannelHandler {
				server: MinecraftServer,
				player: ServerPlayer,
				_: ServerGamePacketListenerImpl,
				buf: FriendlyByteBuf,
				_: PacketSender ->
			handle.accept(decoder.apply(buf), server, player)
		}
	}

	private fun <T> makeClientBoundHandler(decoder: Function<FriendlyByteBuf, T>, handler: Consumer<T>): ClientPlayNetworking.PlayChannelHandler {
		return ClientPlayNetworking.PlayChannelHandler { _: Minecraft, _: ClientPacketListener, buf: FriendlyByteBuf, _: PacketSender ->
			handler.accept(decoder.apply(buf))
		}
	}
}