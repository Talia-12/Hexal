package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
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
	private val castingContextExtractionQueue: MutableList<LinkableType<*, *>> = mutableListOf()
//			= PriorityQueue { type0, type1 -> type1.castingContextPriority - type0.castingContextPriority }
	private val iotaExtractionQueue: MutableList<LinkableType<*, *>> = mutableListOf()
//			= PriorityQueue { type0, type1 -> type1.iotaPriority - type0.iotaPriority }

	class RegisterLinkableTypeException(msg: String) : Exception(msg)
	class InvalidLinkableTypeException(msg: String) : Exception(msg)
	class NullLinkableException(msg: String) : Exception(msg)

	init {
		registerLinkableType(LinkableTypes.LINKABLE_ENTITY_TYPE)
		registerLinkableType(LinkableTypes.PLAYER_LINKSTORE_TYPE)
		registerLinkableType(LinkableTypes.RELAY_TYPE)
	}

	fun registerLinkableType(type: LinkableType<*, *>) {
		if (linkableTypes.containsKey(type.id))
			throw RegisterLinkableTypeException("LinkableRegistry already contains resource id ${type.id}")

		if (type.canCast)
			addSorted(castingContextExtractionQueue, type) { type0, type1 -> type1.castingContextPriority - type0.castingContextPriority }
		addSorted(iotaExtractionQueue, type) { type0, type1 -> type1.iotaPriority - type0.iotaPriority }

		linkableTypes[type.id] = type
	}

	private fun<T> addSorted(list: MutableList<T>, toAdd: T, order: (T, T) -> Int) {
		var i = 0

		while (i < list.size && order(list[i], toAdd) < 0)
			i += 1

		list.add(i, toAdd)
	}

	abstract class LinkableType<T : ILinkable, U : ILinkable.IRenderCentre>(val id: ResourceLocation) {
		/**
		 * Takes an instance of this [ILinkable]'s type and returns a tag containing a saved reference to that
		 * [ILinkable] to be deserialised on the server.
		 */
		abstract fun toNbt(linkable: ILinkable): Tag

		/**
		 * Takes a tag representing a reference to the [ILinkable] and wraps it inside a [CompoundTag] that also stores
		 * a reference to the [LinkableType] of the [ILinkable], meaning that the loader will know which [LinkableType]
		 * to use to restore the reference. This wrap is used to save a reference on saving/loading the world.
		 */
		fun wrapNbt(itTag: Tag): CompoundTag {
			val tag = CompoundTag()
			tag.putString(TAG_TYPE, "$id")
			tag.put(TAG_LINKABLE, itTag)
			return tag
		}

		/**
		 * Takes a tag containing a saved reference to a [ILinkable] of the type specified by this [LinkableType] and
		 * restores the reference. This is used to restore a reference on the server.
		 */
		abstract fun fromNbt(tag: Tag, level: ServerLevel): T?

		/**
		 * Takes an instance of this [ILinkable]'s type and returns a tag containing a saved reference to that
		 * [ILinkable] to be deserialised on the client.
		 */
		abstract fun toSync(linkable: ILinkable): Tag

		/**
		 * Takes a tag representing a reference to the [ILinkable.IRenderCentre] and wraps it inside a [CompoundTag]
		 * that also stores a reference to the [LinkableType] of the [ILinkable], meaning that the loader will know
		 * which [LinkableType] to use to restore the reference. This wrap is used to save a reference to be synced to
		 * the client for rendering the line to the centre of that [ILinkable].
		 */
		fun wrapSync(itTag: Tag): CompoundTag {
			val tag = CompoundTag()
			tag.putString(TAG_TYPE, "$id")
			tag.put(TAG_LINKABLE, itTag)
			return tag
		}

		/**
		 * Takes a tag containing a saved reference to the [ILinkable.IRenderCentre] of the type specified by this
		 * [LinkableType] and restores the reference. This is used to render to the centre of an [ILinkable] on the
		 * client.
		 */
		abstract fun fromSync(tag: Tag, level: Level): U?

		/**
		 * Takes in an [ILinkable.IRenderCentre] and a [Tag] and returns whether that [Tag] is a reference to that
		 * [ILinkable.IRenderCentre]. Used to determine which [ILinkable.IRenderCentre]s to remove on the client, since
		 * e.g. getEntity won't work if the entity's been removed.
		 */
		abstract fun matchSync(centre: ILinkable.IRenderCentre, tag: Tag): Boolean

		/**
		 * Set to true if this ILinkable can cast hexes, and false otherwise.
		 */
		abstract val canCast: Boolean

		/**
		 * Takes in a [CastingEnvironment] and returns an [ILinkable] if an [ILinkable] of type [T] is the caster of the
		 * [CastingEnvironment], and null otherwise (where, e.g., a wisp or a player may be the 'caster' here).
		 */
		abstract fun linkableFromCastingContext(env: CastingEnvironment): T?

		/**
		 * An [Int] representing how high priority this type of [ILinkable] should be when extracting an [ILinkable]
		 * from a [CastingEnvironment]. (Lower number means lower priority)
		 */
		abstract val castingContextPriority: Int

		/**
		 * Takes in an [Iota] and returns an [ILinkable] if an [ILinkable] of type [T] is referenced by that
		 * iota, and null otherwise.
		 */
		abstract fun linkableFromIota(iota: Iota, level: ServerLevel): T?

		/**
		 * An [Int] representing how high priority this type of [ILinkable] should be when extracting an [ILinkable]
		 * from an [Iota]. (Lower number means lower priority)
		 */
		abstract val iotaPriority: Int
	}

	/**
	 * Accepts an [ILinkable] and returns a [CompoundTag] storing both the [ILinkable]'s type and a tag representing it, which can be loaded with [fromNbt]
	 */
	@JvmStatic
	fun wrapNbt(linkable: ILinkable): CompoundTag {
		val type = linkable.getLinkableType()
		return type.wrapNbt(type.toNbt(linkable))
	}

	@JvmStatic
	fun fromNbt(tag: CompoundTag, level: ServerLevel): Result<ILinkable> {
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
	fun wrapSync(linkable: ILinkable): CompoundTag {
		val type = linkable.getLinkableType()
		return type.wrapSync(type.toSync(linkable))
	}

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

	@JvmStatic
	fun linkableFromCastingEnvironment(env: CastingEnvironment): ILinkable {
		castingContextExtractionQueue.forEach { type -> type.linkableFromCastingContext(env)?.let { return it } }
		throw Exception("At least one type should have accepted the env and returned itself (namely the player type).")
	}

	@JvmStatic
	fun linkableFromIota(iota: Iota, level: ServerLevel): ILinkable? {
		iotaExtractionQueue.forEach { type -> type.linkableFromIota(iota, level)?.let { return it } }
		return null
	}
}