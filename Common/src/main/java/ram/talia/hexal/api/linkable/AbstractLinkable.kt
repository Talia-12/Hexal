package ram.talia.hexal.api.linkable

import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.nbt.SerialisedIotaList

abstract class AbstractLinkable : ILinkable {
    override val _lazyLinked = if (_level.isClientSide) null else ILinkable.LazyILinkableList(_level as ServerLevel)
    override val _lazyRenderLinks = if (_level.isClientSide) null else ILinkable.LazyILinkableList(_level as ServerLevel)
    override val _serReceivedIotas = SerialisedIotaList(null)
}