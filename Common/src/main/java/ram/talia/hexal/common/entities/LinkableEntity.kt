package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.spellListOf
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
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.linkable.LinkableTypes
import ram.talia.hexal.api.spell.toNbtList

abstract class LinkableEntity(entityType: EntityType<*>, level: Level) : Entity(entityType, level), ILinkable<LinkableEntity>, ILinkable.IRenderCentre {
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
	var renderLinks: MutableList<ILinkable.IRenderCentre>
		get() {
			if (!level.isClientSide)
			throw Exception("LinkableEntity.renderLinks should only be accessed on client.") // TODO: create and replace with ClientOnlyException
			return entityData.get(RENDER_LINKS).get(TAG_RENDER_LINKS)?.asList?.mapNotNull { LinkableRegistry.fromSync(it.asCompound, level) } as MutableList? ?: mutableListOf()
		}
		set(value) { }

	private val lazyRenderLinks: ILinkable.LazyILinkableList? = if (level.isClientSide) null else ILinkable.LazyILinkableList(level as ServerLevel)

	private fun syncRenderLinks() {
		if (level.isClientSide)
			throw Exception("LinkableEntity.syncRenderLinks should only be accessed on server.") // TODO: create and replace with ServerOnlyException

		val compound = CompoundTag()
		compound.put(TAG_RENDER_LINKS, lazyRenderLinks!!.get().map { LinkableRegistry.wrapSync(it) }.toNbtList())
		entityData.set(RENDER_LINKS, compound)
	}

	private fun addRenderLink(other: ILinkable<*>) {
		if (level.isClientSide)
			throw Exception("LinkableEntity.addRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException

		lazyRenderLinks!!.get().add(other)
		syncRenderLinks()
	}

	private fun removeRenderLink(other: ILinkable<*>) {
		if (level.isClientSide)
			throw Exception("LinkableEntity.removeRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException
		lazyRenderLinks!!.get().remove(other)
		syncRenderLinks()
	}

	fun removeRenderLink(index: Int) {
		if (level.isClientSide)
			throw Exception("LinkableEntity.removeRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException
		lazyRenderLinks!!.get().removeAt(index)
		syncRenderLinks()
	}

	override fun get() = this

	override fun getLinkableType(): LinkableRegistry.LinkableType<LinkableEntity, *> = LinkableTypes.LINKABLE_ENTITY_TYPE

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

	override fun numLinked() = linked.size

	override fun writeToNbt(): Tag = NbtUtils.createUUID(uuid)

	override fun writeToSync(): Tag = IntTag.valueOf(id)

	override fun tick() {
		super.tick()

		if (!level.isClientSide) {
			for (i in (linked.size - 1) downTo 0) {
				if (linked[i].shouldRemove() || !isInRange(linked[i]))
					unlink(linked[i])
			}
		}
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		when (val linkedTag = compound.get(TAG_LINKED) as? ListTag) {
			null -> lazyLinked!!.set(mutableListOf())
			else -> lazyLinked!!.set(linkedTag)
		}

		when (val renderLinkedTag = compound.get(TAG_RENDER_LINKS) as? ListTag) {
			null -> lazyRenderLinks!!.set(mutableListOf())
			else -> lazyRenderLinks!!.set(renderLinkedTag)
		}

		val renderLinkedCompound = CompoundTag()
		renderLinkedCompound.put(TAG_RENDER_LINKS, lazyRenderLinks.get().map { LinkableRegistry.wrapSync(it) }.toNbtList())
		entityData.set(RENDER_LINKS, renderLinkedCompound)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		compound.put(TAG_LINKED, lazyLinked!!.getUnloaded())
		compound.put(TAG_RENDER_LINKS, lazyRenderLinks!!.getUnloaded())
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