package ram.talia.hexal.api.gates

import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.api.utils.getList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.nbt.toNbtList
import ram.talia.hexal.api.nbt.toUUIDList
import ram.talia.hexal.api.spell.iota.GateIota
import java.util.UUID

/**
 * Stores which gate index references which entities.
 */
object GateManager {
    private var currentGateNum = 0

    @JvmField
    val allMarked: MutableMap<Int, MutableList<UUID>> = mutableMapOf()

    @JvmStatic
    fun mark(gate: Int, entity: Entity) = mark(gate, entity.uuid)

    @JvmStatic
    fun mark(gate: Int, entity: UUID) {
        allMarked.putIfAbsent(gate, mutableListOf())

        allMarked[gate]!!.add(entity)
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
    fun makeGate(): GateIota {
        val gate = GateIota(currentGateNum)
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
                allMarked[gateStr.toInt()] = markedTag.getList(gateStr, Tag.TAG_INT_ARRAY).toUUIDList()
            }
        }
    }

    @JvmStatic
    fun writeToNbt(tag: CompoundTag) {
        tag.putInt(TAG_CURRENT_GATE_NUM, currentGateNum)

        val markedTag = CompoundTag()

        for ((gate, marked) in allMarked) {
            markedTag.putList(gate.toString(), marked.toNbtList())
        }

        tag.putCompound(TAG_MARKED, markedTag)
    }

    const val TAG_CURRENT_GATE_NUM = "hexal:current_gate_num"
    const val TAG_MARKED = "hexal:marked"
}