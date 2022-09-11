package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.nbt.Tag
import net.minecraft.world.phys.Vec3

interface ILinkable<T : ILinkable<T>> {
	val asSpellResult: List<SpellDatum<*>>

	fun get(): T

	fun getLinkableType(): LinkableRegistry.LinkableType<T>

	fun getPos(): Vec3

	fun renderCentre() = getPos()

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