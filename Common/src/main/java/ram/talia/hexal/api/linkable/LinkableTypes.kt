package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.api.utils.asUUID
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.common.entities.LinkableEntity
import ram.talia.hexal.xplat.IXplatAbstractions

object LinkableTypes {
	val LINKABLE_ENTITY_TYPE = object : LinkableRegistry.LinkableType<LinkableEntity, LinkableEntity>(modLoc("linkable/entity")) {
		override fun fromNbt(tag: Tag, level: ServerLevel) = level.getEntity(tag.asUUID) as? LinkableEntity
		override fun fromSync(tag: Tag, level: Level) = level.getEntity(tag.asInt) as? LinkableEntity
		override fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag) = (centre as LinkableEntity).id == tag.asInt
		override val canCast = true
		@Suppress("CAST_NEVER_SUCCEEDS")
		override fun linkableFromCastingContext(ctx: CastingContext): LinkableEntity? {
			val mCast = ctx as? IMixinCastingContext
			return mCast?.wisp
		}
		override val castingContextPriority = 0
		override fun linkableFromIota(iota: Iota): LinkableEntity? = (iota as? EntityIota)?.entity as? LinkableEntity
		override val iotaPriority = 0
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
		override val canCast = true

		override fun linkableFromCastingContext(ctx: CastingContext)
			= IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)

		override val castingContextPriority = -100

		override fun linkableFromIota(iota: Iota)
			= ((iota as? EntityIota)?.entity as? ServerPlayer)?.let { IXplatAbstractions.INSTANCE.getLinkstore(it) }

		override val iotaPriority = 0
	}
}