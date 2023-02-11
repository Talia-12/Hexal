package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.iota.EntityIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.linkable.*

abstract class LinkableEntity(entityType: EntityType<*>, level: Level) : Entity(entityType, level), ILinkable, ILinkable.IRenderCentre {
	override val asActionResult
		get() = listOf(EntityIota(this))

	override val linkableHolder = if (level.isClientSide) null else ServerLinkableHolder(this, level as ServerLevel)
	override val clientLinkableHolder = if (level.isClientSide) ClientLinkableHolder(this, level, random) else null

	override fun getLinkableType(): LinkableRegistry.LinkableType<LinkableEntity, *> = LinkableTypes.LINKABLE_ENTITY_TYPE

	override fun getPosition(): Vec3 = position()

	override fun shouldRemove() = isRemoved && removalReason?.shouldDestroy() == true

	override fun writeToNbt(): Tag = NbtUtils.createUUID(uuid)

	override fun writeToSync(): Tag = IntTag.valueOf(id)

	override fun tick() {
		super.tick()

		if (level.isClientSide) {
			clientLinkableHolder!!.renderLinks()
			return
		}

		checkLinks()
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		(compound.get(TAG_LINKABLE_HOLDER) as? CompoundTag)?.let {
			linkableHolder!!.readFromNbt(it)
		}
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		compound.put(TAG_LINKABLE_HOLDER, linkableHolder!!.writeToNbt())
	}

	override fun getAddEntityPacket(): Packet<*> {
		// TODO: not very efficient, sends all players tracking the entity a new packet every time someone else starts tracking it; would be better to only send to the one new tracker.
		linkableHolder!!.syncAll()
		return ClientboundAddEntityPacket(this)
	}

	companion object {
		const val TAG_LINKABLE_HOLDER = "hexal:linkable_holder"
	}
}