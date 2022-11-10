package ram.talia.hexal.api.linkable

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

object LinkableRegistry {
	const val TAG_TYPE = "type"
	const val TAG_LINKABLE = "linkable"

	// TODO: Refactor to use Minecraft Registry class
	private val linkableTypes: MutableMap<ResourceLocation, LinkableType<*, *>> = mutableMapOf()

	class RegisterLinkableTypeException(msg: String) : Exception(msg)
	class InvalidLinkableTypeException(msg: String) : Exception(msg)
	class NullLinkableException(msg: String) : Exception(msg)

	init {
		registerLinkableType(LinkableTypes.LINKABLE_ENTITY_TYPE)
		registerLinkableType(LinkableTypes.PLAYER_LINKSTORE_TYPE)
	}

	fun registerLinkableType(type: LinkableType<*, *>) {
		if (linkableTypes.containsKey(type.id))
			throw RegisterLinkableTypeException("LinkableRegistry already contains resource id ${type.id}")

		linkableTypes[type.id] = type
	}

	abstract class LinkableType<T : ILinkable<T>, U : ILinkable.IRenderCentre>(val id: ResourceLocation) {
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
		 * Takes a tag representing a reference to the [ILinkable.IRenderCentre] and wraps it inside a [CompoundTag] that also stores a
		 * reference to the [LinkableType] of the [ILinkable], meaning that the loader will know which [LinkableType] to use to restore the
		 * reference. This wrap is used to save a reference to be synced to the client for rendering the line to the centre of that
		 * [ILinkable].
		 */
		fun wrapSync(itTag: Tag): CompoundTag {
			val tag = CompoundTag()
			tag.putString(TAG_TYPE, "$id")
			tag.put(TAG_LINKABLE, itTag)
			return tag
		}

		/**
		 * Takes a tag containing a saved reference to the [ILinkable.IRenderCentre] of the type specified by this [LinkableType] and
		 * restores the reference. This is used to render to the centre of an [ILinkable] on the client.
		 */
		abstract fun fromSync(tag: Tag, level: Level): U?

		/**
		 * Takes in an [ILinkable.IRenderCentre] and a [Tag] and returns whether that [Tag] is a reference to that [ILinkable.IRenderCentre]. Used to determine which
		 * [ILinkable.IRenderCentre]s to remove on the client, since e.g. getEntity won't work if the entity's been removed.
		 */
		internal abstract fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag): Boolean
	}

	/**
	 * Accepts an [ILinkable] and returns a [CompoundTag] storing both the [ILinkable]'s type and a tag representing it, which can be loaded with [fromNbt]
	 */
	@JvmStatic
	fun wrapNbt(linkable: ILinkable<*>) = linkable.getLinkableType().wrapNbt(linkable.writeToNbt())

	@JvmStatic
	fun fromNbt(tag: CompoundTag, level: ServerLevel): Result<ILinkable<*>> {
		val typeId = tag.getString(TAG_TYPE)
		if (!ResourceLocation.isValidResourceLocation(typeId))
			return Result.failure(InvalidLinkableTypeException("$typeId is not a valid resource location"))

		val type = linkableTypes[ResourceLocation(typeId)] ?: return Result.failure(InvalidLinkableTypeException("$typeId is not a valid resource location"))

		return when (val linkable = type.fromNbt(tag.get(TAG_LINKABLE)!!, level)) {
			null -> Result.failure(NullLinkableException("linkable for $tag returned null."))
			else -> Result.success(linkable)
		}
	}

	/**
	 * Accepts an [ILinkable] and returns a [CompoundTag] storing both the [ILinkable]'s type and a tag representing it, which can be
	 * loaded with [fromSync]. [wrapSync] and [fromSync] are used to sync the link from Server to Client.
	 */
	@JvmStatic
	fun wrapSync(linkable: ILinkable<*>) = linkable.getLinkableType().wrapSync(linkable.writeToSync())

	@JvmStatic
	fun fromSync(tag: CompoundTag, level: Level): ILinkable.IRenderCentre? {
		val typeId = tag.getString(TAG_TYPE)
		if (!ResourceLocation.isValidResourceLocation(typeId))
			throw InvalidLinkableTypeException("$typeId is not a valid resource location")

		val type = linkableTypes[ResourceLocation(typeId)] ?: throw InvalidLinkableTypeException("no LinkableType registered for $typeId")

		return type.fromSync(tag.get(TAG_LINKABLE)!!, level)
	}

	@JvmStatic
	fun matchSync(centre: ILinkable.IRenderCentre, tag: CompoundTag): Boolean {
		val typeId = tag.getString(TAG_TYPE)
		if (!ResourceLocation.isValidResourceLocation(typeId))
			throw InvalidLinkableTypeException("$typeId is not a valid resource location")

		val type = linkableTypes[ResourceLocation(typeId)] ?: throw InvalidLinkableTypeException("no LinkableType registered for $typeId")

		return type == centre.getLinkableType() && type.matchSync(centre, tag.get(TAG_LINKABLE)!!)
	}
}