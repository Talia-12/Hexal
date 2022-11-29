package ram.talia.hexal.api.fakes

import com.mojang.authlib.GameProfile
import net.minecraft.core.BlockPos
import net.minecraft.network.Connection
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.*
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerAdvancements
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.stats.Stat
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import java.util.*
import java.util.function.Consumer
import javax.annotation.ParametersAreNonnullByDefault

class FakePlayer(level: ServerLevel, name: GameProfile) : ServerPlayer(level.server, level, name, null) {

	private val sendMessageListeners: MutableList<Consumer<Component>> = mutableListOf()
	init {
		connection = FakePlayerNetHandler(level.server, this)
	}

	override fun position() = Vec3.ZERO
	override fun blockPosition() = BlockPos.ZERO
	override fun displayClientMessage(chatComponent: Component, actionBar: Boolean) {}

	override fun sendSystemMessage(component: Component, forceAcceptMessage: Boolean) {
		HexalAPI.LOGGER.debug("player $uuid sent ${component.string} ")
		sendMessageListeners.forEach { it.accept(component) }
	}
	override fun awardStat(par1StatBase: Stat<*>, par2: Int) {}
	//@Override public void openGui(Object mod, int modGuiId, World world, int x, int y, int z){}
	override fun isInvulnerableTo(source: DamageSource) = true
	override fun canHarmPlayer(player: Player) = false
	override fun die(source: DamageSource) { }
	override fun tick() { }
	override fun updateOptions(pkt: ServerboundClientInformationPacket) { }
	override fun getServer() = level.server

	override fun getAdvancements(): PlayerAdvancements {
		return FakePlayerAdvancements(this)
	}

	/**
	 * Register a [listener], which will be alerted whenever this [FakePlayer] is (on the server) sent a message. The parameters the listener receives are the [Component]
	 * for the message, and the [UUID] of the sender.
	 */
	fun registerSendMessageListener(listener: Consumer<Component>) {
		if (!sendMessageListeners.contains(listener))
			sendMessageListeners += listener
	}

	fun deregisterSendMessageListener(listener: Consumer<Component>) {
		sendMessageListeners -= listener
	}

	@ParametersAreNonnullByDefault
	private class FakePlayerNetHandler(server: MinecraftServer, player: ServerPlayer) :
		ServerGamePacketListenerImpl(server, DUMMY_CONNECTION, player) {
		override fun tick() {}
		override fun resetPosition() {}
		override fun disconnect(message: Component) {}
		override fun handlePlayerInput(packet: ServerboundPlayerInputPacket) {}
		override fun handleMoveVehicle(packet: ServerboundMoveVehiclePacket) {}
		override fun handleAcceptTeleportPacket(packet: ServerboundAcceptTeleportationPacket) {}
		override fun handleRecipeBookSeenRecipePacket(packet: ServerboundRecipeBookSeenRecipePacket) {}
		override fun handleRecipeBookChangeSettingsPacket(packet: ServerboundRecipeBookChangeSettingsPacket) {}
		override fun handleSeenAdvancements(packet: ServerboundSeenAdvancementsPacket) {}
		override fun handleCustomCommandSuggestions(packet: ServerboundCommandSuggestionPacket) {}
		override fun handleSetCommandBlock(packet: ServerboundSetCommandBlockPacket) {}
		override fun handleSetCommandMinecart(packet: ServerboundSetCommandMinecartPacket) {}
		override fun handlePickItem(packet: ServerboundPickItemPacket) {}
		override fun handleRenameItem(packet: ServerboundRenameItemPacket) {}
		override fun handleSetBeaconPacket(packet: ServerboundSetBeaconPacket) {}
		override fun handleSetStructureBlock(packet: ServerboundSetStructureBlockPacket) {}
		override fun handleSetJigsawBlock(packet: ServerboundSetJigsawBlockPacket) {}
		override fun handleJigsawGenerate(packet: ServerboundJigsawGeneratePacket) {}
		override fun handleSelectTrade(packet: ServerboundSelectTradePacket) {}
		override fun handleEditBook(packet: ServerboundEditBookPacket) {}
		override fun handleEntityTagQuery(packet: ServerboundEntityTagQuery) {}
		override fun handleBlockEntityTagQuery(packet: ServerboundBlockEntityTagQuery) {}
		override fun handleMovePlayer(packet: ServerboundMovePlayerPacket) {}
		override fun teleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {}
		override fun teleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, flags: Set<ClientboundPlayerPositionPacket.RelativeArgument>) {}
		override fun handlePlayerAction(packet: ServerboundPlayerActionPacket) {}
		override fun handleUseItemOn(packet: ServerboundUseItemOnPacket) {}
		override fun handleUseItem(packet: ServerboundUseItemPacket) {}
		override fun handleTeleportToEntityPacket(packet: ServerboundTeleportToEntityPacket) {}
		override fun handleResourcePackResponse(packet: ServerboundResourcePackPacket) {}
		override fun handlePaddleBoat(packet: ServerboundPaddleBoatPacket) {}
		override fun onDisconnect(message: Component) {}
		override fun send(packet: Packet<*>) {}
		override fun handleSetCarriedItem(packet: ServerboundSetCarriedItemPacket) {}
		override fun handleChat(packet: ServerboundChatPacket) {}
		override fun handleAnimate(packet: ServerboundSwingPacket) {}
		override fun handlePlayerCommand(packet: ServerboundPlayerCommandPacket) {}
		override fun handleInteract(packet: ServerboundInteractPacket) {}
		override fun handleClientCommand(packet: ServerboundClientCommandPacket) {}
		override fun handleContainerClose(packet: ServerboundContainerClosePacket) {}
		override fun handleContainerClick(packet: ServerboundContainerClickPacket) {}
		override fun handlePlaceRecipe(packet: ServerboundPlaceRecipePacket) {}
		override fun handleContainerButtonClick(packet: ServerboundContainerButtonClickPacket) {}
		override fun handleSetCreativeModeSlot(packet: ServerboundSetCreativeModeSlotPacket) {}
		override fun handleSignUpdate(packet: ServerboundSignUpdatePacket) {}
		override fun handleKeepAlive(packet: ServerboundKeepAlivePacket) {}
		override fun handlePlayerAbilities(packet: ServerboundPlayerAbilitiesPacket) {}
		override fun handleClientInformation(packet: ServerboundClientInformationPacket) {}
		override fun handleCustomPayload(packet: ServerboundCustomPayloadPacket) {}
		override fun handleChangeDifficulty(packet: ServerboundChangeDifficultyPacket) {}
		override fun handleLockDifficulty(packet: ServerboundLockDifficultyPacket) {}

		companion object {
			private val DUMMY_CONNECTION = Connection(PacketFlow.CLIENTBOUND)
		}
	}
}