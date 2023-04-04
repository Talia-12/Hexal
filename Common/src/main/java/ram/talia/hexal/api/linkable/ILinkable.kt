package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.asCompound
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nbt.LazyLoad
import ram.talia.hexal.api.nbt.toNbtList
import kotlin.math.sqrt

interface ILinkable {
	val asActionResult: List<Iota>

	val linkableHolder: ServerLinkableHolder?

	/**
	 * Return the registered LinkableType<T> for this [ILinkable], used to save/load the [ILinkable].
	 */
	fun getLinkableType(): LinkableRegistry.LinkableType<*, *>

	fun getPosition(): Vec3

	fun maxSqrLinkRange(): Double

	fun isInRange(other: ILinkable) = (this.getPosition() - other.getPosition()).length() <= 2 * (sqrt(this.maxSqrLinkRange()) +  sqrt(other.maxSqrLinkRange()))

	/**
	 * Set to true if the link should be removed, e.g. the [ILinkable] has been discarded.
	 */
	fun shouldRemove(): Boolean

	fun writeToNbt(): Tag

	fun writeToSync(): Tag

	/**
	 * Returns <= 0 if this [ILinkable] can't accept any media, returns how much media
	 * it is accepting if it can accept media.
	 */
	fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int

	/**
	 * Called to pass [sentMedia] media from [other] to this [ILinkable].
	 */
	fun acceptMedia(other: ILinkable, sentMedia: Int)

	//region default implementations

	fun link(other: ILinkable, linkOther: Boolean = true) {
		if (linkableHolder == null)
			throw Exception("ILinkable.link should only be accessed on server.") // TODO
		linkableHolder!!.link(other, linkOther)
	}
	fun unlink(other: ILinkable, unlinkOther: Boolean = true) {
		if (linkableHolder == null)
			throw Exception("ILinkable.unlink should only be accessed on server.") // TODO
		linkableHolder!!.unlink(other, unlinkOther)
	}
	fun getLinked(index: Int): ILinkable {
		if (linkableHolder == null)
			throw Exception("ILinkable.getLinked should only be accessed on server.") // TODO
		return linkableHolder!!.getLinked(index)
	}
	fun getLinkedIndex(linked: ILinkable): Int {
		if (linkableHolder == null)
			throw Exception("ILinkable.getLinkedIndex should only be accessed on server.") // TODO
		return linkableHolder!!.getLinkedIndex(linked)
	}
	fun numLinked(): Int {
		if (linkableHolder == null)
			throw Exception("ILinkable.numLinked should only be accessed on server.") // TODO
		return linkableHolder!!.numLinked()
	}

	/**
	 * This should be called every tick to remove links that should be removed (i.e. the entity that is linked to has been removed)
	 */
	fun checkLinks() {
		if (linkableHolder == null)
			throw Exception("ILinkable.checkLinks should only be accessed on server.") // TODO
		linkableHolder!!.checkLinks()
	}
	fun receiveIota(iota: Iota) {
		if (linkableHolder == null)
			throw Exception("ILinkable.receiveIota should only be accessed on server.") // TODO
		linkableHolder!!.receiveIota(iota)
	}
	fun nextReceivedIota(): Iota {
		if (linkableHolder == null)
			throw Exception("ILinkable.nextReceivedIota should only be accessed on server.") // TODO
		return linkableHolder!!.nextReceivedIota()
	}
	fun numRemainingIota(): Int {
		if (linkableHolder == null)
			throw Exception("ILinkable.numRemainingIota should only be accessed on server.") // TODO
		return linkableHolder!!.numRemainingIota()
	}
	fun clearReceivedIotas() {
		if (linkableHolder == null)
			throw Exception("ILinkable.clearReceivedIotas should only be accessed on server.") // TODO
		linkableHolder!!.clearReceivedIotas()
	}

	fun allReceivedIotas(): List<Iota> {
		return linkableHolder?.allReceivedIotas() ?: throw Exception("ILinkable.allReceivedIotas should only be accessed on server.")  // TODO
	}


	/**
	 * Called when the player is transmitting to this [ILinkable], should return what should be displayed instead of the stack.
	 */
	fun transmittingTargetReturnDisplay(): List<Component> = this.asActionResult.map(Iota::display)
	//endregion

	/**
	 * returned by [LinkableRegistry.fromSync] to let client renderers render a link to the render centre of a given [ILinkable].
	 */
	interface IRenderCentre {
		val clientLinkableHolder: ClientLinkableHolder?

		/**
		 * Should be called every tick on the client to display the links.
		 */
		fun renderLinks() = clientLinkableHolder?.renderLinks() ?: Unit

		fun renderCentre(other: IRenderCentre, recursioning: Boolean = true): Vec3 // recursioning to stop a player linked to a player causing an infinite loop

		/**
		 * Set to true if the link should be removed, e.g. the [IRenderCentre] has been discarded.
		 */
		fun shouldRemove(): Boolean

		fun colouriser(): FrozenColorizer

		fun getLinkableType(): LinkableRegistry.LinkableType<*, *>
	}

	class LazyILinkable(val level: ServerLevel) : LazyLoad<ILinkable?, CompoundTag>(null) { // default to empty compound tag
		override fun load(unloaded: CompoundTag): ILinkable? = if (unloaded.isEmpty) null else LinkableRegistry.fromNbt(unloaded, level).getOrNull()
		override fun unload(loaded: ILinkable?) = if (loaded == null) CompoundTag() else LinkableRegistry.wrapNbt(loaded)
	}

	class LazyILinkableList(val level: ServerLevel) : LazyLoad<MutableList<ILinkable>, ListTag>(mutableListOf()) {
		override fun load(unloaded: ListTag): MutableList<ILinkable> = unloaded.mapNotNull { LinkableRegistry.fromNbt(it.asCompound, level).getOrNull() } as MutableList
		override fun unload(loaded: MutableList<ILinkable>) = loaded.map { LinkableRegistry.wrapNbt(it) }.toNbtList()

		override fun get(): MutableList<ILinkable> = super.get()!!
	}

	companion object {
		const val MAX_RECEIVED_IOTAS = 144
	}
}