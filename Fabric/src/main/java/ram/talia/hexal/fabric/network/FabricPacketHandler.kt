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
		ServerPlayNetworking.registerGlobalReceiver(MsgSendEverbookSyn.ID, makeServerBoundHandler(
			(MsgSendEverbookSyn)::deserialise, { msg: MsgSendEverbookSyn, server: MinecraftServer, sender: ServerPlayer -> msg.handle(server, sender) })
		)
	}

	/**
	 * For registering packets that are server -> client (called on the client)
	 */
	fun initClientBound() {
		// Everbook
		ClientPlayNetworking.registerGlobalReceiver(MsgSetEverbookAck.ID, makeClientBoundHandler((MsgSetEverbookAck)::deserialise, (MsgSetEverbookAck)::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgRemoveEverbookAck.ID, makeClientBoundHandler((MsgRemoveEverbookAck)::deserialise, (MsgRemoveEverbookAck)::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgToggleMacroAck.ID, makeClientBoundHandler((MsgToggleMacroAck)::deserialise, (MsgToggleMacroAck)::handle))
		// Cast Sound
		ClientPlayNetworking.registerGlobalReceiver(MsgWispCastSoundAck.ID, makeClientBoundHandler((MsgWispCastSoundAck)::deserialise, (MsgWispCastSoundAck)::handle))
		// Render links
		ClientPlayNetworking.registerGlobalReceiver(MsgAddRenderLinkAck.ID, makeClientBoundHandler((MsgAddRenderLinkAck)::deserialise, (MsgAddRenderLinkAck)::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgRemoveRenderLinkAck.ID, makeClientBoundHandler((MsgRemoveRenderLinkAck)::deserialise, (MsgRemoveRenderLinkAck)::handle))
		ClientPlayNetworking.registerGlobalReceiver(MsgSetRenderLinksAck.ID, makeClientBoundHandler((MsgSetRenderLinksAck)::deserialise, (MsgSetRenderLinksAck)::handle))
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