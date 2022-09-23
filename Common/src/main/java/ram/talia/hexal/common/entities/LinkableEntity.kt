package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.spellListOf
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.asList
import com.mojang.datafixers.util.Either
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
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.linkable.LinkableTypes
import ram.talia.hexal.api.spell.toNbtList
import java.util.ArrayList

abstract class LinkableEntity(entityType: EntityType<*>, level: Level) : Entity(entityType, level), ILinkable<LinkableEntity> {
	override val asSpellResult
		get() = spellListOf(this)

	var linked: MutableList<ILinkable<*>>
		get() {
			if (level.isClientSide)
				throw Exception("LinkableEntity.linked should only be accessed on server.") // TODO: create and replace with ServerOnlyException
			return lazyLinked!!.get()
		}
		set(value) {
			lazyLinked?.set(value)
		}

	private val lazyLinked: ILinkable.LazyILinkableList? = if (level.isClientSide) null else ILinkable.LazyILinkableList(level as ServerLevel)
	var renderLinks: MutableList<ILinkable<*>>
		get() {
			if (level.isClientSide)
				return entityData.get(RENDER_LINKS).get(TAG_RENDER_LINKS)?.asList?.mapNotNull { LinkableRegistry.fromSync(it.asCompound, level) } as MutableList?
					?: mutableListOf()
			return renderLinksList!!.get()
		}
		set(value) {
			if (level.isClientSide)
				return

			renderLinksList!!.set(value)

			syncRenderLinks()
		}

	private val renderLinksList: ILinkable.LazyILinkableList? = if (level.isClientSide) null else ILinkable.LazyILinkableList(level as ServerLevel)

	private fun syncRenderLinks() {
		if (level.isClientSide)
			throw Exception("LinkableEntity.syncRenderLinks should only be accessed on server.") // TODO: create and replace with ServerOnlyException

		val compound = CompoundTag()
		compound.put(TAG_RENDER_LINKS, renderLinksList!!.get().map { LinkableRegistry.wrapSync(it) }.toNbtList())
		entityData.set(RENDER_LINKS, compound)
	}

	private fun addRenderLink(other: ILinkable<*>) {
		renderLinks.add(other)
		syncRenderLinks()
	}

	private fun removeRenderLink(other: ILinkable<*>) {
		renderLinks.remove(other)
		syncRenderLinks()
	}

	fun removeRenderLink(index: Int) {
		renderLinks.removeAt(index)
		syncRenderLinks()
	}

	override fun get() = this

	override fun getLinkableType() = LinkableTypes.LINKABLE_ENTITY_TYPE

	override fun getPos() = position()

	override fun shouldRemove() = isRemoved && removalReason?.shouldDestroy() == true

	override fun link(other: ILinkable<*>, linkOther: Boolean) {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("wisp $uuid had linkWisp called in a clientside context.")
			return
		}

		if (other in linked || (other is LinkableEntity && this.uuid.equals(other.uuid)))
			return

		HexalAPI.LOGGER.info("adding $other to $uuid's links.")
		linked.add(other)

		if (linkOther) {
			HexalAPI.LOGGER.info("adding $other to $uuid's render links.")
			addRenderLink(other)
		}

		if (linkOther) {
			other.link(this, false)
		}
	}

	override fun unlink(other: ILinkable<*>, unlinkOther: Boolean) {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("linkable $uuid had unlink called in a clientside context.")
			return
		}

		HexalAPI.LOGGER.info("unlinking LinkableEntity $uuid from $other")

		linked.remove(other)
		removeRenderLink(other)

		if (unlinkOther) {
			other.unlink(this, false)
		}
	}

	override fun getLinked(index: Int): ILinkable<*> {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("linkable $uuid had getLinked called in a clientside context.")
			//TODO: throw error here
		}

		return linked[index]
	}

	override fun getLinkedIndex(linked: ILinkable<*>): Int {
		return this.linked.indexOf(linked)
	}

	override fun numLinked(): Int {
		return linked.size
	}

	override fun writeToNbt(): Tag {
		return NbtUtils.createUUID(uuid)
	}

	override fun writeToSync(): Tag {
		return IntTag.valueOf(id)
	}

	override fun tick() {
		super.tick()

		if (!level.isClientSide) {
			for (i in (linked.size - 1) downTo 0) {
				if (linked[i].shouldRemove())
					unlink(linked[i])
			}
		}
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		when (val linkedTag = compound.get(TAG_LINKED)) {
			null -> lazyLinked!!.set(mutableListOf())
			else -> lazyLinked!!.set(linkedTag as ListTag)
		}

		entityData.set(RENDER_LINKS, compound.get(TAG_RENDER_LINKS) as? CompoundTag ?: CompoundTag())
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		compound.put(TAG_LINKED, lazyLinked!!.getUnloaded())
		compound.put(TAG_RENDER_LINKS, entityData.get(RENDER_LINKS))
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