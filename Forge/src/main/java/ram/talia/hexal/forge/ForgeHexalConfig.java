package ram.talia.hexal.forge;


import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraftforge.common.ForgeConfigSpec;
import ram.talia.hexal.api.config.HexalConfig;

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

        // costs of link spells
        private static ForgeConfigSpec.DoubleValue linkCost;
        private static ForgeConfigSpec.DoubleValue sendIotaCost;
        private static ForgeConfigSpec.DoubleValue unlinkCost;

        // costs of gate spells
        private static ForgeConfigSpec.DoubleValue makeGateCost;
        private static ForgeConfigSpec.DoubleValue markGateCost;
        private static ForgeConfigSpec.DoubleValue closeGateCost;

        // costs of great spells
        private static ForgeConfigSpec.DoubleValue consumeWispOwnCost;
        private static ForgeConfigSpec.DoubleValue consumeWispOthersCostPerMedia;
        private static ForgeConfigSpec.DoubleValue seonWispSetCost;
        private static ForgeConfigSpec.DoubleValue tickConstantCost;
        private static ForgeConfigSpec.DoubleValue tickCostPerTicked;
        private static ForgeConfigSpec.IntValue tickRandomTickIProb;

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

            builder.pop();
        }

        //region getters
        @Override
        public boolean getGenerateSlipwayGeodes() {
            return generateSlipwayGeodes.get();
        }

        @Override
        public int getFallingBlockCost() {
            return (int) (fallingBlockCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getFreezeCost() {
            return (int) (freezeCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getParticlesCost() {
            return (int) (particlesCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getPlaceTypeCost() {
            return (int) (placeTypeCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSmeltCost() {
            return (int) (smeltCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getMoveSpeedSetCost() {
            return (int) (moveSpeedSetCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSummonTickingWispCost() {
            return (int) (summonTickingWispCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSummonProjectileWispCost() {
            return (int) (summonProjectileWispCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSummonProjectileWispMinCost() {
            return (int) (summonProjectileWispMinCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickingWispUpkeepPerTick() {
            return (int) (tickingWispUpkeepPerTick.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getProjectileWispUpkeepPerTick() {
            return (int) (projectileWispUpkeepPerTick.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getUntriggeredWispUpkeepDiscount() {
            return untriggeredWispUpkeepDiscount.get();
        }

        @Override
        public int getLinkUpkeepPerTick() {
            return (int) (linkUpkeepPerTick.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getSeonDiscountFactor() {
            return seonDiscountFactor.get();
        }

        @Override
        public int getLinkCost() {
            return (int) (linkCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getSendIotaCost() {
            return (int) (sendIotaCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getUnlinkCost() {
            return (int) (unlinkCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getMakeGateCost() {
            return (int) (makeGateCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getMarkGateCost() {
            return (int) (markGateCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getCloseGateCost() {
            return (int) (closeGateCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getConsumeWispOwnCost() {
            return (int) (consumeWispOwnCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public double getConsumeWispOthersCostPerMedia() {
            return consumeWispOthersCostPerMedia.get();
        }

        @Override
        public int getSeonWispSetCost() {
            return (int) (seonWispSetCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickConstantCost() {
            return (int) (tickConstantCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickCostPerTicked() {
            return (int) (tickCostPerTicked.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public int getTickRandomTickIProb() {
            return tickRandomTickIProb.get();
        }
        //endregion
    }
}
