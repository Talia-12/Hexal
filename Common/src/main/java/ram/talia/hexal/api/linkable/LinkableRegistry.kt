package ram.talia.hexal.api.linkable

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

object LinkableRegistry {
	const val TAG_TYPE = "type"
	const val TAG_LINKABLE = "linkable"

	private val linkableTypes: MutableMap<ResourceLocation, LinkableType<*>> = mutableMapOf()

	class RegisterLinkableTypeException(msg: String) : Exception(msg)
	class InvalidLinkableTypeException(msg: String) : Exception(msg)

	init {
		registerLinkableType(LinkableTypes.LinkableEntityType)
	}

	fun registerLinkableType(type: LinkableType<*>) {
		if (linkableTypes.containsKey(type.id))
			throw RegisterLinkableTypeException("LinkableRegistry already contains resource id ${type.id}")

		linkableTypes[type.id] = type
	}

	abstract class LinkableType<T : ILinkable<T>>(val id: ResourceLocation) {
		/**
		 * Takes a tag representing a reference to the [ILinkable] and wraps it inside a [CompoundTag] that also stores a reference to the [LinkableType] of the [ILinkable],
		 * meaning that the loader will know which [LinkableType] to use to restore the reference. This wrap is used to save a reference on saving/loading the world.
		 */
		fun wrapNbt(itTag: Tag): CompoundTag {
			val tag = CompoundTag()
			tag.putString(TAG_TYPE, "$id")
			tag.put(TAG_LINKABLE, itTag)
			return tag
		}

		/**
		 * Takes a tag containing a saved reference to a [ILinkable] of the type specified by this [LinkableType] and restores the reference. This is used to restore a
		 * reference on the server.
		 */
		abstract fun fromNbt(tag: Tag, level: ServerLevel): T?

		/**
		 * Takes a tag representing a reference to the [ILinkable] and wraps it inside a [CompoundTag] that also stores a reference to the [LinkableType] of the [ILinkable],
		 * meaning that the loader will know which [LinkableType] to use to restore the reference. This wrap is used to save a reference to be synced to the client.
		 */
		fun wrapSync(itTag: Tag): CompoundTag {
			val tag = CompoundTag()
			tag.putString(TAG_TYPE, "$id")
			tag.put(TAG_LINKABLE, itTag)
			return tag
		}

		/**
		 * Takes a tag containing a saved reference to a [ILinkable] of the type specified by this [LinkableType] and restores the reference. This is used to restore a
		 * reference on the client that has been synced from the server.
		 */
		abstract fun fromSync(tag: Tag, level: Level): T?
	}

	/**
	 * Accepts an [ILinkable] and returns a [CompoundTag] storing both the [ILinkable]'s type and a tag representing it, which can be loaded with [fromNbt]
	 */
	fun wrapNbt(linkable: ILinkable<*>) = linkable.getLinkableType().wrapNbt(linkable.writeToNbt())

	fun fromNbt(tag: CompoundTag, level: ServerLevel): ILinkable<*>? {
		val typeId = tag.getString(TAG_TYPE)
		if (!ResourceLocation.isValidResourceLocation(typeId))
			throw InvalidLinkableTypeException("$typeId is not a valid resource location")

		val type = linkableTypes[ResourceLocation(typeId)] ?: throw InvalidLinkableTypeException("no LinkableType registered for $typeId")

		return type.fromNbt(tag.get(TAG_LINKABLE)!!, level)
	}

	/**
	 * Accepts an [ILinkable] and returns a [CompoundTag] storing both the [ILinkable]'s type and a tag representing it, which can be loaded with [fromSync]
	 * [wrapSync] and [fromSync] are used to sync the link from Server to Client.
	 */
	fun wrapSync(linkable: ILinkable<*>) = linkable.getLinkableType().wrapSync(linkable.writeToSync())

	fun fromSync(tag: CompoundTag, level: Level): ILinkable<*>? {
		val typeId = tag.getString(TAG_TYPE)
		if (!ResourceLocation.isValidResourceLocation(typeId))
			throw InvalidLinkableTypeException("$typeId is not a valid resource location")

		val type = linkableTypes[ResourceLocation(typeId)] ?: throw InvalidLinkableTypeException("no LinkableType registered for $typeId")

		return type.fromSync(tag.get(TAG_LINKABLE)!!, level)
	}
}