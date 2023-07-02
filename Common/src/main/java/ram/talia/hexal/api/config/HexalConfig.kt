package ram.talia.hexal.api.config

import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI

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

        // costs of fabric only spells
        val phaseBlockCostFactor: Double

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
        val storingPlayerCostScaleFactor: Double
        val mediaFlowRateOverLink: Double

        // costs of link spells
        val linkCost: Int
        val sendIotaCost: Int
        val unlinkCost: Int

        // costs of gate spells
        val makeGateCost: Int
        val markGateCost: Int
        val closeGateCost: Int
        val closeGateDistanceCostFactor: Int
        val maxGateOffset: Double

        // costs of item spells
        val bindStorageCost: Int
        val bindTemporaryStorageCost: Int
        val makeItemCost: Int
        val returnItemCost: Int
        val craftItemCost: Int
        val tradeItemCost: Int
        val useItemOnCost: Int

        val maxItemsReturned: Int

        val maxRecordsInMediafiedStorage: Int

        // costs of great spells
        val consumeWispOwnCost: Int
        val consumeWispOthersCostPerMedia: Double
        val seonWispSetCost: Int
        val tickConstantCost: Int
        val tickCostPerTicked: Int

        // inverse probability of OpTick random ticking a block.
        val tickRandomTickIProb: Int

        fun isAccelerateAllowed(blockId: ResourceLocation): Boolean

        companion object {
            const val DEFAULT_GENERATE_SLIPWAY_GEODES: Boolean = true

            const val DEF_MIN_COST = 0.0001
            const val DEF_MAX_COST = 10_000.0

            // default costs of misc spells (in dust)
            const val DEFAULT_FALLING_BLOCK_COST = 1.5
            const val DEFAULT_FREEZE_COST = 1.0
            const val DEFAULT_PARTICLES_COST = 0.002
            const val DEFAULT_PLACE_TYPE_COST = 0.125
            const val DEFAULT_SMELT_COST = 0.75

            // default costs of fabric only spells
            const val DEFAULT_PHASE_BLOCK_COST_FACTOR = 0.0001

            const val MIN_PHASE_BLOCK_COST_FACTOR = 0.00001
            const val MAX_PHASE_BLOCK_COST_FACTOR = 0.001

            // default costs of wisp spells
            const val DEFAULT_MOVE_SPEED_SET_COST =1.0
            const val DEFAULT_SUMMON_TICKING_WISP_COST = 3.0
            const val DEFAULT_SUMMON_PROJECTILE_WISP_COST = 1.7
            const val DEFAULT_SUMMON_PROJECTILE_WISP_MIN_COST = 0.5

            // default costs of wisp upkeep
            const val DEFAULT_TICKING_WISP_UPKEEP_PER_TICK = 0.65 / 20.0
            const val DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK =  0.325 / 20.0
            const val DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = 0.77
            const val DEFAULT_LINK_UPKEEP_PER_TICK = 0.01 / 20.0
            const val DEFAULT_SEON_DISCOUNT_FACTOR = 20.0
            const val DEFAULT_STORING_PLAYER_COST_SCALE_FACTOR = 20.0
            const val DEFAULT_MEDIA_FLOW_RATE_OVER_LINK = 0.01

            const val MIN_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = 0.0
            const val MAX_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = 1.0

            const val MIN_SEON_DISCOUNT_FACTOR = 2.0
            const val MAX_SEON_DISCOUNT_FACTOR = 200.0

            const val MIN_STORING_PLAYER_COST_SCALE_FACTOR = 1.0
            const val MAX_STORING_PLAYER_COST_SCALE_FACTOR = 200.0

            const val MIN_MEDIA_FLOW_RATE_OVER_LINK = 0.0
            const val MAX_MEDIA_FLOW_RATE_OVER_LINK = 0.1

            // default costs of link spells
            const val DEFAULT_LINK_COST = 5.0
            const val DEFAULT_SEND_IOTA_COST = 0.01
            const val DEFAULT_UNLINK_COST = 2.0

            // default costs of gate spells
            const val DEFAULT_MAKE_GATE_COST = 320.0
            const val DEFAULT_MARK_GATE_COST = 0.05
            const val DEFAULT_CLOSE_GATE_COST = 2.5
            const val DEFAULT_CLOSE_GATE_DISTANCE_COST_SCALE_FACTOR = 0.1
            const val DEFAULT_MAX_GATE_OFFSET = 32.0

            const val MIN_MAX_GATE_OFFSET = 1.0
            const val MAX_MAX_GATE_OFFSET = 96.0

            // default costs of item spells
            const val DEFAULT_BIND_STORAGE_COST = 32.0
            const val DEFAULT_BIND_TEMPORARY_STORAGE_COST = 0.001
            const val DEFAULT_MAKE_ITEM_COST = 0.1
            const val DEFAULT_RETURN_ITEM_COST = 0.1
            const val DEFAULT_CRAFT_ITEM_COST = 0.1
            const val DEFAULT_TRADE_ITEM_COST = 0.1
            const val DEFAULT_USE_ITEM_ON_COST = 0.125

            const val DEFAULT_MAX_ITEMS_RETURNED = 32000

            const val MIN_MAX_ITEMS_RETURNED = 640
            const val MAX_MAX_ITEMS_RETURNED = 64000

            const val DEFAULT_MAX_RECORDS_IN_MEDIAFIED_STORAGE = 1023

            const val MIN_MAX_RECORDS_IN_MEDIAFIED_STORAGE = 128
            const val MAX_MAX_RECORDS_IN_MEDIAFIED_STORAGE = 16384


            // default costs of great spells
            const val DEFAULT_CONSUME_WISP_OWN_COST = 5.0
            const val DEFAULT_CONSUME_WISP_OTHERS_COST_PER_MEDIA = 1.5
            const val DEFAULT_SEON_WISP_SET_COST = 50.0
            const val DEFAULT_TICK_CONSTANT_COST = 0.1
            const val DEFAULT_TICK_COST_PER_TICKED = 0.001

            const val MIN_CONSUME_WISP_OTHERS_COST_PER_MEDIA = 1.0
            const val MAX_CONSUME_WISP_OTHERS_COST_PER_MEDIA = 20.0

            const val DEFAULT_TICK_RANDOM_TICK_I_PROB = 1365
            const val MIN_TICK_RANDOM_TICK_I_PROB = 600
            const val MAX_TICK_RANDOM_TICK_I_PROB = 2100

            val DEFAULT_ACCELERATE_DENY_LIST: List<String> = listOf("hexcasting:impetus_look", "create:deployer")
        }
    }

    // Simple extensions for resource location configs
    @JvmStatic
    fun anyMatch(keys: MutableList<out String>, key: ResourceLocation): Boolean {
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

    @JvmStatic
    fun noneMatch(keys: MutableList<out String>, key: ResourceLocation): Boolean {
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
        override val phaseBlockCostFactor: Double
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
        override val storingPlayerCostScaleFactor: Double
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val linkCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val sendIotaCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val unlinkCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val mediaFlowRateOverLink: Double
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val makeGateCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val markGateCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val closeGateCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val closeGateDistanceCostFactor: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val maxGateOffset: Double
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val bindStorageCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val bindTemporaryStorageCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val makeItemCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val returnItemCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val craftItemCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val tradeItemCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val useItemOnCost: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val maxItemsReturned: Int
            get() = throw IllegalStateException("Attempted to access property of Dummy Config Object")
        override val maxRecordsInMediafiedStorage: Int
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

        override fun isAccelerateAllowed(blockId: ResourceLocation): Boolean {
            throw IllegalStateException("Attempted to access property of Dummy Config Object")
        }
    }

    @JvmStatic
    var common: CommonConfigAccess = DummyCommon
        set(access) {
            if (field != DummyCommon) {
                HexalAPI.LOGGER.warn("CommonConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }

    @JvmStatic
    var client: ClientConfigAccess = DummyClient
        set(access) {
            if (field != DummyClient) {
                HexalAPI.LOGGER.warn("ClientConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }

    @JvmStatic
    var server: ServerConfigAccess = DummyServer
        set(access) {
            if (field != DummyServer) {
                HexalAPI.LOGGER.warn("ServerConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }
}