package ram.talia.hexal.forge;

import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.spell.SpellDatum;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.fakes.FakePlayer;
import ram.talia.hexal.api.fakes.FakePlayerFactory;
import ram.talia.hexal.common.entities.TickingWisp;
import ram.talia.hexal.forge.eventhandlers.EverbookEventHandler;
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler;
import ram.talia.hexal.forge.eventhandlers.WispCastingMangerEventHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WispTests {
	private static final List<FakePlayer> fakePlayers = new ArrayList<>();
	
	private static final List<SpellDatum<?>> defaultHex = List.of(Patterns.REVEAL);
	
	private static final int NUM_TESTS = 2;
	
	@AfterBatch(batch = "wispBatch")
	public static void afterWispTests (ServerLevel level) {
		teardownPlayers();
	}
	
	@GameTest(templateNamespace = HexalAPI.MOD_ID, template = "basic", batch = "wispBatch")
	public static void basicTickingWispTest (GameTestHelper helper) {
		var fakePlayer = setupPlayer(helper.getLevel());
		
		HexalAPI.LOGGER.debug("running basic ticking wisp test");
		
		TickingWisp wisp = makeTickingWispAtRelativePos(helper, fakePlayer, new Vec3(1.0, 2.0, 1.0));
		
		if (wisp == null)
			return;
		
		fakePlayer.registerSendMessageListener((component, senderUUID) -> {
			HexalAPI.LOGGER.debug("received in WispTests.basicTickingWispTest: " + component.getString());
			
			// succeed as soon as the wisp sends successfully executes the "Reveal" pattern.
			if (component.getString().equals("Cyclic Wisp"))
				helper.succeed();
		});
		
		
		helper.onEachTick(() -> {
			HexalAPI.LOGGER.debug("current tick: " + helper.getTick() + ", current media: " + (wisp.getMedia() / (float) ManaConstants.DUST_UNIT));

			tickPlayer(fakePlayer);
		});
	}
	
	@GameTest(templateNamespace = HexalAPI.MOD_ID, template = "basic", batch = "wispBatch")
	public static void linkWispsMessageTest (GameTestHelper helper) {
		var fakePlayer = setupPlayer(helper.getLevel());
		
		HexalAPI.LOGGER.debug("running link wisps message test");
		
		var receivingHex = List.of(Patterns.RECITATION, Patterns.GEMINIS_DISINTEGRATION, Patterns.NULLARY, Patterns.EQUALITY, Patterns.INTRO,
															 Patterns.REVEAL, Patterns.DROP, Patterns.RETRO, Patterns.SWAP, Patterns.SELECTION_DISTILLATION, Patterns.HERMES);
		var sendingHex = List.of(Patterns.POPULARITY, Patterns.ZERO, Patterns.EQUALITY, Patterns.IDENTITY, Patterns.INTRO, Patterns.COMPASS,
														 Patterns.FOUR, Patterns.ZONE_DSTL_WISP, Patterns.FLOCKS_DISINTEGRATION, Patterns.LINK_ENTITY, Patterns.RETRO, Patterns.CONJUNCTION,
														 Patterns.INTRO, Patterns.ZERO, Patterns.FOUR, Patterns.SEND_IOTA, Patterns.RETRO, Patterns.DISJUNCTION, Patterns.HERMES);
		
		TickingWisp receivingWisp = makeTickingWispAtRelativePos(helper, fakePlayer, new Vec3(0.0, 2.0, 0.0), receivingHex);
		TickingWisp sendingWisp = makeTickingWispAtRelativePos(helper, fakePlayer, new Vec3(2.0, 2.0, 2.0), sendingHex);
		
		if (receivingWisp == null || sendingWisp == null)
			return;
		
		fakePlayer.registerSendMessageListener((component, senderUUID) -> {
			HexalAPI.LOGGER.debug("received in WispTests.linkWispsMessageTest: " + component.getString());
			
			// succeed as soon as the wisp sends successfully executes the "Reveal" pattern.
			
			if (component.getString().equals("4.0000"))
				helper.succeed();
		});
		
		helper.onEachTick(() -> {
			HexalAPI.LOGGER.debug("current tick: " + helper.getTick());
			
			tickPlayer(fakePlayer);
		});
	}
	
	
	@GameTest(templateNamespace = HexalAPI.MOD_ID, template = "basic", timeoutTicks = 1200) // TODO: change to its own template
	public static void farmingWispTest (GameTestHelper helper) {
		var fakePlayer = setupPlayer(helper.getLevel());
		
		HexalAPI.LOGGER.debug("testing farming wisp");
		
		var farmingHex = List.of(Patterns.ZERO, Patterns.REVEAL, Patterns.DROP);
		
		//TODO: change offset to centre of template.
		var wisp = makeTickingWispAtRelativePos(helper, fakePlayer, new Vec3(0.0, 0.0, 0.0), farmingHex);
		
		helper.onEachTick(() -> {
			HexalAPI.LOGGER.debug("current tick: " + helper.getTick());
			
			tickPlayer(fakePlayer);
		});
		
		helper.succeedWhen(() -> {
			if (wisp == null || wisp.getMedia() < 30 * ManaConstants.SHARD_UNIT)
				throw new GameTestAssertException("Wisp doesn't have enough media yet.");
		});
	}
	
	@Nullable
	private static TickingWisp makeTickingWispAtRelativePos (GameTestHelper helper, FakePlayer fakePlayer, Vec3 pos) {
		return makeTickingWispAtRelativePos(helper, fakePlayer, pos, defaultHex);
	}
	
	@Nullable
	private static TickingWisp makeTickingWispAtRelativePos (GameTestHelper helper, FakePlayer fakePlayer, Vec3 pos, List<SpellDatum<?>> hex) {
		Vec3 spawnPos = helper.absoluteVec(pos);
		
		HexalAPI.LOGGER.debug("making a wisp at " + spawnPos);
		
		TickingWisp wisp = new TickingWisp(helper.getLevel(), spawnPos, fakePlayer, 10 * ManaConstants.SHARD_UNIT);
		
		wisp.setHex(hex);
		
		if (helper.getLevel().addFreshEntity(wisp))
			return wisp;
		else {
			helper.fail("Couldn't create wisp.");
			return null;
		}
	}
	
	private static long lastTicked = 0L;
	
	private static FakePlayer setupPlayer (ServerLevel level) {
		var fakePlayer = FakePlayerFactory.INSTANCE.getRandom(level);
		PlayerEvent.PlayerLoggedInEvent event = new PlayerEvent.PlayerLoggedInEvent(fakePlayer);
		WispCastingMangerEventHandler.playerLoggedIn(event);
		PlayerLinkstoreEventHandler.playerLoggedIn(event);
		EverbookEventHandler.playerLoggedIn(event);
		fakePlayers.add(fakePlayer);
		return fakePlayer;
	}
	
	private static void teardownPlayers() {
		fakePlayers.forEach(fakePlayer -> {
			PlayerEvent.PlayerLoggedOutEvent event = new PlayerEvent.PlayerLoggedOutEvent(fakePlayer);
			WispCastingMangerEventHandler.playerLoggedOut(event);
			PlayerLinkstoreEventHandler.playerLoggedOut(event);
		});
		
		fakePlayers.clear();
	}
	
	private static void tickPlayer (ServerPlayer player) {
		if (lastTicked == player.level.getGameTime())
			return;
		lastTicked = player.level.getGameTime();
		
		TickEvent.PlayerTickEvent event = new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player);
		
		WispCastingMangerEventHandler.playerTick(event);
		try {
			PlayerLinkstoreEventHandler.playerTick(event);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void tickAllPlayers () {
		fakePlayers.forEach(WispTests::tickPlayer);
	}
}
