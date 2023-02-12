package ram.talia.hexal.forge;


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
        private static ForgeConfigSpec.IntValue fallingBlockCost;
        private static ForgeConfigSpec.IntValue freezeCost;
        private static ForgeConfigSpec.IntValue particlesCost;
        private static ForgeConfigSpec.IntValue placeTypeCost;
        private static ForgeConfigSpec.IntValue smeltCost;

        // costs of wisp spells
        private static ForgeConfigSpec.IntValue moveSpeedSetCost;
        private static ForgeConfigSpec.IntValue summonTickingWispCost;
        private static ForgeConfigSpec.IntValue summonProjectileWispCost;
        private static ForgeConfigSpec.IntValue summonProjectileWispMinCost;

        // costs of wisp upkeep
        private static ForgeConfigSpec.IntValue tickingWispUpkeepPerTick;
        private static ForgeConfigSpec.IntValue projectileWispUpkeepPerTick;
        private static ForgeConfigSpec.DoubleValue untriggeredWispUpkeepDiscount;
        private static ForgeConfigSpec.IntValue linkUpkeepPerTick;

        // costs of link spells
        private static ForgeConfigSpec.IntValue linkCost;
        private static ForgeConfigSpec.IntValue sendIotaCost;
        private static ForgeConfigSpec.IntValue unlinkCost;

        // costs of gate spells
        private static ForgeConfigSpec.IntValue makeGateCost;
        private static ForgeConfigSpec.IntValue markGateCost;
        private static ForgeConfigSpec.IntValue closeGateCost;

        // costs of great spells
        private static ForgeConfigSpec.IntValue consumeWispOwnCost;
        private static ForgeConfigSpec.DoubleValue consumeWispOthersCostPerMedia;
        private static ForgeConfigSpec.IntValue seonWispSetCost;
        private static ForgeConfigSpec.IntValue tickConstantCost;
        private static ForgeConfigSpec.IntValue tickCostPerTicked;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("Misc Spells");
            // costs of misc spells
            fallingBlockCost = builder.comment("")
                    .defineInRange("fallingBlockCost", DEFAULT_FALLING_BLOCK_COST, DEF_MIN_COST, DEF_MAX_COST);
            freezeCost = builder.comment("")
                    .defineInRange("freezeCost", DEFAULT_FREEZE_COST, DEF_MIN_COST, DEF_MAX_COST);
            particlesCost = builder.comment("")
                    .defineInRange("particlesCost", DEFAULT_PARTICLES_COST, DEF_MIN_COST, DEF_MAX_COST);
            placeTypeCost = builder.comment("")
                    .defineInRange("placeTypeCost", DEFAULT_PLACE_TYPE_COST, DEF_MIN_COST, DEF_MAX_COST);
            smeltCost = builder.comment("")
                    .defineInRange("smeltCost", DEFAULT_SMELT_COST, DEF_MIN_COST, DEF_MAX_COST);

            // costs of wisp spells
            moveSpeedSetCost = builder.comment("")
                    .defineInRange("moveSpeedSetCost", DEFAULT_MOVE_SPEED_SET_COST, DEF_MIN_COST, DEF_MAX_COST);
            summonTickingWispCost = builder.comment("")
                    .defineInRange("summonTickingWispCost", DEFAULT_SUMMON_TICKING_WISP_COST, DEF_MIN_COST, DEF_MAX_COST);
            summonProjectileWispCost = builder.comment("")
                    .defineInRange("summonProjectileWispCost", DEFAULT_SUMMON_PROJECTILE_WISP_COST, DEF_MIN_COST, DEF_MAX_COST);
            summonProjectileWispMinCost = builder.comment("")
                    .defineInRange("summonProjectileWispMinCost", DEFAULT_SUMMON_PROJECTILE_WISP_MIN_COST, DEF_MIN_COST, DEF_MAX_COST);

            // costs of wisp upkeep
            tickingWispUpkeepPerTick = builder.comment("")
                    .defineInRange("tickingWispUpkeepPerTick", DEFAULT_TICKING_WISP_UPKEEP_PER_TICK, DEF_MIN_COST, DEF_MAX_COST);
            projectileWispUpkeepPerTick = builder.comment("")
                    .defineInRange("projectileWispUpkeepPerTick", DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK, DEF_MIN_COST, DEF_MAX_COST);
            untriggeredWispUpkeepDiscount = builder.comment("")
                    .defineInRange("untriggeredWispUpkeepDiscount", DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT, MIN_UNTRIGGERED_WISP_UPKEEP_DISCOUNT, MAX_UNTRIGGERED_WISP_UPKEEP_DISCOUNT);
            linkUpkeepPerTick = builder.comment("")
                    .defineInRange("linkUpkeepPerTick", DEFAULT_LINK_UPKEEP_PER_TICK, DEF_MIN_COST, DEF_MAX_COST);

            // costs of link spells
            linkCost = builder.comment("")
                    .defineInRange("linkCost", DEFAULT_LINK_COST, DEF_MIN_COST, DEF_MAX_COST);
            sendIotaCost = builder.comment("")
                    .defineInRange("sendIotaCost", DEFAULT_SEND_IOTA_COST, DEF_MIN_COST, DEF_MAX_COST);
            unlinkCost = builder.comment("")
                    .defineInRange("unlinkCost", DEFAULT_UNLINK_COST, DEF_MIN_COST, DEF_MAX_COST);

            // costs of gate spells
            makeGateCost = builder.comment("")
                    .defineInRange("makeGateCost", DEFAULT_MAKE_GATE_COST, DEF_MIN_COST, DEF_MAX_COST);
            markGateCost = builder.comment("")
                    .defineInRange("markGateCost", DEFAULT_MARK_GATE_COST, DEF_MIN_COST, DEF_MAX_COST);
            closeGateCost = builder.comment("")
                    .defineInRange("closeGateCost", DEFAULT_CLOSE_GATE_COST, DEF_MIN_COST, DEF_MAX_COST);

            // costs of great spells
            consumeWispOwnCost = builder.comment("")
                    .defineInRange("consumeWispOwnCost", DEFAULT_CONSUME_WISP_OWN_COST, DEF_MIN_COST, DEF_MAX_COST);
            consumeWispOthersCostPerMedia = builder.comment("")
                    .defineInRange("consumeWispOthersCostPerMedia", DEFAULT_CONSUME_WISP_OTHERS_COST_PER_MEDIA, MIN_CONSUME_WISP_OTHERS_COST_PER_MEDIA, MAX_CONSUME_WISP_OTHERS_COST_PER_MEDIA);
            seonWispSetCost = builder.comment("")
                    .defineInRange("seonWispSetCost", DEFAULT_SEON_WISP_SET_COST, DEF_MIN_COST, DEF_MAX_COST);
            tickConstantCost = builder.comment("")
                    .defineInRange("tickConstantCost", DEFAULT_TICK_CONSTANT_COST, DEF_MIN_COST, DEF_MAX_COST);
            tickCostPerTicked = builder.comment("")
                    .defineInRange("tickCostPerTicked", DEFAULT_TICK_COST_PER_TICKED, DEF_MIN_COST, DEF_MAX_COST);

            builder.push("Terrain Generation");
            generateSlipwayGeodes = builder.comment("Should Slipway geodes be generated?")
                    .define("generateSlipwayGeodes", DEFAULT_GENERATE_SLIPWAY_GEODES);
        }

        //region getters
        @Override
        public boolean getGenerateSlipwayGeodes() {
            return generateSlipwayGeodes.get();
        }

        @Override
        public int getFallingBlockCost() {
            return fallingBlockCost.get();
        }

        @Override
        public int getFreezeCost() {
            return freezeCost.get();
        }

        @Override
        public int getParticlesCost() {
            return particlesCost.get();
        }

        @Override
        public int getPlaceTypeCost() {
            return placeTypeCost.get();
        }

        @Override
        public int getSmeltCost() {
            return smeltCost.get();
        }

        @Override
        public int getMoveSpeedSetCost() {
            return moveSpeedSetCost.get();
        }

        @Override
        public int getSummonTickingWispCost() {
            return summonTickingWispCost.get();
        }

        @Override
        public int getSummonProjectileWispCost() {
            return summonProjectileWispCost.get();
        }

        @Override
        public int getSummonProjectileWispMinCost() {
            return summonProjectileWispMinCost.get();
        }

        @Override
        public int getTickingWispUpkeepPerTick() {
            return tickingWispUpkeepPerTick.get();
        }

        @Override
        public int getProjectileWispUpkeepPerTick() {
            return projectileWispUpkeepPerTick.get();
        }

        @Override
        public double getUntriggeredWispUpkeepDiscount() {
            return untriggeredWispUpkeepDiscount.get();
        }

        @Override
        public int getLinkUpkeepPerTick() {
            return linkUpkeepPerTick.get();
        }

        @Override
        public int getLinkCost() {
            return linkCost.get();
        }

        @Override
        public int getSendIotaCost() {
            return sendIotaCost.get();
        }

        @Override
        public int getUnlinkCost() {
            return unlinkCost.get();
        }

        @Override
        public int getMakeGateCost() {
            return makeGateCost.get();
        }

        @Override
        public int getMarkGateCost() {
            return markGateCost.get();
        }

        @Override
        public int getCloseGateCost() {
            return closeGateCost.get();
        }

        @Override
        public int getConsumeWispOwnCost() {
            return consumeWispOwnCost.get();
        }

        @Override
        public double getConsumeWispOthersCostPerMedia() {
            return consumeWispOthersCostPerMedia.get();
        }

        @Override
        public int getSeonWispSetCost() {
            return seonWispSetCost.get();
        }

        @Override
        public int getTickConstantCost() {
            return tickConstantCost.get();
        }

        @Override
        public int getTickCostPerTicked() {
            return tickCostPerTicked.get();
        }
        //endregion
    }
}
