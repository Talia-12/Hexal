package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.EntityIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.plus

class PlayerLinkstore(val player: ServerPlayer) : ILinkable {
	override val asActionResult = listOf(EntityIota(player))
	override val linkableHolder: ServerLinkableHolder = ServerLinkableHolder(this, player.getLevel())

	//region Transmitting
	var transmittingTo: ILinkable?
		get() {
			val it = lazyTransmittingTo.get(player.getLevel()) ?: return null

			if (isInRange(it)) return it
			resetTransmittingTo() // if it isn't in range stop transmitting to it
			return null
		}
		private set(value) = lazyTransmittingTo.set(value)
	private val lazyTransmittingTo: ILinkable.LazyILinkable = ILinkable.LazyILinkable()

	fun setTransmittingTo(to: Int) {
		if (to >= numLinked()) {
			resetTransmittingTo()
			return
		}

		transmittingTo = getLinked(to)
	}

	fun resetTransmittingTo() {
		transmittingTo = null
	}
	//endregion

	override fun maxSqrLinkRange() = Action.MAX_DISTANCE * Action.MAX_DISTANCE

	override fun getLinkableType(): LinkableRegistry.LinkableType<PlayerLinkstore, *> = LinkableTypes.PLAYER_LINKSTORE_TYPE

	override fun getPosition(): Vec3 = player.position()

	override fun shouldRemove() = player.isRemoved && player.removalReason?.shouldDestroy() == true
	override fun currentMediaLevel() = -1

	override fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int = 0

	override fun acceptMedia(other: ILinkable, sentMedia: Int) { }

	fun loadAdditionalData(tag: CompoundTag) {
		(tag.get(TAG_LINKABLE_HOLDER) as? CompoundTag)?.let {
			linkableHolder.readFromNbt(it)
		}

		when (val transmittingToTag = tag.get(TAG_TRANSMITTING_TO) as? CompoundTag) {
			null -> lazyTransmittingTo.set(null)
			else -> lazyTransmittingTo.set(transmittingToTag)
		}
	}

	fun saveAdditionalData(tag: CompoundTag) {
		tag.put(TAG_LINKABLE_HOLDER, linkableHolder.writeToNbt())
		tag.put(TAG_TRANSMITTING_TO, lazyTransmittingTo.getUnloaded())
	}

	class RenderCentre(val player: Player) : ILinkable.IRenderCentre {
		override val clientLinkableHolder = ClientLinkableHolder(this, player.level, player.random)

		override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3 {
			if (!recursioning)
				return player.eyePosition
			return player.eyePosition + (other.renderCentre(this, false) - player.eyePosition).normalize()
		}

		override fun shouldRemove() = player.isRemoved && player.removalReason?.shouldDestroy() == true

		override fun colouriser() = at.petrak.hexcasting.xplat.IXplatAbstractions.INSTANCE.getColorizer(player)

		override fun getLinkableType() = LinkableTypes.PLAYER_LINKSTORE_TYPE
	}

	companion object {
		const val TAG_LINKABLE_HOLDER = "hexal:linkable_holder"
		const val TAG_TRANSMITTING_TO = "hexal:transmitting_to"
	}

	override fun toString() = "PlayerLinkstore(player=$player)"
}