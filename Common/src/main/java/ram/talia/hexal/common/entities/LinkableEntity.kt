package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.asList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.linkable.LinkableTypes
import ram.talia.hexal.api.nbt.toNbtList

abstract class LinkableEntity(entityType: EntityType<*>, level: Level) : Entity(entityType, level), ILinkable<LinkableEntity>, ILinkable.IRenderCentre {
	override val asActionResult
		get() = listOf(EntityIota(this))
	override val _level = level

	override val _lazyLinked: ILinkable.LazyILinkableList?
		= if (level.isClientSide) null else ILinkable.LazyILinkableList(level as ServerLevel)
	val renderLinks: MutableList<ILinkable.IRenderCentre>
		get() {
			if (!level.isClientSide)
			throw Exception("LinkableEntity.renderLinks should only be accessed on client.") // TODO: create and replace with ClientOnlyException
			return entityData.get(RENDER_LINKS).get(TAG_RENDER_LINKS)?.asList?.mapNotNull { LinkableRegistry.fromSync(it.asCompound, level) } as MutableList? ?: mutableListOf()
		}

	override val _lazyRenderLinks: ILinkable.LazyILinkableList?
		= if (level.isClientSide) null else ILinkable.LazyILinkableList(level as ServerLevel)

	private fun syncRenderLinks() {
		if (level.isClientSide)
			throw Exception("LinkableEntity.syncRenderLinks should only be accessed on server.") // TODO: create and replace with ServerOnlyException

		val compound = CompoundTag()
		compound.put(TAG_RENDER_LINKS, _lazyRenderLinks!!.get().map { LinkableRegistry.wrapSync(it) }.toNbtList())
		entityData.set(RENDER_LINKS, compound)
	}

	override fun syncAddRenderLink(other: ILinkable<*>) = syncRenderLinks()

	override fun syncRemoveRenderLink(other: ILinkable<*>) = syncRenderLinks()

	override fun get() = this

	override fun getLinkableType(): LinkableRegistry.LinkableType<LinkableEntity, *> = LinkableTypes.LINKABLE_ENTITY_TYPE

	override fun getPos() = position()

	override fun shouldRemove() = isRemoved && removalReason?.shouldDestroy() == true

	override fun writeToNbt(): Tag = NbtUtils.createUUID(uuid)

	override fun writeToSync(): Tag = IntTag.valueOf(id)


	var isFirstTick = true
	override fun tick() {
		super.tick()

		if (level.isClientSide)
			return

		if (isFirstTick)
			syncRenderLinks()
		isFirstTick = false

		checkLinks()
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		when (val linkedTag = compound.get(TAG_LINKED) as? ListTag) {
			null -> _lazyLinked!!.set(mutableListOf())
			else -> _lazyLinked!!.set(linkedTag)
		}

		when (val renderLinkedTag = compound.get(TAG_RENDER_LINKS) as? ListTag) {
			null -> _lazyRenderLinks!!.set(mutableListOf())
			else -> _lazyRenderLinks!!.set(renderLinkedTag)
		}
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		compound.put(TAG_LINKED, _lazyLinked!!.getUnloaded())
		compound.put(TAG_RENDER_LINKS, _lazyRenderLinks!!.getUnloaded())
	}

	override fun defineSynchedData() {
		val compound = CompoundTag()
		compound.put(TAG_RENDER_LINKS, ListTag())
		entityData.define(RENDER_LINKS, CompoundTag())
	}


	companion object {
		val RENDER_LINKS: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(LinkableEntity::class.java, EntityDataSerializers.COMPOUND_TAG)

		const val TAG_LINKED = "linked"
		const val TAG_RENDER_LINKS = "render_link_list"
	}
}