package ram.talia.hexal.datagen.tag

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.TagsProvider
import net.minecraft.resources.ResourceKey
import ram.talia.hexal.api.HexalAPI.modLoc
import java.util.concurrent.CompletableFuture

class HexalActionTagProvider(output: PackOutput, provider: CompletableFuture<HolderLookup.Provider>)
        : TagsProvider<ActionRegistryEntry>(output, IXplatAbstractions.INSTANCE.actionRegistry.key(), provider) {

    override fun addTags(provider: HolderLookup.Provider) {
        // In-game almost all great spells are always per-world
        for (normalGreat in arrayOf(
                "wisp/consume", "wisp/seon/set", "tick", "gate/make"
        )) {
            val loc = modLoc(normalGreat)
            val key = ResourceKey.create(IXplatAbstractions.INSTANCE.actionRegistry.key(), loc)
            tag(HexTags.Actions.REQUIRES_ENLIGHTENMENT).add(key)
            tag(HexTags.Actions.CAN_START_ENLIGHTEN).add(key)
            tag(HexTags.Actions.PER_WORLD_PATTERN).add(key)
        }
    }
}