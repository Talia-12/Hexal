package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.Vec3Iota
import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.api.utils.asUUID
import at.petrak.hexcasting.api.utils.downcast
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay
import ram.talia.hexal.common.entities.LinkableEntity
import ram.talia.hexal.xplat.IXplatAbstractions

object LinkableTypes {
	val LINKABLE_ENTITY_TYPE = object : LinkableRegistry.LinkableType<LinkableEntity, LinkableEntity>(modLoc("linkable/entity")) {
		override fun toNbt(linkable: ILinkable): Tag = NbtUtils.createUUID((linkable as LinkableEntity).uuid)
		override fun fromNbt(tag: Tag, level: ServerLevel) = level.getEntity(tag.asUUID) as? LinkableEntity
		override fun toSync(linkable: ILinkable): Tag = IntTag.valueOf((linkable as LinkableEntity).id)
		override fun fromSync(tag: Tag, level: Level) = level.getEntity(tag.asInt) as? LinkableEntity
		override fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag) = (centre as LinkableEntity).id == tag.asInt
		override val canCast = true
		@Suppress("CAST_NEVER_SUCCEEDS")
		override fun linkableFromCastingContext(ctx: CastingContext): LinkableEntity? {
			val mCast = ctx as? IMixinCastingContext
			return mCast?.wisp
		}
		override val castingContextPriority = 0
		override fun linkableFromIota(iota: Iota, level: ServerLevel): LinkableEntity? = (iota as? EntityIota)?.entity as? LinkableEntity
		override val iotaPriority = 0
	}

	val PLAYER_LINKSTORE_TYPE = object : LinkableRegistry.LinkableType<PlayerLinkstore, PlayerLinkstore.RenderCentre>(modLoc("linkable/player")) {
		override fun toNbt(linkable: ILinkable): Tag = NbtUtils.createUUID((linkable as PlayerLinkstore).player.uuid)

		override fun fromNbt(tag: Tag, level: ServerLevel): PlayerLinkstore? {
			val player = level.getEntity(tag.asUUID) as? ServerPlayer ?: return null
			return IXplatAbstractions.INSTANCE.getLinkstore(player)
		}

		override fun toSync(linkable: ILinkable): Tag = IntTag.valueOf((linkable as PlayerLinkstore).player.id)

		override fun fromSync(tag: Tag, level: Level): PlayerLinkstore.RenderCentre? {
			val player = level.getEntity(tag.asInt) as? AbstractClientPlayer ?: return null
			return IXplatAbstractions.INSTANCE.getPlayerRenderCentre(player)
		}

		override fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag) = (centre as PlayerLinkstore.RenderCentre).player.id == tag.asInt
		override val canCast = true

		override fun linkableFromCastingContext(ctx: CastingContext)
			= IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)

		override val castingContextPriority = -100

		override fun linkableFromIota(iota: Iota, level: ServerLevel)
			= ((iota as? EntityIota)?.entity as? ServerPlayer)?.let { IXplatAbstractions.INSTANCE.getLinkstore(it) }

		override val iotaPriority = 0
	}

	val RELAY_TYPE = object : LinkableRegistry.LinkableType<BlockEntityRelay, BlockEntityRelay>(modLoc("linkable/relay")) {
		val TAG_X = "x"
		val TAG_Y = "y"
		val TAG_Z = "z"

		private fun toTag(relay: BlockEntityRelay): Tag {
			val ctag = CompoundTag()
			ctag.putInt(TAG_X, relay.pos.x)
			ctag.putInt(TAG_Y, relay.pos.y)
			ctag.putInt(TAG_Z, relay.pos.z)

			return ctag
		}

		override fun toNbt(linkable: ILinkable): Tag = toTag(linkable as BlockEntityRelay)
		override fun toSync(linkable: ILinkable): Tag = toTag(linkable as BlockEntityRelay)

		private fun fromTag(tag: Tag, level: Level): BlockEntityRelay? {
			val ctag = tag.downcast(CompoundTag.TYPE)

			if (!ctag.contains(TAG_X) || !ctag.contains(TAG_Y) || !ctag.contains(TAG_Z))
				return null

			return level.getBlockEntity(BlockPos(ctag.getInt(TAG_X), ctag.getInt(TAG_Y), ctag.getInt(TAG_Z))) as? BlockEntityRelay
		}

		override fun fromNbt(tag: Tag, level: ServerLevel) = fromTag(tag, level)

		override fun fromSync(tag: Tag, level: Level) = fromTag(tag, level)

		override fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag): Boolean {
			val ctag = tag.downcast(CompoundTag.TYPE)

			if (!ctag.contains(TAG_X) || !ctag.contains(TAG_Y) || !ctag.contains(TAG_Z))
				return false

			return (centre as BlockEntityRelay).pos == BlockPos(ctag.getInt(TAG_X), ctag.getInt(TAG_Y), ctag.getInt(TAG_Z))
		}

		override val canCast = false

		override fun linkableFromCastingContext(ctx: CastingContext) = null

		override val castingContextPriority = -10_000_000

		override fun linkableFromIota(iota: Iota, level: ServerLevel): BlockEntityRelay? = (iota as? Vec3Iota)?.let {
			 level.getBlockEntity(BlockPos(it.vec3)) as? BlockEntityRelay
		}

		override val iotaPriority = 0
	}
}