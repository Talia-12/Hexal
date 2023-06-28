package ram.talia.hexal.datagen

import at.petrak.paucal.api.datagen.PaucalLootTableProvider
import net.minecraft.data.DataGenerator
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootTable
import ram.talia.hexal.common.lib.HexalBlocks

class HexalLootTables(pGenerator: DataGenerator) : PaucalLootTableProvider(pGenerator) {
    override fun makeLootTables(blockTables: MutableMap<Block, LootTable.Builder>, lootTables: MutableMap<ResourceLocation, LootTable.Builder>) {
        dropSelf(blockTables, HexalBlocks.MEDIAFIED_STORAGE)
    }
}