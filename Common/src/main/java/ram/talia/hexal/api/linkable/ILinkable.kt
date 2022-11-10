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
import kotlin.math.max

interface ILinkable<T : ILinkable<T>> {
	val asActionResult: List<Iota>

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

	fun link(other: ILinkable<*>, linkOther: Boolean = true)

	fun unlink(other: ILinkable<*>, unlinkOther: Boolean = true)

	fun getLinked(index: Int): ILinkable<*>

	fun getLinkedIndex(linked: ILinkable<*>): Int

	fun numLinked(): Int

	fun receiveIota(iota: Iota)

	fun nextReceivedIota(): Iota

	fun numRemainingIota(): Int

	/**
	 * Called when the player is transmitting to this [ILinkable], should return what should be displayed instead of the stack.
	 */
	fun transmittingTargetReturnDisplay(): List<Component> = this.asActionResult.map(Iota::display)

	fun writeToNbt(): Tag

	fun writeToSync(): Tag

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