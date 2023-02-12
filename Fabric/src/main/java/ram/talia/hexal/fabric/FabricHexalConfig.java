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

//        @ConfigEntry.BoundedDiscrete(min = MIN_MAX_MATRIX_SIZE, max = MAX_MAX_MATRIX_SIZE)
//        @ConfigEntry.Gui.Tooltip
//        private int maxMatrixSize = DEFAULT_MAX_MATRIX_SIZE;
//
//        @ConfigEntry.BoundedDiscrete(min = MIN_MAX_STRING_LENGTH, max = MAX_MAX_STRING_LENGTH)
//        @ConfigEntry.Gui.Tooltip
//        private int maxStringLength = DEFAULT_MAX_STRING_LENGTH;

        @Override
        public void validatePostLoad() throws ValidationException {

//            this.maxMatrixSize = bound(this.maxMatrixSize, MIN_MAX_MATRIX_SIZE, MAX_MAX_MATRIX_SIZE);
//            this.maxStringLength = bound(this.maxStringLength, MIN_MAX_STRING_LENGTH, MAX_MAX_STRING_LENGTH);
        }

        private int bound(int toBind, int lower, int upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }


        @Override
        public boolean getGenerateSlipwayGeodes() {
            return generateSlipwayGeodes;
        }
    }
}
