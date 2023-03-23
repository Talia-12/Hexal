package ram.talia.hexal.datagen

import at.petrak.paucal.api.datagen.PaucalBlockTagProvider
import net.minecraft.data.DataGenerator
import net.minecraft.tags.BlockTags
import ram.talia.hexal.common.lib.HexalBlocks

class HexalBlockTagProvider(pGenerator: DataGenerator) : PaucalBlockTagProvider(pGenerator) {
    override fun addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(HexalBlocks.MEDIAFIED_STORAGE)
    }
}