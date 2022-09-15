package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.nbt.Tag
import net.minecraft.world.phys.Vec3

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
}