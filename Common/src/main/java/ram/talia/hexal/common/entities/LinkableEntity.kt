package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import ram.talia.hexal.api.spell.toIotaList

abstract class LinkableEntity(entityType: EntityType<*>, level: Level) : Entity(entityType, level) {
	abstract fun link(other: LinkableEntity, linkOther: Boolean = true)

	abstract fun unlink(other: LinkableEntity, unlinkOther: Boolean = true)

	abstract fun getLinked(index: Int): LinkableEntity

	abstract fun numLinked(): Int

	abstract fun receiveIota(iota: SpellDatum<*>)

	abstract fun nextReceivedIota(): SpellDatum<*>

	abstract fun numRemainingIota(): Int
}