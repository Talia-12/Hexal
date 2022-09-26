package ram.talia.hexal.forge.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.util.TriConsumer;
import ram.talia.hexal.common.network.MsgWispCastSoundAck;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class ForgePacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	private static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
					modLoc("main"),
					() -> PROTOCOL_VERSION,
					PROTOCOL_VERSION::equals,
					PROTOCOL_VERSION::equals
	);
	
	public static SimpleChannel getNetwork() {
		return NETWORK;
	}
	
	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		int messageIdx = 0;
		
		// Server -> client
		//general
		NETWORK.registerMessage(messageIdx++, MsgWispCastSoundAck.class, MsgWispCastSoundAck::serialize,
														MsgWispCastSoundAck::deserialise, makeClientBoundHandler(MsgWispCastSoundAck::handle));
		
		
		// forge specific
		NETWORK.registerMessage(messageIdx++, MsgPlayerRenderLinksAck.class, MsgPlayerRenderLinksAck::serialize,
														MsgPlayerRenderLinksAck::deserialise, makeClientBoundHandler(MsgPlayerRenderLinksAck::handle));
		NETWORK.registerMessage(messageIdx++, MsgPlayerAddRenderLinkAck.class, MsgPlayerAddRenderLinkAck::serialize,
														MsgPlayerAddRenderLinkAck::deserialise, makeClientBoundHandler(MsgPlayerAddRenderLinkAck::handle));
		NETWORK.registerMessage(messageIdx++, MsgPlayerRemoveRenderLinkAck.class, MsgPlayerRemoveRenderLinkAck::serialize,
														MsgPlayerRemoveRenderLinkAck::deserialise, makeClientBoundHandler(MsgPlayerRemoveRenderLinkAck::handle));
		NETWORK.registerMessage(messageIdx++, MsgPlayerClearRenderLinksAck.class, MsgPlayerClearRenderLinksAck::serialize,
														MsgPlayerClearRenderLinksAck::deserialise, makeClientBoundHandler(MsgPlayerClearRenderLinksAck::handle));


		// Client -> server
		
	}
	
	private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> makeServerBoundHandler(
					TriConsumer<T, MinecraftServer, ServerPlayer> handler) {
		return (m, ctx) -> {
			handler.accept(m, Objects.requireNonNull(ctx.get().getSender()).getServer(), ctx.get().getSender());
			ctx.get().setPacketHandled(true);
		};
	}
	
	private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> makeClientBoundHandler(Consumer<T> consumer) {
		return (m, ctx) -> {
			consumer.accept(m);
			ctx.get().setPacketHandled(true);
		};
	}
}
