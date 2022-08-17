package ram.talia.hexal.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.spell.toIotaList
import ram.talia.hexal.api.spell.toNbtList
import ram.talia.hexal.common.entities.BaseLemma
import java.util.*
import kotlin.collections.ArrayList

class LemmaCastingManager(private val caster: ServerPlayer) {

	val queue: PriorityQueue<LemmaCast> = PriorityQueue()

	/**
	 * Schedule a given cast to be added to this LemmaCastingManager's priority queue. It will be evaluated in the next tick unless the player is doing something that
	 * is producing a lot of Wisp casts. Higher [priority] casts will always be executed first - between casts of equal [priority], the first one added to the stack is
	 * preferred.
	 */
	fun scheduleCast(
		wisp: BaseLemma,
		priority: Int,
		hex: List<SpellDatum<*>>,
		initialStack: MutableList<SpellDatum<*>> = ArrayList<SpellDatum<*>>().toMutableList(),
		initialRavenmind: SpellDatum<*> = SpellDatum.make(Widget.NULL),
	) {
		val cast = LemmaCast(wisp, priority, caster.level.gameTime, hex, initialStack, initialRavenmind)

		// if the wisp is one that is hard enough to forkbomb with (specifically, lasting wisps), let it go through without reaching the queue
		if (specialHandlers.any { handler -> handler.invoke(this, cast) })
			return

		queue.add(cast)
	}

	/**
	 * Called by CCWispCastingManager (Fabric) and XXX (Forge) each tick, evaluates up to WISP_EVALS_PER_TICK Wisp casts (letting through any handled by specialHandlers
	 * without decrementing the counter).
	 */
	fun executeCasts() {
		if (caster.level.isClientSide) {
			HexalAPI.LOGGER.info("HOW DID THIS HAPPEN")
			return
		}

//		if (queue.size > 0) {
//			HexalAPI.LOGGER.info("player ${caster.uuid} is executing up to $WISP_EVALS_PER_TICK of ${queue.size} on tick ${caster.level.gameTime}")
//		}

		var evalsLeft = LEMMA_EVALS_PER_TICK

		val itr = queue.iterator()

		val results = ArrayList<LemmaCastResult>()

		while (evalsLeft > 0 && itr.hasNext()) {
			val cast = itr.next()
			itr.remove()

			// if the wisp isn't chunkloaded at the moment, delete it from the queue (this is a small enough edge case I can't be bothered robustly handling it)
			if (cast.lemma == null) {
				cast.lemma = (caster.level as ServerLevel).getEntity(cast.lemmaUUID) as BaseLemma

				if (cast.lemma == null) continue
			}

			results += cast(cast)

			evalsLeft--
		}

		results.forEach { result -> result.callback() }
	}

	/**
	 * Actually executes the cast described in [cast]. Will throw a NullPointerException if it somehow got here with [cast] == null.
	 */
	public fun cast(cast: LemmaCast): LemmaCastResult {
		val ctx = CastingContext(
			caster,
			InteractionHand.MAIN_HAND
		)

		val wisp = cast.lemma!!

		// IntelliJ is complaining that ctx will never be an instance of MixinCastingContextInterface cause it doesn't know about mixin, but we know better
		val mCast = ctx as? MixinCastingContextInterface
		mCast?.wisp = wisp

		val harness = CastingHarness(ctx)

		harness.stack = cast.initialStack
		harness.localIota = cast.initialRavenmind

		val info = harness.executeIotas(cast.hex, caster.getLevel())

		// the wisp will have things it wants to do once the cast is successful, so a callback on it is called to let it know that happened, and what the end state of the
		// stack and ravenmind is. This is returned and added to a list that [executeCasts] will loop over to hopefully prevent concurrent modification problems.
		return LemmaCastResult(wisp, info.makesCastSound, harness.stack, harness.localIota)
	}

	fun readFromNbt(tag: CompoundTag, level: ServerLevel) {
		val numCasts = tag.getInt(TAG_NUM_CASTS)

		val list = tag.getList(TAG_CAST_LIST, numCasts)

		for (castTag in list) {
			queue.add(LemmaCast.makeFromNbt(castTag.asCompound, level))
		}
	}

	fun writeToNbt(tag: CompoundTag) {
		tag.putInt(TAG_NUM_CASTS, queue.size)

		val list = ListTag()

		for (cast in queue) {
			list.add(cast.writeToNbt())
		}

		tag.putList(TAG_CAST_LIST, list)
	}

	data class LemmaCast(
		val lemmaUUID: UUID,
		val priority: Int,
		val timeAdded: Long,
		val hex: List<SpellDatum<*>>,
		val initialStack: MutableList<SpellDatum<*>> = ArrayList<SpellDatum<*>>().toMutableList(),
		val initialRavenmind: SpellDatum<*> = SpellDatum.make(Widget.NULL),
	) : Comparable<LemmaCast> {
		/**
		 * when loading from NBT, it calls ServerLevel.entity(UUID), which could return null.
		 */
		var lemma: BaseLemma? = null

		constructor(
			lemma: BaseLemma,
			priority: Int,
			timeAdded: Long,
			hex: List<SpellDatum<*>>,
			initialStack: MutableList<SpellDatum<*>>,
			initialRavenmind: SpellDatum<*>
		) : this(lemma.uuid, priority, timeAdded, hex, initialStack, initialRavenmind) {
			this.lemma = lemma
		}

		override fun compareTo(other: LemmaCast): Int {
			if (priority != other.priority)
				return priority - other.priority
			return (timeAdded - other.timeAdded).toInt()
		}

		fun writeToNbt(): CompoundTag {
			val tag = CompoundTag()

			tag.putUUID(TAG_LEMMA, lemmaUUID)
			tag.putInt(TAG_PRIORITY, priority)
			tag.putLong(TAG_TIME_ADDED, timeAdded)
			tag.putInt(TAG_HEX_LENGTH, hex.size)
			tag.putList(TAG_HEX, hex.toNbtList())
			tag.putInt(TAG_INITIAL_STACK_LENGTH, initialStack.size)
			tag.putList(TAG_INITIAL_STACK, initialStack.toNbtList())
			tag.putCompound(TAG_INITIAL_RAVENMIND, initialRavenmind.serializeToNBT())

			return tag
		}

		companion object {
			const val TAG_LEMMA = "wisp"
			const val TAG_PRIORITY = "priority"
			const val TAG_TIME_ADDED = "time_added"
			const val TAG_HEX_LENGTH = "hex_length"
			const val TAG_HEX = "hex"
			const val TAG_INITIAL_STACK_LENGTH = "initial_stack_length"
			const val TAG_INITIAL_STACK = "initial_stack"
			const val TAG_INITIAL_RAVENMIND = "initial_ravenmind"

			fun makeFromNbt(tag: CompoundTag, level: ServerLevel): LemmaCast {
				val wispUUID = tag.getUUID(TAG_LEMMA)
				val lemma: BaseLemma? = level.getEntity(wispUUID) as? BaseLemma

				if (lemma != null) {
					return LemmaCast(
						lemma,
						tag.getInt(TAG_PRIORITY),
						tag.getLong(TAG_TIME_ADDED),
						tag.getList(TAG_HEX, tag.getInt(TAG_HEX_LENGTH)).toIotaList(level),
						tag.getList(TAG_INITIAL_STACK, tag.getInt(TAG_INITIAL_STACK_LENGTH)).toIotaList(level),
						SpellDatum.fromNBT(tag.getCompound(TAG_INITIAL_RAVENMIND), level)
					)
				}

				return LemmaCast(
					level.getEntity(tag.getUUID(TAG_LEMMA)) as BaseLemma,
					tag.getInt(TAG_PRIORITY),
					tag.getLong(TAG_TIME_ADDED),
					tag.getList(TAG_HEX, tag.getInt(TAG_HEX_LENGTH)).toIotaList(level),
					tag.getList(TAG_INITIAL_STACK, tag.getInt(TAG_INITIAL_STACK_LENGTH)).toIotaList(level),
					SpellDatum.fromNBT(tag.getCompound(TAG_INITIAL_RAVENMIND), level)
				)
			}
		}
	}

	/**
	 * the result passed back to the Wisp after its cast is successfully executed.
	 */
	data class LemmaCastResult(val lemma: BaseLemma, val makesCastSound: Boolean, val endStack: MutableList<SpellDatum<*>>, val endRavenmind: SpellDatum<*>) {
		fun callback() { lemma.castCallback(this) }
	}

	companion object {
		const val TAG_NUM_CASTS = "num_casts"
		const val TAG_CAST_LIST = "cast_list"
		const val LEMMA_EVALS_PER_TICK = 10

		var specialHandlers: MutableList<(LemmaCastingManager, LemmaCast) -> Boolean> = mutableListOf()
	}
}