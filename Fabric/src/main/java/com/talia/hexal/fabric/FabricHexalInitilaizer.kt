package com.talia.hexal.fabric

import com.talia.hexal.api.HexalAPI
import com.talia.hexal.common.casting.RegisterPatterns
import net.fabricmc.api.ModInitializer

object FabricHexalInitializer : ModInitializer {
    override fun onInitialize() {
        HexalAPI.LOGGER.info("Hello Fabric World!"
    }
}