package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
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

//	fun writeToNbt(): Tag
//
//	fun writeToSync(): Tag

	/**
	 * Media level of this [ILinkable], that is able to be sent to other [ILinkable]s. If this [ILinkable] will never
	 * send media to others, return -1, regardless of the internal media level.
	 */
	fun currentMediaLevel(): Int

	/**
	 * Returns <= 0 if this [ILinkable] can't accept any media, returns how much media
	 * it is accepting if it can accept media. If [otherMediaLevel] is -1, the [other]
	 * is requesting to know how much media your [ILinkable] can store, and you should return
	 * MAX_MEDIA - CURRENT_MEDIA.
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
	fun getLinked(index: Int): ILinkable? {
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
	fun receiveIota(sender: ILinkable, iota: Iota) {
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

	class LazyILinkable : LazyLoad<ILinkable?, CompoundTag>(null) { // default to empty compound tag
		override fun load(unloaded: CompoundTag, level: ServerLevel): ILinkable? = if (unloaded.isEmpty) null else LinkableRegistry.fromNbt(unloaded, level).getOrNull()
		override fun unload(loaded: ILinkable?) = if (loaded == null) CompoundTag() else LinkableRegistry.wrapNbt(loaded)

		companion object {
			fun from(linkable: ILinkable): LazyILinkable {
				val lazy = LazyILinkable()
				lazy.set(linkable)
				return lazy
			}
			fun from(tag: CompoundTag): LazyILinkable {
				val lazy = LazyILinkable()
				lazy.set(tag)
				return lazy
			}
		}
	}

	class LazyILinkableList {

		private val lazies: MutableList<LazyILinkable> = mutableListOf()
		private val loaded: MutableList<ILinkable?> = mutableListOf()

		fun add(linkable: ILinkable) {
			val lazy = LazyILinkable()
			lazy.set(linkable)
			lazies.add(lazy)
			loaded.add(linkable)
		}

		fun remove(linkable: ILinkable): Boolean {
			val tag = LinkableRegistry.wrapNbt(linkable)
			val idx = lazies.indexOfFirst { it.getUnloaded() == tag }
			if (idx == -1)
				return false

			lazies.removeAt(idx)
			loaded.remove(linkable)

			return true
		}

		fun contains(linkable: ILinkable): Boolean {
			return loaded.contains(linkable)
		}

		operator fun get(index: Int): ILinkable? = loaded[index]

		fun indexOf(linkable: ILinkable) = loaded.indexOf(linkable)

		fun size() = loaded.size

		fun tryLoad(level: ServerLevel): List<ILinkable> = lazies.mapIndexedNotNull { i, lazy ->
				val wasLoaded = loaded.size > i && loaded[i] != null
				if (loaded.size <= i) {
					loaded.add(lazy.get(level))

				} else if (loaded[i] == null) {
					loaded[i] = lazy.get(level)
				}
				if (wasLoaded && loaded[i] != null) loaded[i] else null
			}

		fun getLoaded() = loaded.filterNotNull()

		fun getUnloaded(): ListTag = lazies.map { it.getUnloaded() }.toNbtList()

		fun set(it: List<ILinkable>) {
			// get lazies to a list of LazyILinkables the same size as it.
			if (lazies.size > it.size)
				lazies.drop(lazies.size - it.size)
			if (lazies.size < it.size)
				lazies.addAll( (1 .. (it.size - lazies.size) ).map { LazyILinkable() } )


			it.forEachIndexed { i, linkable -> lazies[i].set(linkable) }
			loaded.clear()
			loaded.addAll(it)
		}

		fun set(it: ListTag) {
			// get lazies to a list of LazyILinkables the same size as it.
			if (lazies.size > it.size)
				lazies.drop(lazies.size - it.size)
			if (lazies.size < it.size)
				lazies.addAll( (1 .. (it.size - lazies.size) ).map { LazyILinkable() } )

			it.forEachIndexed { i, tag -> lazies[i].set(tag as CompoundTag) }
			loaded.clear()
			repeat(it.size) { loaded.add(null) }
		}
	}

	class LazyILinkableSet : MutableSet<ILinkable> {


		private val lazies: MutableSet<LazyILinkable> = mutableSetOf()
		private val loaded: MutableSet<ILinkable> = mutableSetOf()

		override val size: Int = loaded.size

		override fun add(element: ILinkable): Boolean {
			val lazy = LazyILinkable()
			lazy.set(element)
			loaded.add(element)
			return lazies.add(lazy)
		}

		fun add(element: LazyILinkable): Boolean = lazies.add(element)

		override fun addAll(elements: Collection<ILinkable>): Boolean {
			var anyAdded = false
			elements.forEach { anyAdded = anyAdded || add(it) }
			return anyAdded
		}

		override fun remove(element: ILinkable): Boolean {
			val tag = LinkableRegistry.wrapNbt(element)
			loaded.remove(element)
			return lazies.removeIf { it.getUnloaded() == tag }
		}

		/**
		 * Attempts to load elements of [lazies], and returns any elements that were newly loaded by this call.
		 */
		fun tryLoad(level: ServerLevel): Set<ILinkable> {
			val out = mutableSetOf<ILinkable>()
			lazies.mapNotNullTo(out) { lazy -> lazy.get(level)?.let { if (loaded.add(it)) it else null } }
			return out
		}

		fun getLoaded() = loaded

		fun getLazies() = lazies

		fun getUnloaded(): ListTag = lazies.map { it.getUnloaded() }.toNbtList()

		fun set(it: Set<ILinkable>) {
			clear()

			lazies.addAll(it.map { LazyILinkable.from(it) })
			loaded.addAll(it)
		}

		fun set(it: ListTag) {
			clear()

			lazies.addAll(it.map { LazyILinkable.from(it as CompoundTag) })
		}

		override fun clear() {
			lazies.clear()
			loaded.clear()
		}

		override fun isEmpty(): Boolean = loaded.isEmpty()

		override fun containsAll(elements: Collection<ILinkable>): Boolean = loaded.containsAll(elements)

		override fun contains(element: ILinkable): Boolean = loaded.contains(element)

		override fun iterator(): MutableIterator<ILinkable> = loaded.iterator()

		override fun retainAll(elements: Collection<ILinkable>): Boolean {
			val elementsSet = elements.toSet()
			val elementsUnloaded = mutableSetOf<CompoundTag>()
			elementsSet.mapTo(elementsUnloaded) { LinkableRegistry.wrapNbt(it) }

			loaded.retainAll(elementsSet)
			return lazies.removeIf { lazy -> lazy.toEither().map({ it !in elementsSet }, { it !in elementsUnloaded }) }
		}

		override fun removeAll(elements: Collection<ILinkable>): Boolean {
			val elementsSet = elements.toSet()
			val elementsUnloaded = mutableSetOf<CompoundTag>()
			elementsSet.mapTo(elementsUnloaded) { LinkableRegistry.wrapNbt(it) }

			loaded.removeAll(elementsSet)
			return lazies.removeIf { lazy -> lazy.toEither().map({ it in elementsSet }, { it in elementsUnloaded }) }
		}
	}

	companion object {
		const val MAX_RECEIVED_IOTAS = 64
	}
}