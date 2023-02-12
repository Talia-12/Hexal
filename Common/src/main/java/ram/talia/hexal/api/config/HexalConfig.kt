package ram.talia.hexal.api.config

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.resources.ResourceLocation

object HexalConfig {
    interface CommonConfigAccess { }

    interface ClientConfigAccess { }

    interface ServerConfigAccess {
        val generateSlipwayGeodes: Boolean

        // costs of misc spells
        val fallingBlockCost: Int
        val freezeCost: Int
        val particlesCost: Int
        val placeTypeCost: Int
        val smeltCost: Int

        // costs of wisp spells
        val moveSpeedSetCost: Int
        val summonTickingWispCost: Int
        val summonProjectileWispCost: Int
        val summonProjectileWispMinCost: Int

        // costs of wisp upkeep
        val tickingWispUpkeepPerTick: Int
        val projectileWispUpkeepPerTick: Int
        val untriggeredWispUpkeepDiscount: Double
        val linkUpkeepPerTick: Int
        val seonDiscountFactor: Double

        // costs of link spells
        val linkCost: Int
        val sendIotaCost: Int
        val unlinkCost: Int

        // costs of gate spells
        val makeGateCost: Int
        val markGateCost: Int
        val closeGateCost: Int

        // costs of great spells
        val consumeWispOwnCost: Int
        val consumeWispOthersCostPerMedia: Double
        val seonWispSetCost: Int
        val tickConstantCost: Int
        val tickCostPerTicked: Int

        // inverse probability of OpTick random ticking a block.
        val tickRandomTickIProb: Int


        companion object {
            const val DEFAULT_GENERATE_SLIPWAY_GEODES: Boolean = true

            const val DEF_MIN_COST = (0.0001 * MediaConstants.DUST_UNIT).toInt()
            const val DEF_MAX_COST = 10_000 * MediaConstants.DUST_UNIT

            // default costs of misc spells
            const val DEFAULT_FALLING_BLOCK_COST = MediaConstants.DUST_UNIT * 3 / 2
            const val DEFAULT_FREEZE_COST = MediaConstants.DUST_UNIT
            const val DEFAULT_PARTICLES_COST = MediaConstants.DUST_UNIT / 100
            const val DEFAULT_PLACE_TYPE_COST = MediaConstants.DUST_UNIT / 8
            const val DEFAULT_SMELT_COST = MediaConstants.DUST_UNIT * 3 / 4

            // default costs of wisp spells
            const val DEFAULT_MOVE_SPEED_SET_COST =MediaConstants.DUST_UNIT
            const val DEFAULT_SUMMON_TICKING_WISP_COST = MediaConstants.DUST_UNIT * 3
            const val DEFAULT_SUMMON_PROJECTILE_WISP_COST = (MediaConstants.DUST_UNIT * 3 / 1.75).toInt()
            const val DEFAULT_SUMMON_PROJECTILE_WISP_MIN_COST = MediaConstants.DUST_UNIT / 2

            // default costs of wisp upkeep
            const val DEFAULT_TICKING_WISP_UPKEEP_PER_TICK = (MediaConstants.DUST_UNIT * 0.65/20.0).toInt()
            const val DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK = (MediaConstants.DUST_UNIT * 0.325 / 20.0).toInt()
            const val DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = 0.25 / 0.325
            const val DEFAULT_LINK_UPKEEP_PER_TICK = (MediaConstants.DUST_UNIT * 0.01 / 20.0).toInt()
            const val DEFAULT_SEON_DISCOUNT_FACTOR = 20.0

            const val MIN_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = 0
            const val MAX_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = 1

            const val MIN_SEON_DISCOUNT_FACTOR = 2.0
            const val MAX_SEON_DISCOUNT_FACTOR = 200.0


            // default costs of link spells
            const val DEFAULT_LINK_COST = MediaConstants.SHARD_UNIT * 1
            const val DEFAULT_SEND_IOTA_COST = MediaConstants.DUST_UNIT / 100
            const val DEFAULT_UNLINK_COST = MediaConstants.DUST_UNIT * 2

            // default costs of gate spells
            const val DEFAULT_MAKE_GATE_COST = MediaConstants.CRYSTAL_UNIT * 32
            const val DEFAULT_MARK_GATE_COST = MediaConstants.DUST_UNIT / 20
            const val DEFAULT_CLOSE_GATE_COST = MediaConstants.SHARD_UNIT / 2


            // default costs of great spells
            const val DEFAULT_CONSUME_WISP_OWN_COST = MediaConstants.SHARD_UNIT
            const val DEFAULT_CONSUME_WISP_OTHERS_COST_PER_MEDIA = 1.5
            const val DEFAULT_SEON_WISP_SET_COST = MediaConstants.CRYSTAL_UNIT * 5
            const val DEFAULT_TICK_CONSTANT_COST = MediaConstants.DUST_UNIT / 10
            const val DEFAULT_TICK_COST_PER_TICKED = MediaConstants.DUST_UNIT / 1000

            const val MIN_CONSUME_WISP_OTHERS_COST_PER_MEDIA = 1.0
            const val MAX_CONSUME_WISP_OTHERS_COST_PER_MEDIA = 20.0

            const val DEFAULT_TICK_RANDOM_TICK_I_PROB = 1365
            const val MIN_TICK_RANDOM_TICK_I_PROB = 600
            const val MAX_TICK_RANDOM_TICK_I_PROB = 2100
        }
    }

    // Simple extensions for resource location configs
    fun anyMatch(keys: List<String>, key: ResourceLocation): Boolean {
        for (s in keys) {
            if (ResourceLocation.isValidResourceLocation(s)) {
                val rl = ResourceLocation(s)
                if (rl == key) {
                    return true
                }
            }
        }
        return false
    }

    fun noneMatch(keys: List<String>, key: ResourceLocation): Boolean {
        return !anyMatch(keys, key)
    }

    private object DummyCommon : CommonConfigAccess {  }
    private object DummyClient : ClientConfigAccess {  }
    private object DummyServer : ServerConfigAccess {
        override val generateSlipwayGeodes: Boolean
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val fallingBlockCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val freezeCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val particlesCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val placeTypeCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val smeltCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val moveSpeedSetCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val summonTickingWispCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val summonProjectileWispCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val summonProjectileWispMinCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val tickingWispUpkeepPerTick: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val projectileWispUpkeepPerTick: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val untriggeredWispUpkeepDiscount: Double
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val linkUpkeepPerTick: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val seonDiscountFactor: Double
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val linkCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val sendIotaCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val unlinkCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val makeGateCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val markGateCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val closeGateCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val consumeWispOwnCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val consumeWispOthersCostPerMedia: Double
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val seonWispSetCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val tickConstantCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val tickCostPerTicked: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val tickRandomTickIProb: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
    }

    @JvmStatic
    var common: CommonConfigAccess = DummyCommon
        set(access) {
            if (field != DummyCommon) {
                HexAPI.LOGGER.warn("CommonConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }

    @JvmStatic
    var client: ClientConfigAccess = DummyClient
        set(access) {
            if (field != DummyClient) {
                HexAPI.LOGGER.warn("ClientConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }

    @JvmStatic
    var server: ServerConfigAccess = DummyServer
        set(access) {
            if (field != DummyServer) {
                HexAPI.LOGGER.warn("ServerConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }
}