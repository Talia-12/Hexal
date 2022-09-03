package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.api.utils.asUUID
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.LinkableEntity

object LinkableTypes {
	val LinkableEntityType = object : LinkableRegistry.LinkableType<LinkableEntity>(HexalAPI.modLoc("linkable/entity")) {
		override fun fromNbt(tag: Tag, level: ServerLevel): LinkableEntity? {
			return level.getEntity(tag.asUUID) as? LinkableEntity
		}

		override fun fromSync(tag: Tag, level: Level): LinkableEntity? {
			return level.getEntity(tag.asInt) as? LinkableEntity
		}
	}
}