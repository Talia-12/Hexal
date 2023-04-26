package ram.talia.hexal.api.gates

import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.api.utils.getList
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.nbt.toNbtList
import ram.talia.hexal.api.nbt.toUUIDList
import ram.talia.hexal.api.spell.iota.GateIota
import java.util.UUID

/**
 * Stores which gate index references which entities. Data saved by [GateSavedData].
 */
object GateManager {
    private var currentGateNum = 0

    @JvmField
    var shouldClearOnWrite = false

    @JvmField
    val allMarked: MutableMap<Int, MutableSet<UUID>> = mutableMapOf()

    @JvmStatic
    fun mark(gate: Int, entity: Entity) = mark(gate, entity.uuid)

    @JvmStatic
    fun mark(gate: Int, entity: UUID) {
        allMarked.putIfAbsent(gate, mutableSetOf())

        allMarked[gate]!!.add(entity)
    }

    @JvmStatic
    fun unmark(gate: Int, entity: Entity) = unmark(gate, entity.uuid)

    @JvmStatic
    fun unmark(gate: Int, entity: UUID) {
        allMarked[gate]?.remove(entity)
    }

    @JvmStatic
    fun clearMarked(gate: Int) {
        allMarked.remove(gate)
    }

    /**
     * This creates a new gate iota with a new index; any gate iota created any other way
     * will be a reference to a previously made gate.
     */
    @JvmStatic
    fun makeGate(): GateIota = makeGate(null)

    /**
     * This creates a new gate iota with a new index; any gate iota created any other way
     * will be a reference to a previously made gate.
     */
    @JvmStatic
    fun makeGate(target: Vec3) = makeGate(Either.left(target))

    /**
     * This creates a new gate iota with a new index; any gate iota created any other way
     * will be a reference to a previously made gate.
     */
    @JvmStatic
    fun makeGate(target: Pair<Entity, Vec3>): GateIota = makeGate(Either.right(target))

    private fun makeGate(target: Either<Vec3, Pair<Entity, Vec3>>?): GateIota {
        val gate = GateIota(currentGateNum, target)
        currentGateNum += 1
        return gate
    }

    @JvmStatic
    fun readFromNbt(tag: CompoundTag) {
        if (tag.contains(TAG_CURRENT_GATE_NUM))
            currentGateNum = tag.getInt(TAG_CURRENT_GATE_NUM)

        if (tag.contains(TAG_MARKED)) {
            val markedTag = tag.getCompound(TAG_MARKED)

            for (gateStr in markedTag.allKeys) {
                allMarked[gateStr.toInt()] = markedTag.getList(gateStr, Tag.TAG_INT_ARRAY).toUUIDList().toMutableSet()
            }
        }
    }

    @JvmStatic
    fun writeToNbt(tag: CompoundTag) {
        tag.putInt(TAG_CURRENT_GATE_NUM, currentGateNum)

        val markedTag = CompoundTag()

        for ((gate, marked) in allMarked) {
            markedTag.putList(gate.toString(), marked.toList().toNbtList())
        }

        tag.putCompound(TAG_MARKED, markedTag)

        if (shouldClearOnWrite) {
            currentGateNum = 0
            allMarked.clear()
        }
    }

    const val TAG_CURRENT_GATE_NUM = "hexal:current_gate_num"
    const val TAG_MARKED = "hexal:marked"
}