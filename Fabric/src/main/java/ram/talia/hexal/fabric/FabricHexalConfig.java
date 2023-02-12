package ram.talia.hexal.fabric;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.xplat.IXplatAbstractions;

@Config(name = HexalAPI.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/calcite.png")
public class FabricHexalConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.TransitiveObject
    public final Common common = new Common();
    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public final Client client = new Client();
    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    public final Server server = new Server();

    public static FabricHexalConfig setup() {
        AutoConfig.register(FabricHexalConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        var instance = AutoConfig.getConfigHolder(FabricHexalConfig.class).getConfig();

        HexalConfig.setCommon(instance.common);
        // We care about the client only on the *physical* client ...
        if (IXplatAbstractions.INSTANCE.isPhysicalClient()) {
            HexalConfig.setClient(instance.client);
        }
        // but we care about the server on the *logical* server
        // i believe this should Just Work without a guard? assuming we don't access it from the client ever
        HexalConfig.setServer(instance.server);

        return instance;
    }


    @Config(name = "common")
    private static class Common implements ConfigData, HexalConfig.CommonConfigAccess { }

    @Config(name = "client")
    private static class Client implements ConfigData, HexalConfig.ClientConfigAccess { }


    @Config(name = "server")
    private static class Server implements ConfigData, HexalConfig.ServerConfigAccess {

        @ConfigEntry.Gui.Tooltip
        private boolean generateSlipwayGeodes = DEFAULT_GENERATE_SLIPWAY_GEODES;

        @ConfigEntry.Gui.CollapsibleObject
        private MiscSpells miscSpells = new MiscSpells();

        static class MiscSpells {
            // costs of misc spells
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int fallingBlockCost = DEFAULT_FALLING_BLOCK_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int freezeCost = DEFAULT_FREEZE_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int particlesCost = DEFAULT_PARTICLES_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int placeTypeCost = DEFAULT_PLACE_TYPE_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int smeltCost = DEFAULT_SMELT_COST;
        }

        @ConfigEntry.Gui.CollapsibleObject
        private WispSpells wispSpells = new WispSpells();

        static class WispSpells {
            // costs of wisp spells
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int moveSpeedSetCost = DEFAULT_MOVE_SPEED_SET_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int summonTickingWispCost = DEFAULT_SUMMON_TICKING_WISP_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int summonProjectileWispCost = DEFAULT_SUMMON_PROJECTILE_WISP_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int summonProjectileWispMinCost = DEFAULT_SUMMON_PROJECTILE_WISP_MIN_COST;
        }

        @ConfigEntry.Gui.CollapsibleObject
        private WispUpkeep wispUpkeep = new WispUpkeep();

        static class WispUpkeep {
            // costs of wisp upkeep
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int tickingWispUpkeepPerTick = DEFAULT_TICKING_WISP_UPKEEP_PER_TICK;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int projectileWispUpkeepPerTick = DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK;
            @ConfigEntry.Gui.Tooltip
            double untriggeredWispUpkeepDiscount = DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int linkUpkeepPerTick = DEFAULT_LINK_UPKEEP_PER_TICK;
            @ConfigEntry.Gui.Tooltip
            double seonDiscountFactor = DEFAULT_SEON_DISCOUNT_FACTOR;
        }


        @ConfigEntry.Gui.CollapsibleObject
        private LinkSpells linkSpells = new LinkSpells();

        static class LinkSpells {
            // costs of link spells
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int linkCost = DEFAULT_LINK_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int sendIotaCost = DEFAULT_SEND_IOTA_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int unlinkCost = DEFAULT_UNLINK_COST;
        }


        @ConfigEntry.Gui.CollapsibleObject
        private GateSpells gateSpells = new GateSpells();

        static class GateSpells {
            // costs of gate spells
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int makeGateCost = DEFAULT_MAKE_GATE_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int markGateCost = DEFAULT_MARK_GATE_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int closeGateCost = DEFAULT_CLOSE_GATE_COST;
        }


        @ConfigEntry.Gui.CollapsibleObject
        private GreatSpells greatSpells = new GreatSpells();

        static class GreatSpells {
            // costs of great spells
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int consumeWispOwnCost = DEFAULT_CONSUME_WISP_OWN_COST;
            @ConfigEntry.Gui.Tooltip
            double consumeWispOthersCostPerMedia = DEFAULT_CONSUME_WISP_OTHERS_COST_PER_MEDIA;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int seonWispSetCost = DEFAULT_SEON_WISP_SET_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int tickConstantCost = DEFAULT_TICK_CONSTANT_COST;
            @ConfigEntry.BoundedDiscrete(min = DEF_MIN_COST, max = DEF_MAX_COST)
            @ConfigEntry.Gui.Tooltip
            int tickCostPerTicked = DEFAULT_TICK_COST_PER_TICKED;
            @ConfigEntry.BoundedDiscrete(min = MIN_TICK_RANDOM_TICK_I_PROB, max = MAX_TICK_RANDOM_TICK_I_PROB)
            @ConfigEntry.Gui.Tooltip
            int tickRandomTickIProb = DEFAULT_TICK_RANDOM_TICK_I_PROB;
        }



        @Override
        public void validatePostLoad() throws ValidationException {
            // costs of misc spells
            this.miscSpells.fallingBlockCost = bound(this.miscSpells.fallingBlockCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.freezeCost = bound(this.miscSpells.freezeCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.particlesCost = bound(this.miscSpells.particlesCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.placeTypeCost = bound(this.miscSpells.placeTypeCost, DEF_MIN_COST, DEF_MAX_COST);
            this.miscSpells.smeltCost = bound(this.miscSpells.smeltCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of wisp spells
            this.wispSpells.moveSpeedSetCost = bound(this.wispSpells.moveSpeedSetCost, DEF_MIN_COST, DEF_MAX_COST);
            this.wispSpells.summonTickingWispCost = bound(this.wispSpells.summonTickingWispCost, DEF_MIN_COST, DEF_MAX_COST);
            this.wispSpells.summonProjectileWispCost = bound(this.wispSpells.summonProjectileWispCost, DEF_MIN_COST, DEF_MAX_COST);
            this.wispSpells.summonProjectileWispMinCost = bound(this.wispSpells.summonProjectileWispMinCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of wisp upkeep
            this.wispUpkeep.tickingWispUpkeepPerTick = bound(this.wispUpkeep.tickingWispUpkeepPerTick, DEF_MIN_COST, DEF_MAX_COST);
            this.wispUpkeep.projectileWispUpkeepPerTick = bound(this.wispUpkeep.projectileWispUpkeepPerTick, DEF_MIN_COST, DEF_MAX_COST);
            this.wispUpkeep.untriggeredWispUpkeepDiscount = bound(this.wispUpkeep.untriggeredWispUpkeepDiscount, MIN_UNTRIGGERED_WISP_UPKEEP_DISCOUNT, MAX_UNTRIGGERED_WISP_UPKEEP_DISCOUNT);
            this.wispUpkeep.linkUpkeepPerTick = bound(this.wispUpkeep.linkUpkeepPerTick, DEF_MIN_COST, DEF_MAX_COST);
            this.wispUpkeep.seonDiscountFactor = bound(this.wispUpkeep.seonDiscountFactor, MIN_SEON_DISCOUNT_FACTOR, MAX_SEON_DISCOUNT_FACTOR);

            // costs of link spells
            this.linkSpells.linkCost = bound(this.linkSpells.linkCost, DEF_MIN_COST, DEF_MAX_COST);
            this.linkSpells.sendIotaCost = bound(this.linkSpells.sendIotaCost, DEF_MIN_COST, DEF_MAX_COST);
            this.linkSpells.unlinkCost = bound(this.linkSpells.unlinkCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of gate spells
            this.gateSpells.makeGateCost = bound(this.gateSpells.makeGateCost, DEF_MIN_COST, DEF_MAX_COST);
            this.gateSpells.markGateCost = bound(this.gateSpells.markGateCost, DEF_MIN_COST, DEF_MAX_COST);
            this.gateSpells.closeGateCost = bound(this.gateSpells.closeGateCost, DEF_MIN_COST, DEF_MAX_COST);

            // costs of great spells
            this.greatSpells.consumeWispOwnCost = bound(this.greatSpells.consumeWispOwnCost, DEF_MIN_COST, DEF_MAX_COST);
            this.greatSpells.consumeWispOthersCostPerMedia = bound(this.greatSpells.consumeWispOthersCostPerMedia, MIN_CONSUME_WISP_OTHERS_COST_PER_MEDIA, MAX_CONSUME_WISP_OTHERS_COST_PER_MEDIA);
            this.greatSpells.seonWispSetCost = bound(this.greatSpells.seonWispSetCost, DEF_MIN_COST, DEF_MAX_COST);
            this.greatSpells.tickConstantCost = bound(this.greatSpells.tickConstantCost, DEF_MIN_COST, DEF_MAX_COST);
            this.greatSpells.tickCostPerTicked = bound(this.greatSpells.tickCostPerTicked, DEF_MIN_COST, DEF_MAX_COST);

            this.greatSpells.tickRandomTickIProb = bound(this.greatSpells.tickRandomTickIProb, MIN_TICK_RANDOM_TICK_I_PROB, MAX_TICK_RANDOM_TICK_I_PROB);
        }

        private int bound(int toBind, int lower, int upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }
        private double bound(double toBind, double lower, double upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }


        //region getters
        @Override
        public boolean getGenerateSlipwayGeodes() {
            return generateSlipwayGeodes;
        }

        @Override
        public int getFallingBlockCost() {
            return miscSpells.fallingBlockCost;
        }

        @Override
        public int getFreezeCost() {
            return miscSpells.freezeCost;
        }

        @Override
        public int getParticlesCost() {
            return miscSpells.particlesCost;
        }

        @Override
        public int getPlaceTypeCost() {
            return miscSpells.placeTypeCost;
        }

        @Override
        public int getSmeltCost() {
            return miscSpells.smeltCost;
        }

        @Override
        public int getMoveSpeedSetCost() {
            return wispSpells.moveSpeedSetCost;
        }

        @Override
        public int getSummonTickingWispCost() {
            return wispSpells.summonTickingWispCost;
        }

        @Override
        public int getSummonProjectileWispCost() {
            return wispSpells.summonProjectileWispCost;
        }

        @Override
        public int getSummonProjectileWispMinCost() {
            return wispSpells.summonProjectileWispMinCost;
        }

        @Override
        public int getTickingWispUpkeepPerTick() {
            return wispUpkeep.tickingWispUpkeepPerTick;
        }

        @Override
        public int getProjectileWispUpkeepPerTick() {
            return wispUpkeep.projectileWispUpkeepPerTick;
        }

        @Override
        public double getUntriggeredWispUpkeepDiscount() {
            return wispUpkeep.untriggeredWispUpkeepDiscount;
        }

        @Override
        public int getLinkUpkeepPerTick() {
            return wispUpkeep.linkUpkeepPerTick;
        }

        @Override
        public double getSeonDiscountFactor() {
            return wispUpkeep.seonDiscountFactor;
        }

        @Override
        public int getLinkCost() {
            return linkSpells.linkCost;
        }

        @Override
        public int getSendIotaCost() {
            return linkSpells.sendIotaCost;
        }

        @Override
        public int getUnlinkCost() {
            return linkSpells.unlinkCost;
        }

        @Override
        public int getMakeGateCost() {
            return gateSpells.makeGateCost;
        }

        @Override
        public int getMarkGateCost() {
            return gateSpells.markGateCost;
        }

        @Override
        public int getCloseGateCost() {
            return gateSpells.closeGateCost;
        }

        @Override
        public int getConsumeWispOwnCost() {
            return greatSpells.consumeWispOwnCost;
        }

        @Override
        public double getConsumeWispOthersCostPerMedia() {
            return greatSpells.consumeWispOthersCostPerMedia;
        }

        @Override
        public int getSeonWispSetCost() {
            return greatSpells.seonWispSetCost;
        }

        @Override
        public int getTickConstantCost() {
            return greatSpells.tickConstantCost;
        }

        @Override
        public int getTickCostPerTicked() {
            return greatSpells.tickCostPerTicked;
        }

        @Override
        public int getTickRandomTickIProb() {
            return greatSpells.tickRandomTickIProb;
        }
        //endregion
    }
}
