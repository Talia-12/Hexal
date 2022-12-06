package ram.talia.hexal.api.linkable

import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import ram.talia.hexal.client.playLinkParticles

class ClientLinkableHolder(private val thisLinkable: ILinkable.IRenderCentre, private val level: Level, private val random: RandomSource) {
    private val renderLinks: MutableList<ILinkable.IRenderCentre> = mutableListOf()

    fun addRenderLink(other: ILinkable.IRenderCentre) = renderLinks.add(other)
    fun removeRenderLink(other: ILinkable.IRenderCentre) = renderLinks.remove(other)
    fun setRenderLinks(newLinks: List<ILinkable.IRenderCentre>) {
        renderLinks.clear()
        renderLinks.addAll(newLinks)
    }

    /**
     * Should be called every tick on the client to display the links.
     */
    fun renderLinks() {
        renderLinks.forEach { playLinkParticles(thisLinkable, it, random, level) }
    }
}