package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.EntityIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nbt.SerialisedIotaList
import ram.talia.hexal.api.plus
import ram.talia.hexal.xplat.IXplatAbstractions

class PlayerLinkstore(val player: ServerPlayer) : ILinkable<PlayerLinkstore> {
	override val asActionResult = listOf(EntityIota(player))
	override val _level: ServerLevel = player.getLevel()

	override val _serReceivedIotas: SerialisedIotaList = SerialisedIotaList(null)

	override val _lazyLinked = ILinkable.LazyILinkableList(player.level as ServerLevel)

	val renderLinks: MutableList<ILinkable<*>>
		get() = _lazyRenderLinks.get()

	override val _lazyRenderLinks: ILinkable.LazyILinkableList = ILinkable.LazyILinkableList(player.level as ServerLevel)

	//region Transmitting
	var transmittingTo: ILinkable<*>?
		get() {
			val it = lazyTransmittingTo.get() ?: return null

			if (isInRange(it)) return it
			resetTransmittingTo() // if it isn't in range stop transmitting to it
			return null
		}
		private set(value) = lazyTransmittingTo.set(value)
	private val lazyTransmittingTo: ILinkable.LazyILinkable = ILinkable.LazyILinkable(player.level as ServerLevel)

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

	override fun get() = this

	override fun maxSqrLinkRange() = Action.MAX_DISTANCE * Action.MAX_DISTANCE

	override fun getLinkableType(): LinkableRegistry.LinkableType<PlayerLinkstore, *> = LinkableTypes.PLAYER_LINKSTORE_TYPE

	override fun getPosition(): Vec3 = player.position()

	override fun shouldRemove() = player.isRemoved && player.removalReason?.shouldDestroy() == true

	override fun syncAddRenderLink(other: ILinkable<*>)
		= IXplatAbstractions.INSTANCE.syncAddRenderLinkPlayer(player, other)

	override fun syncRemoveRenderLink(other: ILinkable<*>)
		= IXplatAbstractions.INSTANCE.syncRemoveRenderLinkPlayer(player, other)

	override fun writeToNbt(): Tag = NbtUtils.createUUID(player.uuid)

	override fun writeToSync(): Tag = IntTag.valueOf(player.id)

	fun loadAdditionalData(tag: CompoundTag) {
		when (val linkedTag = tag.get(TAG_LINKS) as? ListTag) {
			null -> _lazyLinked.set(mutableListOf())
			else -> _lazyLinked.set(linkedTag)
		}
		when (val renderLinkedTag = tag.get(TAG_RENDER_LINKS) as? ListTag) {
			null -> _lazyRenderLinks.set(mutableListOf())
			else -> _lazyRenderLinks.set(renderLinkedTag)
		}
		when (val receivedIotaTag = tag.get(TAG_RECEIVED_IOTAS) as? ListTag) {
			null -> _serReceivedIotas.set(mutableListOf())
			else -> _serReceivedIotas.tag = receivedIotaTag
		}
		when (val transmittingToTag = tag.get(TAG_TRANSMITTING_TO) as? CompoundTag) {
			null -> lazyTransmittingTo.set(null)
			else -> lazyTransmittingTo.set(transmittingToTag)
		}
	}

	fun saveAdditionalData(tag: CompoundTag) {
		tag.put(TAG_LINKS, _lazyLinked.getUnloaded())
		tag.put(TAG_RENDER_LINKS, _lazyRenderLinks.getUnloaded())
		_serReceivedIotas.tag?.let { tag.put(TAG_RECEIVED_IOTAS, it) }
		tag.put(TAG_TRANSMITTING_TO, lazyTransmittingTo.getUnloaded())
	}

	class RenderCentre(val player: Player) : ILinkable.IRenderCentre {
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
		const val TAG_LINKS = "links"
		const val TAG_RENDER_LINKS = "render_links"
		const val TAG_RECEIVED_IOTAS = "received_iotas"
		const val TAG_TRANSMITTING_TO = "transmitting_to"
	}

	override fun toString() = "PlayerLinkstore(player=$player)"
}