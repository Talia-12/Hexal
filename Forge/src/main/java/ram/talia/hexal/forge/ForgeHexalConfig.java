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
        private static ForgeConfigSpec.IntValue maxMatrixSize;
        private static ForgeConfigSpec.IntValue maxStringLength;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("Spells");
            maxMatrixSize = builder.comment("How large can matrices be")
                    .defineInRange("maxMatrixSize", DEFAULT_MAX_MATRIX_SIZE, MIN_MAX_MATRIX_SIZE, MAX_MAX_MATRIX_SIZE);
            maxStringLength = builder.comment("How long can strings be")
                    .defineInRange("maxStringLength", DEFAULT_MAX_STRING_LENGTH, MIN_MAX_STRING_LENGTH, MAX_MAX_STRING_LENGTH);
        }

            @Override
        public int getMaxMatrixSize() {
            return maxMatrixSize.get();
        }

        @Override
        public int getMaxStringLength() {
            return maxStringLength.get();
        }
    }
}
