package ram.talia.hexal.forge;


import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.api.config.HexalConfig;

import java.util.List;

import static ram.talia.hexal.api.config.HexalConfig.noneMatch;

public class ForgeHexalConfig implements HexalConfig.CommonConfigAccess {
    public ForgeHexalConfig(ForgeConfigSpec.Builder builder) {

    }

    public static class Client implements HexalConfig.ClientConfigAccess {
        public Client(ForgeConfigSpec.Builder builder) {

        }
    }

    public static class Server implements HexalConfig.ServerConfigAccess {
        private static ForgeConfigSpec.BooleanValue generateSlipwayGeodes;

        // costs of misc spells
        private static ForgeConfigSpec.DoubleValue fallingBlockCost;
        private static ForgeConfigSpec.DoubleValue freezeCost;
        private static ForgeConfigSpec.DoubleValue particlesCost;
        private static ForgeConfigSpec.DoubleValue placeTypeCost;
        private static ForgeConfigSpec.DoubleValue smeltCost;

        // costs of wisp spells
        private static ForgeConfigSpec.DoubleValue moveSpeedSetCost;
        private static ForgeConfigSpec.DoubleValue summonTickingWispCost;
        private static ForgeConfigSpec.DoubleValue summonProjectileWispCost;
        private static ForgeConfigSpec.DoubleValue summonProjectileWispMinCost;

        // costs of wisp upkeep
        private static ForgeConfigSpec.DoubleValue tickingWispUpkeepPerTick;
        private static ForgeConfigSpec.DoubleValue projectileWispUpkeepPerTick;
        private static ForgeConfigSpec.DoubleValue untriggeredWispUpkeepDiscount;
        private static ForgeConfigSpec.DoubleValue linkUpkeepPerTick;
        private static ForgeConfigSpec.DoubleValue seonDiscountFactor;
        private static ForgeConfigSpec.DoubleValue storingPlayerCostScaleFactor;
        private static ForgeConfigSpec.DoubleValue mediaFlowRateOverLink;

        // costs of link spells
        private static ForgeConfigSpec.DoubleValue linkCost;
        private static ForgeConfigSpec.DoubleValue sendIotaCost;
        private static ForgeConfigSpec.DoubleValue unlinkCost;

        // costs of gate spells
        private static ForgeConfigSpec.DoubleValue makeGateCost;
        private static ForgeConfigSpec.DoubleValue markGateCost;
        private static ForgeConfigSpec.DoubleValue closeGateCost;
        private static ForgeConfigSpec.DoubleValue closeGateDistanceCostFactor;
        private static ForgeConfigSpec.DoubleValue maxGateOffset;

        // costs of item spells
        private static ForgeConfigSpec.DoubleValue bindStorageCost;
        private static ForgeConfigSpec.DoubleValue bindTemporaryStorageCost;
        private static ForgeConfigSpec.DoubleValue makeItemCost;
        private static ForgeConfigSpec.DoubleValue returnItemCost;
        private static ForgeConfigSpec.DoubleValue craftItemCost;
        private static ForgeConfigSpec.DoubleValue tradeItemCost;
        private static ForgeConfigSpec.DoubleValue useItemOnCost;
        private static ForgeConfigSpec.IntValue maxItemsReturned;
        private static ForgeConfigSpec.IntValue maxRecordsInMediafiedStorage;


        // costs of great spells
        private static ForgeConfigSpec.DoubleValue consumeWispOwnCost;
        private static ForgeConfigSpec.DoubleValue consumeWispOthersCostPerMedia;
        private static ForgeConfigSpec.DoubleValue seonWispSetCost;
        private static ForgeConfigSpec.DoubleValue tickConstantCost;
        private static ForgeConfigSpec.DoubleValue tickCostPerTicked;
        private static ForgeConfigSpec.IntValue tickRandomTickIProb;

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> accelerateDenyList;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.translation("text.autoconfig.hexal.option.server.terrainGeneration").push("terrainGeneration");
            generateSlipwayGeodes = builder.translation("text.autoconfig.hexal.option.server.generateSlipwayGeodes")
                    .define("generateSlipwayGeodes", DEFAULT_GENERATE_SLIPWAY_GEODES);

            builder.pop();

            // costs of misc spells
            builder.translation("text.autoconfig.hexal.option.server.miscSpells").push("miscSpells");
            fallingBlockCost = builder.translation("text.autoconfig.hexal.option.server.miscSpells.fallingBlockCost")
                    .defineInRange("fallingBlockCost", DEFAULT_FALLING_BLOCK_COST, DEF_MIN_COST, DEF_MAX_COST);
            freezeCost = builder.translation("text.autoconfig.hexal.option.server.miscSpells.freezeCost")
                    .defineInRange("freezeCost", DEFAULT_FREEZE_COST, DEF_MIN_COST, DEF_MAX_COST);
            particlesCost = builder.translation("text.autoconfig.hexal.option.server.miscSpells.particlesCost")
                    .defineInRange("particlesCost", DEFAULT_PARTICLES_COST, DEF_MIN_COST, DEF_MAX_COST);
            placeTypeCost = builder.translation("text.autoconfig.hexal.option.server.miscSpells.placeTypeCost")
                    .defineInRange("placeTypeCost", DEFAULT_PLACE_TYPE_COST, DEF_MIN_COST, DEF_MAX_COST);
            smeltCost = builder.translation("text.autoconfig.hexal.option.server.miscSpells.smeltCost")
                    .defineInRange("smeltCost", DEFAULT_SMELT_COST, DEF_MIN_COST, DEF_MAX_COST);

            builder.pop();


            // costs of wisp spells
            builder.translation("text.autoconfig.hexal.option.server.wispSpells").push("wispSpells");
            moveSpeedSetCost = builder.translation("text.autoconfig.hexal.option.server.wispSpells.moveSpeedSetCost")
                    .comment(
                            "Cost to increase the movement speed of a wisp by 1.",
                            "This is the base cost, with greater increases being quadratically based on this."
                    )
                    .defineInRange("moveSpeedSetCost", DEFAULT_MOVE_SPEED_SET_COST, DEF_MIN_COST, DEF_MAX_COST);
            summonTickingWispCost = builder.translation("text.autoconfig.hexal.option.server.wispSpells.summonTickingWispCost")
                    .defineInRange("summonTickingWispCost", DEFAULT_SUMMON_TICKING_WISP_COST, DEF_MIN_COST, DEF_MAX_COST);
            summonProjectileWispCost = builder.translation("text.autoconfig.hexal.option.server.wispSpells.summonProjectileWispCost")
                    .defineInRange("summonProjectileWispCost", DEFAULT_SUMMON_PROJECTILE_WISP_COST, DEF_MIN_COST, DEF_MAX_COST);
            summonProjectileWispMinCost = builder.translation("text.autoconfig.hexal.option.server.wispSpells.summonProjectileWispMinCost")
                    .defineInRange("summonProjectileWispMinCost", DEFAULT_SUMMON_PROJECTILE_WISP_MIN_COST, DEF_MIN_COST, DEF_MAX_COST);

            builder.pop();


            // costs of wisp upkeep
            builder.translation("text.autoconfig.hexal.option.server.wispUpkeep").push("wispUpkeep");
            tickingWispUpkeepPerTick = builder.translation("text.autoconfig.hexal.option.server.wispUpkeep.tickingWispUpkeepPerTick")
                    .defineInRange("tickingWispUpkeepPerTick", DEFAULT_TICKING_WISP_UPKEEP_PER_TICK, DEF_MIN_COST, DEF_MAX_COST);
            projectileWispUpkeepPerTick = builder.translation("text.autoconfig.hexal.option.server.wispUpkeep.projectileWispUpkeepPerTick")
                    .defineInRange("projectileWispUpkeepPerTick", DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK, DEF_MIN_COST, DEF_MAX_COST);
            untriggeredWispUpkeepDiscount = builder.translation("text.autoconfig.hexal.option.server.wispUpkeep.untriggeredWispUpkeepDiscount")
                    .comment("The upkeep cost of untriggered cyclic wisps is multiplied by this number.")
                    .defineInRange("untriggeredWispUpkeepDiscount", DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT, MIN_UNTRIGGERED_WISP_UPKEEP_DISCOUNT, MAX_UNTRIGGERED_WISP_UPKEEP_DISCOUNT);
            linkUpkeepPerTick = builder.translation("text.autoconfig.hexal.option.server.wispUpkeep.linkUpkeepPerTick")
                    .defineInRange("linkUpkeepPerTick", DEFAULT_LINK_UPKEEP_PER_TICK, DEF_MIN_COST, DEF_MAX_COST);
            seonDiscountFactor = builder.translation("text.autoconfig.hexal.option.server.wispUpkeep.seonDiscountFactor")
                    .comment("The upkeep cost of bound wisps is divided by this number.")
                    .defineInRange("seonDiscountFactor", DEFAULT_SEON_DISCOUNT_FACTOR, MIN_SEON_DISCOUNT_FACTOR, MAX_SEON_DISCOUNT_FACTOR);
            storingPlayerCostScaleFactor = builder.translation("text.autoconfig.hexal.option.server.wispUpkeep.storingPlayerCostScaleFactor")
                    .comment("The multiplicative cost increase of wisps containing an iota of another player.")
                    .defineInRange("storingPlayerCostScaleFactor", DEFAULT_STORING_PLAYER_COST_SCALE_FACTOR, MIN_STORING_PLAYER_COST_SCALE_FACTOR, MAX_STORING_PLAYER_COST_SCALE_FACTOR);
            mediaFlowRateOverLink = builder.translation("text.autoconfig.hexal.option.server.wispUpkeep.mediaFlowRateOverLink")
                    .comment("Proportion of the difference between media amounts that flows across a link per tick.")
                    .defineInRange("mediaFlowRateOverLink", DEFAULT_MEDIA_FLOW_RATE_OVER_LINK, MIN_MEDIA_FLOW_RATE_OVER_LINK, MAX_MEDIA_FLOW_RATE_OVER_LINK);

            builder.pop();


            // costs of link spells
            builder.translation("text.autoconfig.hexal.option.server.linkSpells").push("linkSpells");
            linkCost = builder.translation("text.autoconfig.hexal.option.server.linkSpells.linkCost")
                    .defineInRange("linkCost", DEFAULT_LINK_COST, DEF_MIN_COST, DEF_MAX_COST);
            sendIotaCost = builder.translation("text.autoconfig.hexal.option.server.linkSpells.sendIotaCost")
                    .defineInRange("sendIotaCost", DEFAULT_SEND_IOTA_COST, DEF_MIN_COST, DEF_MAX_COST);
            unlinkCost = builder.translation("text.autoconfig.hexal.option.server.linkSpells.unlinkCost")
                    .defineInRange("unlinkCost", DEFAULT_UNLINK_COST, DEF_MIN_COST, DEF_MAX_COST);

            builder.pop();


            // costs of gate spells
            builder.translation("text.autoconfig.hexal.option.server.gateSpells").push("gateSpells");
            makeGateCost = builder.translation("text.autoconfig.hexal.option.server.gateSpells.makeGateCost")
                    .defineInRange("makeGateCost", DEFAULT_MAKE_GATE_COST, DEF_MIN_COST, DEF_MAX_COST);
            markGateCost = builder.translation("text.autoconfig.hexal.option.server.gateSpells.markGateCost")
                    .defineInRange("markGateCost", DEFAULT_MARK_GATE_COST, DEF_MIN_COST, DEF_MAX_COST);
            closeGateCost = builder.translation("text.autoconfig.hexal.option.server.gateSpells.closeGateCost")
                    .defineInRange("closeGateCost", DEFAULT_CLOSE_GATE_COST, DEF_MIN_COST, DEF_MAX_COST);
            closeGateDistanceCostFactor = builder.translation("text.autoconfig.hexal.option.server.gateSpells.closeGateDistanceCostFactor")
                    .defineInRange("closeGateDistanceCostFactor", DEFAULT_CLOSE_GATE_DISTANCE_COST_SCALE_FACTOR, DEF_MIN_COST, DEF_MAX_COST);
            maxGateOffset = builder.translation("text.autoconfig.hexal.option.server.gateSpells.maxGateOffset")
                    .defineInRange("maxGateOffset", DEFAULT_MAX_GATE_OFFSET, MIN_MAX_GATE_OFFSET, MAX_MAX_GATE_OFFSET);

            builder.pop();


            // costs of item spells
            builder.translation("text.autoconfig.hexal.option.server.itemSpells").push("itemSpells");
            bindStorageCost = builder.translation("text.autoconfig.hexal.option.server.itemSpells.bindStorageCost")
                    .defineInRange("bindStorageCost", DEFAULT_BIND_STORAGE_COST, DEF_MIN_COST, DEF_MAX_COST);
            bindTemporaryStorageCost = builder.translation("text.autoconfig.hexal.option.server.itemSpells.bindTemporaryStorageCost")
                    .defineInRange("bindTemporaryStorageCost", DEFAULT_BIND_TEMPORARY_STORAGE_COST, DEF_MIN_COST, DEF_MAX_COST);
            makeItemCost = builder.translation("text.autoconfig.hexal.option.server.itemSpells.makeItemCost")
                    .defineInRange("makeItemCost", DEFAULT_MAKE_ITEM_COST, DEF_MIN_COST, DEF_MAX_COST);
            returnItemCost = builder.translation("text.autoconfig.hexal.option.server.itemSpells.returnItemCost")
                    .defineInRange("returnItemCost", DEFAULT_RETURN_ITEM_COST, DEF_MIN_COST, DEF_MAX_COST);
            craftItemCost = builder.translation("text.autoconfig.hexal.option.server.itemSpells.craftItemCost")
                    .defineInRange("craftItemCost", DEFAULT_CRAFT_ITEM_COST, DEF_MIN_COST, DEF_MAX_COST);
            tradeItemCost = builder.translation("text.autoconfig.hexal.option.server.itemSpells.tradeItemCost")
                    .defineInRange("tradeItemCost", DEFAULT_TRADE_ITEM_COST, DEF_MIN_COST, DEF_MAX_COST);
            useItemOnCost = builder.translation("text.autoconfig.hexal.option.server.itemSpells.useItemOnCost")
                    .defineInRange("useItemOnCost", DEFAULT_USE_ITEM_ON_COST, DEF_MIN_COST, DEF_MAX_COST);
            maxItemsReturned = builder.translation("text.autoconfig.hexal.option.server.itemSpells.maxItemsReturned")
                    .comment("Maximum number of items that can be returned to the world as item entities in a single OpReturnMote call.")
                    .defineInRange("maxItemsReturned", DEFAULT_MAX_ITEMS_RETURNED, MIN_MAX_ITEMS_RETURNED, MAX_MAX_ITEMS_RETURNED);
            maxRecordsInMediafiedStorage = builder.translation("text.autoconfig.hexal.option.server.itemSpells.maxRecordsInMediafiedStorage")
                    .comment("Maximum number of Item Records (the things Item Iotas point at) that can be stored in a Mediafied Storage block.")
                    .defineInRange("maxRecordsInMediafiedStorage", DEFAULT_MAX_RECORDS_IN_MEDIAFIED_STORAGE, MIN_MAX_RECORDS_IN_MEDIAFIED_STORAGE, MAX_MAX_RECORDS_IN_MEDIAFIED_STORAGE);

            builder.pop();


            // costs of great spells
            builder.translation("text.autoconfig.hexal.option.server.greatSpells").push("greatSpells");
            consumeWispOwnCost = builder.translation("text.autoconfig.hexal.option.server.greatSpells.consumeWispOwnCost")
                    .comment(
                            "Cost to consume a wisp you own,",
                            "or that doesn't fight back for some other reason."
                    )
                    .defineInRange("consumeWispOwnCost", DEFAULT_CONSUME_WISP_OWN_COST, DEF_MIN_COST, DEF_MAX_COST);
            consumeWispOthersCostPerMedia = builder.translation("text.autoconfig.hexal.option.server.greatSpells.consumeWispOthersCostPerMedia")
                    .comment(
                            "Cost to consume a wisp that fights back, this number is multiplied by",
                            "the media possessed by the wisp you are attempting to consume."
                    )
                    .defineInRange("consumeWispOthersCostPerMedia", DEFAULT_CONSUME_WISP_OTHERS_COST_PER_MEDIA, MIN_CONSUME_WISP_OTHERS_COST_PER_MEDIA, MAX_CONSUME_WISP_OTHERS_COST_PER_MEDIA);
            seonWispSetCost = builder.translation("text.autoconfig.hexal.option.server.greatSpells.seonWispSetCost")
                    .defineInRange("seonWispSetCost", DEFAULT_SEON_WISP_SET_COST, DEF_MIN_COST, DEF_MAX_COST);
            tickConstantCost = builder.translation("text.autoconfig.hexal.option.server.greatSpells.tickConstantCost")
                    .comment("Constant cost to tick a block. Always applies.")
                    .defineInRange("tickConstantCost", DEFAULT_TICK_CONSTANT_COST, DEF_MIN_COST, DEF_MAX_COST);
            tickCostPerTicked = builder.translation("text.autoconfig.hexal.option.server.greatSpells.tickCostPerTicked")
                    .comment(
                            "Cost to tick a block per time that block has already been ticked.",
                            "Added to the constant cost above."
                    )
                    .defineInRange("tickCostPerTicked", DEFAULT_TICK_COST_PER_TICKED, DEF_MIN_COST, DEF_MAX_COST);
            tickRandomTickIProb = builder.translation("text.autoconfig.hexal.option.server.greatSpells.tickRandomTickIProb")
                    .comment(
                            "The inverse probability of tick randomly ticking a block.",
                            "Higher numbers make random ticks less likely, lower numbers make them more likely."
                    )
                    .defineInRange("tickRandomTickIProb", DEFAULT_TICK_RANDOM_TICK_I_PROB, MIN_TICK_RANDOM_TICK_I_PROB, MAX_TICK_RANDOM_TICK_I_PROB);


            accelerateDenyList = builder.comment("Resource locations of blocks you can't accelerate.")
                    .defineList("accelerateDenyList", HexalConfig.ServerConfigAccess.Companion.getDEFAULT_ACCELERATE_DENY_LIST(), ForgeHexalConfig.Server::isValidReslocArg);

            builder.pop();
        }

        private static boolean isValidReslocArg(Object o) {
            return o instanceof String s && ResourceLocation.isValidResourceLocation(s);
        }

        //region getters
        @Override
        public boolean getGenerateSlipwayGeodes() {
            return generateSlipwayGeodes.get();
        }

        @Override
        public long getFallingBlockCost() {
            return (long) (fallingBlockCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getFreezeCost() {
            return (long) (freezeCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getParticlesCost() {
            return (long) (particlesCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getPlaceTypeCost() {
            return (long) (placeTypeCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getSmeltCost() {
            return (long) (smeltCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getPhaseBlockCostFactor() {
            return 0;
        }

        @Override
        public long getMoveSpeedSetCost() {
            return (long) (moveSpeedSetCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getSummonTickingWispCost() {
            return (long) (summonTickingWispCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getSummonProjectileWispCost() {
            return (long) (summonProjectileWispCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getSummonProjectileWispMinCost() {
            return (long) (summonProjectileWispMinCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getTickingWispUpkeepPerTick() {
            return (long) (tickingWispUpkeepPerTick.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getProjectileWispUpkeepPerTick() {
            return (long) (projectileWispUpkeepPerTick.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getUntriggeredWispUpkeepDiscount() {
            return untriggeredWispUpkeepDiscount.get();
        }

        @Override
        public long getLinkUpkeepPerTick() {
            return (long) (linkUpkeepPerTick.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getSeonDiscountFactor() {
            return seonDiscountFactor.get();
        }

        @Override
        public double getStoringPlayerCostScaleFactor() {
            return storingPlayerCostScaleFactor.get();
        }

        @Override
        public double getMediaFlowRateOverLink() {
            return mediaFlowRateOverLink.get();
        }

        @Override
        public long getLinkCost() {
            return (long) (linkCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getSendIotaCost() {
            return (long) (sendIotaCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getUnlinkCost() {
            return (long) (unlinkCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getMakeGateCost() {
            return (long) (makeGateCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getMarkGateCost() {
            return (long) (markGateCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getCloseGateCost() {
            return (long) (closeGateCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getCloseGateDistanceCostFactor() {
            return (long) (closeGateDistanceCostFactor.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getMaxGateOffset() {
            return maxGateOffset.get();
        }

        @Override
        public long getBindStorageCost() {
            return (long) (bindStorageCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getBindTemporaryStorageCost() {
            return (long) (bindTemporaryStorageCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getMakeItemCost() {
            return (long) (makeItemCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getReturnItemCost() {
            return (long) (returnItemCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getCraftItemCost() {
            return (long) (craftItemCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getTradeItemCost() {
            return (long) (tradeItemCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getUseItemOnCost() {return (long) (useItemOnCost.get() * MediaConstants.DUST_UNIT); }

        @Override
        public int getMaxItemsReturned() {
            return maxItemsReturned.get();
        }

        @Override
        public int getMaxRecordsInMediafiedStorage() {
            return maxRecordsInMediafiedStorage.get();
        }

        @Override
        public long getConsumeWispOwnCost() {
            return (long) (consumeWispOwnCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getConsumeWispOthersCostPerMedia() {
            return consumeWispOthersCostPerMedia.get();
        }

        @Override
        public long getSeonWispSetCost() {
            return (long) (seonWispSetCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getTickConstantCost() {
            return (long) (tickConstantCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public long getTickCostPerTicked() {
            return (long) (tickCostPerTicked.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickRandomTickIProb() {
            return tickRandomTickIProb.get();
        }

        @Override
        public boolean isAccelerateAllowed(@NotNull ResourceLocation blockId) {
            return noneMatch(accelerateDenyList.get(), blockId);
        }
        //endregion
    }
}
