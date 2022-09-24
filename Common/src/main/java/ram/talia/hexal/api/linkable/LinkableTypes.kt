package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.api.utils.asUUID
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.common.entities.LinkableEntity
import ram.talia.hexal.xplat.IXplatAbstractions

object LinkableTypes {
	val LINKABLE_ENTITY_TYPE = object : LinkableRegistry.LinkableType<LinkableEntity, LinkableEntity>(modLoc("linkable/entity")) {
		override fun fromNbt(tag: Tag, level: ServerLevel) = level.getEntity(tag.asUUID) as? LinkableEntity
		override fun fromSync(tag: Tag, level: Level) = level.getEntity(tag.asInt) as? LinkableEntity
		override fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag) = (centre as LinkableEntity).id == tag.asInt
	}

	val PLAYER_LINKSTORE_TYPE = object : LinkableRegistry.LinkableType<PlayerLinkstore, PlayerLinkstore.RenderCentre>(modLoc("linkable/player")) {
		override fun fromNbt(tag: Tag, level: ServerLevel): PlayerLinkstore? {
			val player = level.getEntity(tag.asUUID) as? ServerPlayer ?: return null
			return IXplatAbstractions.INSTANCE.getLinkstore(player)
		}

		override fun fromSync(tag: Tag, level: Level): PlayerLinkstore.RenderCentre? {
			val player = level.getEntity(tag.asInt) as? AbstractClientPlayer ?: return null
			return PlayerLinkstore.RenderCentre(player)
		}

		override fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag) = (centre as PlayerLinkstore.RenderCentre).player.id == tag.asInt
	}
}