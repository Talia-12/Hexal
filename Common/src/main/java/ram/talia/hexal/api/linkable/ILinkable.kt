package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.utils.asCompound
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.nbt.LazyLoad
import ram.talia.hexal.api.spell.toNbtList

interface ILinkable<T : ILinkable<T>> {
	val asSpellResult: List<SpellDatum<*>>

	/**
	 * Return the [ILinkable] as its type - E.g., an [ILinkable]<[ram.talia.hexal.common.entities.LinkableEntity]> would return LinkableEntity
	 */
	fun get(): T

	/**
	 * Return the registered LinkableType<T> for this [ILinkable], used to save/load the [ILinkable].
	 */
	fun getLinkableType(): LinkableRegistry.LinkableType<T>

	fun getPos(): Vec3

	/**
	 * If the [ILinkable]'s position is different to the place the link should be rendered to, override this to change where the link is rendered to.
	 */
	fun renderCentre() = getPos()

	/**
	 * Set to true if the link should be removed, e.g. the [ILinkable] has been discarded.
	 */
	fun shouldRemove(): Boolean

	fun link(other: ILinkable<*>, linkOther: Boolean = true)

	fun unlink(other: ILinkable<*>, unlinkOther: Boolean = true)

	fun getLinked(index: Int): ILinkable<*>

	fun getLinkedIndex(linked: ILinkable<*>): Int

	fun numLinked(): Int

	fun receiveIota(iota: SpellDatum<*>)

	fun nextReceivedIota(): SpellDatum<*>

	fun numRemainingIota(): Int

	fun writeToNbt(): Tag

	fun writeToSync(): Tag

	class LazyILinkable(val level: ServerLevel) : LazyLoad<ILinkable<*>, Tag>() {
		override fun load(unloaded: Tag): ILinkable<*>? = LinkableRegistry.fromNbt(unloaded.asCompound, level)
		override fun unload(loaded: ILinkable<*>) = LinkableRegistry.wrapNbt(loaded)
	}

	class LazyILinkableList(val level: ServerLevel) : LazyLoad<MutableList<ILinkable<*>>, ListTag>() {
		override fun load(unloaded: ListTag): MutableList<ILinkable<*>> = unloaded.mapNotNull { LinkableRegistry.fromNbt(it.asCompound, level) } as MutableList
		override fun unload(loaded: MutableList<ILinkable<*>>) = loaded.map { LinkableRegistry.wrapNbt(it) }.toNbtList()

		override fun get(): MutableList<ILinkable<*>> = either.map({ it }, { load(it) })
	}
}