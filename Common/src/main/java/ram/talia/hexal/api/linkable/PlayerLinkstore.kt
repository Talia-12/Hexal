package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

class PlayerLinkstore(val player: ServerPlayer) : ILinkable<PlayerLinkstore> {
	override val asSpellResult = listOf(SpellDatum.make(player))

	override fun get() = this

	override fun getLinkableType() = LinkableTypes.PLAYER_LINKSTORE_TYPE

	override fun getPos() = player.position()

	override fun shouldRemove(): Boolean {
		TODO("Not yet implemented")
	}

	override fun link(other: ILinkable<*>, linkOther: Boolean) {
		TODO("Not yet implemented")
	}

	override fun unlink(other: ILinkable<*>, unlinkOther: Boolean) {
		TODO("Not yet implemented")
	}

	override fun getLinked(index: Int): ILinkable<*> {
		TODO("Not yet implemented")
	}

	override fun getLinkedIndex(linked: ILinkable<*>): Int {
		TODO("Not yet implemented")
	}

	override fun numLinked(): Int {
		TODO("Not yet implemented")
	}

	override fun receiveIota(iota: SpellDatum<*>) {
		TODO("Not yet implemented")
	}

	override fun nextReceivedIota(): SpellDatum<*> {
		TODO("Not yet implemented")
	}

	override fun numRemainingIota(): Int {
		TODO("Not yet implemented")
	}

	override fun writeToNbt(): Tag {
		TODO("Not yet implemented")
	}

	override fun writeToSync(): Tag {
		TODO("Not yet implemented")
	}
}