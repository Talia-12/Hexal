package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
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

	private val serReceivedIotas: SerialisedIotaList = SerialisedIotaList(null)

	var linked: MutableList<ILinkable<*>>
		get() = lazyLinked.get()
		set(value) = lazyLinked.set(value)

	private val lazyLinked = ILinkable.LazyILinkableList(player.level as ServerLevel)

	val renderLinks: MutableList<ILinkable<*>>
		get() = lazyRenderLinks.get()

	private val lazyRenderLinks: ILinkable.LazyILinkableList = ILinkable.LazyILinkableList(player.level as ServerLevel)


	private fun addRenderLink(other: ILinkable<*>) {
		renderLinks.add(other)
		IXplatAbstractions.INSTANCE.syncAddRenderLinkPlayer(player, other)
	}

	private fun removeRenderLink(other: ILinkable<*>) {
		renderLinks.remove(other)
		IXplatAbstractions.INSTANCE.syncRemoveRenderLinkPlayer(player, other)
	}

	fun removeRenderLink(index: Int) {
		val removed = renderLinks.removeAt(index)
		IXplatAbstractions.INSTANCE.syncRemoveRenderLinkPlayer(player, removed)
	}

	/**
	 * This should be called every tick to remove links that should be removed (i.e. the entity that is linked to has been removed)
	 */
	fun pruneLinks() {
		for (i in (linked.size - 1) downTo 0) {
			if (linked[i].shouldRemove() || !isInRange(linked[i]))
				unlink(linked[i])
		}
	}

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

	override fun getPos() = player.position()

	override fun shouldRemove() = player.isRemoved && player.removalReason?.shouldDestroy() == true

	override fun link(other: ILinkable<*>, linkOther: Boolean) {
		if (other in linked || (other is PlayerLinkstore && this.player.uuid.equals(other.player.uuid)))
			return

		linked.add(other)

		if (linkOther) {
			addRenderLink(other)
		}

		if (linkOther) {
			other.link(this, false)
		}
	}

	override fun unlink(other: ILinkable<*>, unlinkOther: Boolean) {
		linked.remove(other)
		removeRenderLink(other)

		if (unlinkOther) {
			other.unlink(this, false)
		}
	}

	override fun getLinked(index: Int) = linked[index]

	override fun getLinkedIndex(linked: ILinkable<*>) = this.linked.indexOf(linked)

	override fun numLinked() = linked.size

	override fun receiveIota(iota: Iota) = serReceivedIotas.add(iota, player.getLevel())

	override fun nextReceivedIota() = serReceivedIotas.pop(player.getLevel()) ?: NullIota()

	override fun numRemainingIota() = linked.size

	override fun writeToNbt(): Tag = NbtUtils.createUUID(player.uuid)

	override fun writeToSync(): Tag = IntTag.valueOf(player.id)

	fun loadAdditionalData(tag: CompoundTag) {
		when (val linkedTag = tag.get(TAG_LINKS) as? ListTag) {
			null -> lazyLinked.set(mutableListOf())
			else -> lazyLinked.set(linkedTag)
		}
		when (val renderLinkedTag = tag.get(TAG_RENDER_LINKS) as? ListTag) {
			null -> lazyRenderLinks.set(mutableListOf())
			else -> lazyRenderLinks.set(renderLinkedTag)
		}
		when (val receivedIotaTag = tag.get(TAG_RECEIVED_IOTAS) as? ListTag) {
			null -> serReceivedIotas.set(mutableListOf())
			else -> serReceivedIotas.tag = receivedIotaTag
		}
		when (val transmittingToTag = tag.get(TAG_TRANSMITTING_TO) as? CompoundTag) {
			null -> lazyTransmittingTo.set(null)
			else -> lazyTransmittingTo.set(transmittingToTag)
		}
	}

	fun saveAdditionalData(tag: CompoundTag) {
		tag.put(TAG_LINKS, lazyLinked.getUnloaded())
		tag.put(TAG_RENDER_LINKS, lazyRenderLinks.getUnloaded())
		serReceivedIotas.tag?.let { tag.put(TAG_RECEIVED_IOTAS, it) }
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