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


        companion object {
            const val DEFAULT_GENERATE_SLIPWAY_GEODES: Boolean = true

            // default costs of misc spells
            val DEFAULT_FALLING_BLOCK_COST = BoundedConfig.dustCost(1.5)
            val DEFAULT_FREEZE_COST = BoundedConfig.dustCost(3)
            val DEFAULT_PARTICLES_COST = BoundedConfig.dustCost(0.1)
            val DEFAULT_PLACE_TYPE_COST = BoundedConfig.dustCost(0.125)
            val DEFAULT_SMELT_COST = BoundedConfig.dustCost(0.75)

            // default costs of wisp spells
            val DEFAULT_MOVE_SPEED_SET_COST = BoundedConfig.dustCost(1)
            val DEFAULT_SUMMON_TICKING_WISP_COST = BoundedConfig.dustCost(3)
            val DEFAULT_SUMMON_PROJECTILE_WISP_COST = BoundedConfig.dustCost(3.0/1.75)
            val DEFAULT_SUMMON_PROJECTILE_WISP_MIN_COST = BoundedConfig.dustCost(0.5)

            // default costs of wisp upkeep
            val DEFAULT_TICKING_WISP_UPKEEP_PER_TICK = BoundedConfig.dustCost(0.65/20.0)
            val DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK = BoundedConfig.dustCost(0.325 / 20.0)
            val DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = BoundedConfig(0.25 / 0.325, 0.0, 1.0)
            val DEFAULT_LINK_UPKEEP_PER_TICK = BoundedConfig.dustCost(0.01 / 20.0)

            // default costs of link spells
            val DEFAULT_LINK_COST = BoundedConfig.shardCost(1)
            val DEFAULT_SEND_IOTA_COST = BoundedConfig.dustCost(0.01)
            val DEFAULT_UNLINK_CCOST = BoundedConfig.dustCost(2)

            // default costs of gate spells
            val DEFAULT_MAKE_GATE_COST = BoundedConfig.chargedCost(32)
            val DEFAULT_MARK_GATE_COST = BoundedConfig.dustCost(0.05)
            val DEFAULT_CLOSE_GATE_COST = BoundedConfig.shardCost(0.5)


            // default costs of great spells
            val DEFAULT_CONSUME_WISP_OWN_COST = BoundedConfig.shardCost(1)
            val DEFAULT_CONSUME_WISP_OTHERS_COST_PER_MEDIA = BoundedConfig(1.5, 1.0, 20.0)
            val DEFAULT_SEON_WISP_SET_COST = BoundedConfig.chargedCost(5)
            val DEFAULT_TICK_CONSTANT_COST = BoundedConfig.dustCost(0.1)
            val DEFAULT_TICK_COST_PER_TICKED = BoundedConfig.dustCost(0.001)
        }
    }

    data class BoundedConfig<T>(val default: T, val min: T?, val max: T?) {
        companion object {
            const val DEF_MIN_COST = (0.0001 * MediaConstants.DUST_UNIT).toInt()
            const val DEF_MAX_COST = 10_000 * MediaConstants.DUST_UNIT

            fun dustCost(default: Int): BoundedConfig<Int>
                    = BoundedConfig(default * MediaConstants.DUST_UNIT, DEF_MIN_COST, DEF_MAX_COST)
            fun dustCost(default: Double): BoundedConfig<Int>
                    = BoundedConfig((default * MediaConstants.DUST_UNIT).toInt(), DEF_MIN_COST, DEF_MAX_COST)
            fun dustCost(default: Int, min: Int?, max: Int?): BoundedConfig<Int>
                = BoundedConfig(default * MediaConstants.DUST_UNIT, min?.let { it * MediaConstants.DUST_UNIT }, max?.let { it * MediaConstants.DUST_UNIT })
            fun dustCost(default: Double, min: Double?, max: Double?): BoundedConfig<Int>
                    = BoundedConfig((default * MediaConstants.DUST_UNIT).toInt(), min?.let { (it * MediaConstants.DUST_UNIT).toInt() }, max?.let { (it * MediaConstants.DUST_UNIT).toInt() })

            fun shardCost(default: Int): BoundedConfig<Int>
                    = BoundedConfig(default * MediaConstants.SHARD_UNIT, DEF_MIN_COST, DEF_MAX_COST)
            fun shardCost(default: Double): BoundedConfig<Int>
                    = BoundedConfig((default * MediaConstants.SHARD_UNIT).toInt(), DEF_MIN_COST, DEF_MAX_COST)
            fun shardCost(default: Int, min: Int?, max: Int?): BoundedConfig<Int>
                    = BoundedConfig(default * MediaConstants.SHARD_UNIT, min?.let { it * MediaConstants.SHARD_UNIT }, max?.let { it * MediaConstants.SHARD_UNIT })
            fun shardCost(default: Double, min: Double?, max: Double?): BoundedConfig<Int>
                    = BoundedConfig((default * MediaConstants.SHARD_UNIT).toInt(), min?.let { (it * MediaConstants.SHARD_UNIT).toInt() }, max?.let { (it * MediaConstants.SHARD_UNIT).toInt() })

            fun chargedCost(default: Int): BoundedConfig<Int>
                    = BoundedConfig(default * MediaConstants.CRYSTAL_UNIT, DEF_MIN_COST, DEF_MAX_COST)
            fun chargedCost(default: Double): BoundedConfig<Int>
                    = BoundedConfig((default * MediaConstants.CRYSTAL_UNIT).toInt(), DEF_MIN_COST, DEF_MAX_COST)
            fun chargedCost(default: Int, min: Int?, max: Int?): BoundedConfig<Int>
                    = BoundedConfig(default * MediaConstants.CRYSTAL_UNIT, min?.let { it * MediaConstants.CRYSTAL_UNIT }, max?.let { it * MediaConstants.CRYSTAL_UNIT })
            fun chargedCost(default: Double, min: Double?, max: Double?): BoundedConfig<Int>
                    = BoundedConfig((default * MediaConstants.CRYSTAL_UNIT).toInt(), min?.let { (it * MediaConstants.CRYSTAL_UNIT).toInt() }, max?.let { (it * MediaConstants.CRYSTAL_UNIT).toInt() })
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