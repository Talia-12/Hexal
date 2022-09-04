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
			resolveLinked()
			return linkedEither.left().get()
		}
		set(value) {
			linkedEither = Either.left(value)
		}

	private var linkedEither: Either<MutableList<ILinkable<*>>, ListTag> = Either.left(ArrayList())
	var renderLinks: MutableList<ILinkable<*>>
		get() {
			if (level.isClientSide)
				return entityData.get(RENDER_LINKS).get(TAG_RENDER_LINKS)?.asList?.mapNotNull { LinkableRegistry.fromSync(it.asCompound, level) } as MutableList? ?: mutableListOf()
			return renderLinksList.map({ it }, { listTag -> listTag.mapNotNull { LinkableRegistry.fromNbt(it.asCompound, level as ServerLevel) } as MutableList })
		}
		set(value) {
			if (level.isClientSide)
				return

			renderLinksList = Either.left(value)

			syncRenderLinks()
		}

	private var renderLinksList: Either<MutableList<ILinkable<*>>, ListTag> = Either.left(ArrayList())

	private fun syncRenderLinks() {
		val compound = CompoundTag()
		val rRenderLinksList = renderLinksList.map({ it }, { listTag -> listTag.mapNotNull { LinkableRegistry.fromSync(it.asCompound, level as ServerLevel) } })
		compound.put(TAG_RENDER_LINKS, rRenderLinksList.map { LinkableRegistry.wrapSync(it) }.toNbtList())
		entityData.set(RENDER_LINKS, compound)
	}

	private fun resolveLinked() {
		linkedEither = Either.left(linkedEither.map({ it }, { listTag -> listTag.mapNotNull { LinkableRegistry.fromNbt(it.asCompound, level as ServerLevel) } as MutableList }))
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

	override fun getLinkableType() = LinkableTypes.LinkableEntityType

	override fun getPos() = position()

	override fun link(other: ILinkable<*>, linkOther: Boolean) {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("wisp $uuid had linkWisp called in a clientside context.")
			return
		}

		if (other in linked)
			return

		HexalAPI.LOGGER.info("adding $other to $uuid's links.")
		linked.add(other)

		if (linkOther) {
			HexalAPI.LOGGER.info("adding $other to $uuid's render links.")
			addRenderLink(other)
		}

		if (linkOther) {
			link(this, false)
		}
	}

	override fun unlink(other: ILinkable<*>, unlinkOther: Boolean) {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("linkable $uuid had unlink called in a clientside context.")
			return
		}

		linked.remove(other)
		removeRenderLink(other)

		if (unlinkOther) {
			unlink(this, false)
		}
	}

	override fun getLinked(index: Int): ILinkable<*> {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("linkable $uuid had getLinked called in a clientside context.")
			//TODO: throw error here
		}

		return linked[index]
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

		if (isRemoved && removalReason?.shouldDestroy() == true)
			linked.forEach { unlink(it) }
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		linkedEither = Either.right(compound.get(TAG_LINKED) as ListTag)
		entityData.set(RENDER_LINKS, compound.get(TAG_RENDER_LINKS) as CompoundTag)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		compound.put(TAG_LINKED, linkedEither.map({ linkables -> linkables.map { LinkableRegistry.wrapNbt(it) }.toNbtList() }, { it }))
		compound.put(TAG_RENDER_LINKS, entityData.get(RENDER_LINKS))
	}

	override fun defineSynchedData() {
		val compound = CompoundTag()
		compound.put(TAG_RENDER_LINKS, ListTag())
		entityData.define(RENDER_LINKS, CompoundTag())
	}


		companion object {
		val RENDER_LINKS: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)

		const val TAG_LINKED = "linked"
		const val TAG_RENDER_LINKS = "render_link_list"
	}
}