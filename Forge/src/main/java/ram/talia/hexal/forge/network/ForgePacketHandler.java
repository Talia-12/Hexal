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
		NETWORK.registerMessage(messageIdx++, MsgSendEverbookSyn.class, MsgSendEverbookSyn::serialize,
				MsgSendEverbookSyn::deserialise, makeServerBoundHandler(MsgSendEverbookSyn::handle));
		
		// Server -> client
		//everbook
		NETWORK.registerMessage(messageIdx++, MsgSetEverbookAck.class, MsgSetEverbookAck::serialize,
				MsgSetEverbookAck::deserialise, makeClientBoundHandler(MsgSetEverbookAck::handle));
		NETWORK.registerMessage(messageIdx++, MsgRemoveEverbookAck.class, MsgRemoveEverbookAck::serialize,
				MsgRemoveEverbookAck::deserialise, makeClientBoundHandler(MsgRemoveEverbookAck::handle));
		NETWORK.registerMessage(messageIdx++, MsgToggleMacroAck.class, MsgToggleMacroAck::serialize,
				MsgToggleMacroAck::deserialise, makeClientBoundHandler(MsgToggleMacroAck::handle));

		//cast sound
		NETWORK.registerMessage(messageIdx++, MsgWispCastSoundAck.class, MsgWispCastSoundAck::serialize,
				MsgWispCastSoundAck::deserialise, makeClientBoundHandler(MsgWispCastSoundAck::handle));

		//syncing render links
		NETWORK.registerMessage(messageIdx++, MsgAddRenderLinkAck.class, MsgAddRenderLinkAck::serialize,
				MsgAddRenderLinkAck::deserialise, makeClientBoundHandler(MsgAddRenderLinkAck::handle));
		NETWORK.registerMessage(messageIdx++, MsgRemoveRenderLinkAck.class, MsgRemoveRenderLinkAck::serialize,
				MsgRemoveRenderLinkAck::deserialise, makeClientBoundHandler(MsgRemoveRenderLinkAck::handle));
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
