package ram.talia.hexal.api.linkable

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import ram.talia.hexal.client.playLinkParticles

class ClientLinkableHolder(private val thisLinkable: ILinkable.IRenderCentre, private val level: Level, private val random: RandomSource) {
    private val renderLinks: MutableMap<CompoundTag, ILinkable.IRenderCentre> = mutableMapOf()

    fun addRenderLink(otherTag: CompoundTag, other: ILinkable.IRenderCentre) {
        renderLinks[otherTag] = other
    }
    fun removeRenderLink(otherTag: CompoundTag) = renderLinks.remove(otherTag)
    fun setRenderLinks(newLinks: Map<CompoundTag, ILinkable.IRenderCentre>) {
        renderLinks.clear()
        renderLinks.putAll(newLinks)
    }

    /**
     * Should be called every tick on the client to display the links.
     */
    fun renderLinks() {
        renderLinks.forEach { playLinkParticles(thisLinkable, it.value, random, level) }
    }
}