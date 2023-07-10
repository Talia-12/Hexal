package ram.talia.hexal.datagen

import at.petrak.paucal.api.datagen.PaucalBlockTagProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import ram.talia.hexal.common.lib.HexalBlocks
import java.util.concurrent.CompletableFuture

class HexalBlockTagProvider(packout: PackOutput, lookup: CompletableFuture<HolderLookup.Provider>) : PaucalBlockTagProvider(packout, lookup) {
    override fun addTags(lookup: HolderLookup.Provider) {
        add(tag(BlockTags.MINEABLE_WITH_PICKAXE),
            HexalBlocks.MEDIAFIED_STORAGE)
    }

    private fun add(appender: TagAppender<Block>, vararg blocks: Block) {
        for (block in blocks) {
            appender.add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow())
        }
    }
}