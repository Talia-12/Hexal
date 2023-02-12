package ram.talia.hexal.api.config

import at.petrak.hexcasting.api.HexAPI
import net.minecraft.resources.ResourceLocation

object HexalConfig {
    interface CommonConfigAccess { }

    interface ClientConfigAccess { }

    interface ServerConfigAccess {
        val maxMatrixSize: Int
        val maxStringLength: Int

        companion object {
            const val DEFAULT_MAX_MATRIX_SIZE: Int = 144
            const val MIN_MAX_MATRIX_SIZE: Int = 3
            const val MAX_MAX_MATRIX_SIZE: Int = 512
            const val DEFAULT_MAX_STRING_LENGTH: Int = 1728
            const val MIN_MAX_STRING_LENGTH: Int = 1
            const val MAX_MAX_STRING_LENGTH: Int = 32768
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

    private class DummyCommon : CommonConfigAccess {  }
    private class DummyClient : ClientConfigAccess {  }
    private class DummyServer : ServerConfigAccess {
        override val maxMatrixSize: Int
            get() = TODO("Not yet implemented")
        override val maxStringLength: Int
            get() = TODO("Not yet implemented")
    }

    @JvmStatic
    var common: CommonConfigAccess = DummyCommon()
        set(access) {
            if (field !is DummyCommon) {
                HexAPI.LOGGER.warn("CommonConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }

    @JvmStatic
    var client: ClientConfigAccess = DummyClient()
        set(access) {
            if (field !is DummyClient) {
                HexAPI.LOGGER.warn("ClientConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }

    @JvmStatic
    var server: ServerConfigAccess = DummyServer()
        set(access) {
            if (field !is DummyServer) {
                HexAPI.LOGGER.warn("ServerConfigAccess was replaced! Old {} New {}",
                        field.javaClass.name, access.javaClass.name)
            }
            field = access
        }
}