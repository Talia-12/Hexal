package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.utils.asCompound
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nbt.LazyLoad
import ram.talia.hexal.api.nbt.SerialisedIotaList
import ram.talia.hexal.api.nbt.toNbtList
import kotlin.math.max

@Suppress("PropertyName")
interface ILinkable<T : ILinkable<T>> {
	val asActionResult: List<Iota>

	val _level: Level
	var linked: MutableList<ILinkable<*>>
		get() {
			if (_level.isClientSide)
				throw Exception("LinkableEntity.linked should only be accessed on server.") // TODO: create and replace with ServerOnlyException
			return _lazyLinked!!.get()
		}
		set(value) {
			_lazyLinked?.set(value)
		}

	/**
	 * Initialise to null if [_level] is clientside, or as [LazyILinkableList] ([_level]) otherwise.
	 */
	val _lazyLinked: LazyILinkableList?

	/**
	 * Initialise to null if [_level] is clientside, or as [LazyILinkableList] ([_level]) otherwise.
	 */
	val _lazyRenderLinks: LazyILinkableList?

	/**
	 * Initialise to [SerialisedIotaList] (null)
	 */
	val serReceivedIotas: SerialisedIotaList

	/**
	 * Return the [ILinkable] as its type - E.g., an [ILinkable]<[ram.talia.hexal.common.entities.LinkableEntity]> would return LinkableEntity
	 */
	fun get(): T

	/**
	 * Return the registered LinkableType<T> for this [ILinkable], used to save/load the [ILinkable].
	 */
	fun getLinkableType(): LinkableRegistry.LinkableType<T, *>

	fun getPos(): Vec3

	fun maxSqrLinkRange(): Double

	fun isInRange(other: ILinkable<*>) = (this.getPos() - other.getPos()).lengthSqr() <= max(this.maxSqrLinkRange(), other.maxSqrLinkRange())

	/**
	 * Set to true if the link should be removed, e.g. the [ILinkable] has been discarded.
	 */
	fun shouldRemove(): Boolean

	/**
	 * Sync adding a render link in the serverside version of this Linkable to the clientside.
	 */
	fun syncAddRenderLink(other: ILinkable<*>)

	/**
	 * Sync removing a render link in the serverside version of this Linkable to the clientside.
	 */
	fun syncRemoveRenderLink(other: ILinkable<*>)

	fun writeToNbt(): Tag

	fun writeToSync(): Tag

	//region default implementations
	private fun addRenderLink(other: ILinkable<*>) {
		if (_level.isClientSide)
			throw Exception("LinkableEntity.addRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException

		_lazyRenderLinks!!.get().add(other)
		syncAddRenderLink(other)
	}

	private fun removeRenderLink(other: ILinkable<*>) {
		if (_level.isClientSide)
			throw Exception("LinkableEntity.removeRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException
		_lazyRenderLinks!!.get().remove(other)
		syncRemoveRenderLink(other)
	}

	fun removeRenderLink(index: Int) {
		if (_level.isClientSide)
			throw Exception("LinkableEntity.removeRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException
		val other = _lazyRenderLinks!!.get().removeAt(index)
		syncRemoveRenderLink(other)
	}

	fun link(other: ILinkable<*>, linkOther: Boolean = true) {
		if (_level.isClientSide) {
			HexalAPI.LOGGER.info("$this link called in a clientside context.")
			return
		}

		if (other in linked || (other == this))
			return

		HexalAPI.LOGGER.info("adding $other to $this's links.")
		linked.add(other)

		if (linkOther) {
			HexalAPI.LOGGER.info("adding $other to $this's render links.")
			addRenderLink(other)
		}

		if (linkOther) {
			other.link(this, false)
		}
	}

	fun unlink(other: ILinkable<*>, unlinkOther: Boolean = true) {
		if (_level.isClientSide) {
			HexalAPI.LOGGER.info("linkable $this had unlink called in a clientside context.")
			return
		}

		HexalAPI.LOGGER.info("unlinking $this from $other")

		linked.remove(other)
		removeRenderLink(other)

		if (unlinkOther) {
			other.unlink(this, false)
		}
	}

	fun getLinked(index: Int): ILinkable<*> {
		if (_level.isClientSide)
			throw Exception("linkable $this had getLinked called in a clientside context.") // TODO

		return linked[index]
	}

	fun getLinkedIndex(linked: ILinkable<*>): Int = this.linked.indexOf(linked)

	fun numLinked(): Int = linked.size

	/**
	 * This should be called every tick to remove links that should be removed (i.e. the entity that is linked to has been removed)
	 */
	fun checkLinks() {
		for (i in (linked.size - 1) downTo 0) {
			if (linked[i].shouldRemove() || !isInRange(linked[i]))
				unlink(linked[i])
		}
	}

	fun receiveIota(iota: Iota) {
		if (_level.isClientSide)
			throw Exception("BaseWisp.receiveIota should only be called on server.") // TODO

		serReceivedIotas.add(iota, _level as ServerLevel)
	}

	fun nextReceivedIota(): Iota {
		if (_level.isClientSide)
			throw Exception("BaseWisp.receiveIota should only be called on server.") // TODO

		return serReceivedIotas.pop(_level as ServerLevel) ?: NullIota()
	}

	fun numRemainingIota() = serReceivedIotas.size

	/**
	 * Called when the player is transmitting to this [ILinkable], should return what should be displayed instead of the stack.
	 */
	fun transmittingTargetReturnDisplay(): List<Component> = this.asActionResult.map(Iota::display)
	//endregion

	/**
	 * returned by [LinkableRegistry.fromSync] to let client renderers render a link to the render centre of a given [ILinkable].
	 */
	interface IRenderCentre {
		fun renderCentre(other: IRenderCentre, recursioning: Boolean = true): Vec3 // recursioning to stop a player linked to a player causing an infinite loop

		/**
		 * Set to true if the link should be removed, e.g. the [IRenderCentre] has been discarded.
		 */
		fun shouldRemove(): Boolean

		fun colouriser(): FrozenColorizer

		fun getLinkableType(): LinkableRegistry.LinkableType<*, *>
	}

	class LazyILinkable(val level: ServerLevel) : LazyLoad<ILinkable<*>?, CompoundTag>(null) { // default to empty compound tag
		override fun load(unloaded: CompoundTag): ILinkable<*>? = if (unloaded.isEmpty) null else LinkableRegistry.fromNbt(unloaded, level).getOrNull()
		override fun unload(loaded: ILinkable<*>?) = if (loaded == null) CompoundTag() else LinkableRegistry.wrapNbt(loaded)
	}

	class LazyILinkableList(val level: ServerLevel) : LazyLoad<MutableList<ILinkable<*>>, ListTag>(mutableListOf()) {
		override fun load(unloaded: ListTag): MutableList<ILinkable<*>> = unloaded.mapNotNull { LinkableRegistry.fromNbt(it.asCompound, level).getOrNull() } as MutableList
		override fun unload(loaded: MutableList<ILinkable<*>>) = loaded.map { LinkableRegistry.wrapNbt(it) }.toNbtList()

		override fun get(): MutableList<ILinkable<*>> = super.get()!!
	}
}