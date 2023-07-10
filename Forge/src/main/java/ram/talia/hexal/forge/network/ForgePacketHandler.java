package ram.talia.hexal.forge.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.util.TriConsumer;
import ram.talia.hexal.common.network.*;

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
		
		// Client -> server
		NETWORK.registerMessage(messageIdx++, MsgSendEverbookC2S.class, MsgSendEverbookC2S::serialize,
				MsgSendEverbookC2S::deserialise, makeServerBoundHandler(MsgSendEverbookC2S::handle));
		
		// Server -> client
		//everbook
		NETWORK.registerMessage(messageIdx++, MsgSetEverbookS2C.class, MsgSetEverbookS2C::serialize,
				MsgSetEverbookS2C::deserialise, makeClientBoundHandler(MsgSetEverbookS2C::handle));
		NETWORK.registerMessage(messageIdx++, MsgRemoveEverbookS2C.class, MsgRemoveEverbookS2C::serialize,
				MsgRemoveEverbookS2C::deserialise, makeClientBoundHandler(MsgRemoveEverbookS2C::handle));
		NETWORK.registerMessage(messageIdx++, MsgToggleMacroS2C.class, MsgToggleMacroS2C::serialize,
				MsgToggleMacroS2C::deserialise, makeClientBoundHandler(MsgToggleMacroS2C::handle));

		//cast sound
		NETWORK.registerMessage(messageIdx++, MsgWispCastSoundS2C.class, MsgWispCastSoundS2C::serialize,
				MsgWispCastSoundS2C::deserialise, makeClientBoundHandler(MsgWispCastSoundS2C::handle));

		//syncing render links
		NETWORK.registerMessage(messageIdx++, MsgAddRenderLinkS2C.class, MsgAddRenderLinkS2C::serialize,
				MsgAddRenderLinkS2C::deserialise, makeClientBoundHandler(MsgAddRenderLinkS2C::handle));
		NETWORK.registerMessage(messageIdx++, MsgRemoveRenderLinkS2C.class, MsgRemoveRenderLinkS2C::serialize,
				MsgRemoveRenderLinkS2C::deserialise, makeClientBoundHandler(MsgRemoveRenderLinkS2C::handle));
		NETWORK.registerMessage(messageIdx++, MsgSetRenderLinksAck.class, MsgSetRenderLinksAck::serialize,
				MsgSetRenderLinksAck::deserialise, makeClientBoundHandler(MsgSetRenderLinksAck::handle));

		// Particles spell
		NETWORK.registerMessage(messageIdx++, MsgSingleParticleAck.class, MsgSingleParticleAck::serialize,
				MsgSingleParticleAck::deserialise, makeClientBoundHandler(MsgSingleParticleAck::handle));
		NETWORK.registerMessage(messageIdx++, MsgParticleLinesAck.class, MsgParticleLinesAck::serialize,
				MsgParticleLinesAck::deserialise, makeClientBoundHandler(MsgParticleLinesAck::handle));
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
