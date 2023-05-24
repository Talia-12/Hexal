package ram.talia.hexal.api.spell.casting

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.isTooLargeToSerialize
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.nbt.SerialisedIota
import ram.talia.hexal.api.nbt.SerialisedIotaList
import ram.talia.hexal.common.entities.BaseCastingWisp
import java.util.*

class WispCastingManager(private val caster: ServerPlayer) {

	private val queue: PriorityQueue<WispCast> = PriorityQueue()

	/**
	 * Schedule a given cast to be added to this WispCastingManager's priority queue. It will be evaluated in the next tick unless the player is doing something that
	 * is producing a lot of Wisp casts. Higher [priority] casts will always be executed first - between casts of equal [priority], the first one added to the stack is
	 * preferred.
	 */
	fun scheduleCast(
			wisp: BaseCastingWisp,
			priority: Int,
			hex: SerialisedIotaList,
			initialStack: SerialisedIotaList,
			initialRavenmind: SerialisedIota,
	) {
		val cast = WispCast(wisp, priority, caster.level.gameTime, hex, initialStack, initialRavenmind)

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

		var evalsLeft = WISP_EVALS_PER_TICK

		val itr = queue.iterator()

		val results = ArrayList<WispCastResult>()

		while (evalsLeft > 0 && itr.hasNext()) {
			val cast = itr.next()
			itr.remove()

			// if the wisp isn't chunkloaded at the moment, delete it from the queue (this is a small enough edge case I can't be bothered robustly handling it)
			val wisp = cast.wisp ?: (caster.level as ServerLevel).getEntity(cast.wispUUID) as? BaseCastingWisp ?: continue
			cast.wisp = wisp

			if (wisp.isRemoved)
				continue

			if (wisp.level.dimension() != wisp.caster?.level?.dimension())
				continue

			results += cast(cast)

			evalsLeft--
		}

		results.forEach { result -> result.callback() }
	}

	/**
	 * Actually executes the cast described in [cast]. Will throw a NullPointerException if it somehow got here with [cast] == null.
	 */
	@Suppress("CAST_NEVER_SUCCEEDS")
	fun cast(cast: WispCast): WispCastResult {
		val ctx = CastingContext(
			caster,
			InteractionHand.MAIN_HAND,
			CastingContext.CastSource.PACKAGED_HEX
		)

		val wisp = cast.wisp!!
		wisp.summonedChildThisCast = false // restricts the wisp to only summoning one other wisp per cast.

		// IntelliJ is complaining that ctx will never be an instance of IMixinCastingContext cause it doesn't know about mixin, but we know better
		val mCast = ctx as? IMixinCastingContext
		mCast?.wisp = wisp

		val harness = CastingHarness(ctx)

		harness.stack = cast.initialStack.getIotas(ctx.world).toMutableList()
		harness.ravenmind = cast.initialRavenmind.getIota(ctx.world)

		val info = harness.executeIotas(cast.hex.getIotas(ctx.world), caster.getLevel())

		// TODO: Make this a mishap
		// Clear stack if it gets too large
		var endStack = harness.stack
		if (isTooLargeToSerialize(endStack)) {
            endStack = mutableListOf()
        }

		// the wisp will have things it wants to do once the cast is successful, so a callback on it is called to let it know that happened, and what the end state of the
		// stack and ravenmind is. This is returned and added to a list that [executeCasts] will loop over to hopefully prevent concurrent modification problems.
		return WispCastResult(wisp, info.resolutionType.success, endStack, harness.ravenmind)
	}

	fun readFromNbt(tag: CompoundTag?, level: ServerLevel) {
		val list = tag?.get(TAG_CAST_LIST) as? ListTag ?: return

		for (castTag in list) {
			queue.add(WispCast.makeFromNbt(castTag.asCompound, level))
		}
	}

	fun writeToNbt(tag: CompoundTag) {
		val list = ListTag()

		for (cast in queue) {
			list.add(cast.writeToNbt())
		}

		tag.put(TAG_CAST_LIST, list)
	}

	data class WispCast(
			val wispUUID: UUID,
			val priority: Int,
			val timeAdded: Long,
			val hex: SerialisedIotaList,
			val initialStack: SerialisedIotaList,
			val initialRavenmind: SerialisedIota,
	) : Comparable<WispCast> {
		/**
		 * when loading from NBT, it calls ServerLevel.entity(UUID), which could return null.
		 */
		var wisp: BaseCastingWisp? = null

		constructor(
			wisp: BaseCastingWisp,
			priority: Int,
			timeAdded: Long,
			hex: SerialisedIotaList,
			initialStack: SerialisedIotaList,
			initialRavenmind: SerialisedIota
		) : this(wisp.uuid, priority, timeAdded, hex, initialStack, initialRavenmind) {
			this.wisp = wisp
		}

		override fun compareTo(other: WispCast): Int {
			if (priority != other.priority)
				return priority - other.priority
			return (timeAdded - other.timeAdded).toInt()
		}

		fun writeToNbt(): CompoundTag {
			val tag = CompoundTag()

			tag.putUUID(TAG_WISP, wispUUID)
			tag.putInt(TAG_PRIORITY, priority)
			tag.putLong(TAG_TIME_ADDED, timeAdded)
			tag.put(TAG_HEX, hex.getTag())
			tag.put(TAG_INITIAL_STACK, initialStack.getTag())
			tag.putCompound(TAG_INITIAL_RAVENMIND, initialRavenmind.getTag())

			return tag
		}

		companion object {
			const val TAG_WISP = "wisp"
			const val TAG_PRIORITY = "priority"
			const val TAG_TIME_ADDED = "time_added"
			const val TAG_HEX = "hex"
			const val TAG_INITIAL_STACK = "initial_stack"
			const val TAG_INITIAL_RAVENMIND = "initial_ravenmind"

			fun makeFromNbt(tag: CompoundTag, level: ServerLevel): WispCast {
				val wispUUID = tag.getUUID(TAG_WISP)
				val wisp: BaseCastingWisp? = level.getEntity(wispUUID) as? BaseCastingWisp

				if (wisp != null) {
					return WispCast(
						wisp,
						tag.getInt(TAG_PRIORITY),
						tag.getLong(TAG_TIME_ADDED),
						SerialisedIotaList(tag.get(TAG_HEX) as? ListTag),
						SerialisedIotaList(tag.get(TAG_INITIAL_STACK) as? ListTag),
						SerialisedIota(tag.get(TAG_INITIAL_RAVENMIND) as? CompoundTag)
					)
				}

				return WispCast(
					tag.getUUID(TAG_WISP),
					tag.getInt(TAG_PRIORITY),
					tag.getLong(TAG_TIME_ADDED),
					SerialisedIotaList(tag.get(TAG_HEX) as? ListTag),
					SerialisedIotaList(tag.get(TAG_INITIAL_STACK) as? ListTag),
					SerialisedIota(tag.get(TAG_INITIAL_RAVENMIND) as? CompoundTag)
				)
			}
		}
	}

	/**
	 * the result passed back to the Wisp after its cast is successfully executed.
	 */
	data class WispCastResult(val wisp: BaseCastingWisp, val succeeded: Boolean, val endStack: SerialisedIotaList, val endRavenmind: SerialisedIota) {
		constructor(wisp: BaseCastingWisp, succeeded: Boolean, endStack: MutableList<Iota>, endRavenmind: Iota?)
			: this(wisp, succeeded, SerialisedIotaList(endStack), SerialisedIota(endRavenmind))
		fun callback() { wisp.castCallback(this) }
	}

	companion object {
		const val TAG_CAST_LIST = "cast_list"
		const val WISP_EVALS_PER_TICK = 10

		var specialHandlers: MutableList<(WispCastingManager, WispCast) -> Boolean> = mutableListOf()
	}
}