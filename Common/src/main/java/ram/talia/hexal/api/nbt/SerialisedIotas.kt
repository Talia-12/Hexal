package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.downcast
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.getTypeFromTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import java.util.*


class SerialisedIotaList(private var tag: ListTag?, private var iotas: MutableList<Iota>?) {
    /*
     * Most of the time this will operate as a smart caching layer for an iota list that is stored in a tag.
     *
     * If it is given a raw iota list instead, then it will operate without the serialized version for as long as
     * possible. If required it will promote the raw iota list to a serialized one and revert to the other behavior.
     * */

    constructor(iotas: MutableList<Iota>?) : this(null, iotas)
    constructor(tag: ListTag?) : this(tag, null)

    constructor() : this(null, null)

    private var level: ServerLevel? = null
    private var tagReferencedEntityUUIDs: MutableList<UUID>? = null
    private var tagReferencedEntitiesAreLoaded: MutableList<Boolean>? = null

    private var iotasReferencedEntities: MutableList<Entity>? = null

    private fun scanIotaForEntities(iota: Iota, entityList: MutableList<Entity>)
    {
        when (iota.type) {
            HexIotaTypes.ENTITY -> {
                val entity = (iota as EntityIota).entity
                if (!entityList.contains(entity)) {
                    entityList.add(entity)
                }
            }
            HexIotaTypes.LIST -> {
                for (subIota in (iota as ListIota).list)
                {
                    scanIotaForEntities(subIota, entityList)
                }
            }
        }
    }

    private fun scanTagForEntities(tag: CompoundTag, referencedEntityUUIDs: MutableList<UUID>)
    {
        val type = getTypeFromTag(tag) ?: return
        val data = tag[HexIotaTypes.KEY_DATA] ?: return

        when (getTypeFromTag(tag)) {
            HexIotaTypes.ENTITY -> {
                val uuidTag = data.downcast(CompoundTag.TYPE)["uuid"] ?: return
                val uuid = NbtUtils.loadUUID(uuidTag)
                if (!referencedEntityUUIDs.contains(uuid)) {
                    referencedEntityUUIDs.add(uuid)
                }
            }
            HexIotaTypes.LIST -> {
                val listTag = data.downcast(ListTag.TYPE)
                for (sub in listTag) {
                    scanTagForEntities(sub.downcast(CompoundTag.TYPE), referencedEntityUUIDs)
                }
            }
        }
    }

    fun clear() {
        iotas = null
        tag = null
        level = null
        tagReferencedEntityUUIDs = null
        tagReferencedEntitiesAreLoaded = null
        iotasReferencedEntities = null
    }

    fun set(iotas: MutableList<Iota>) {
        this.iotas = iotas

        // Invalidate caches
        tag = null
        level = null
        tagReferencedEntityUUIDs = null
        tagReferencedEntitiesAreLoaded = null
        iotasReferencedEntities = null
    }

    fun set(tag: ListTag) {
        this.tag = tag

        // Invalidate caches
        level = null
        iotas = null
        tagReferencedEntityUUIDs = null
        tagReferencedEntitiesAreLoaded = null
        iotasReferencedEntities = null
    }

    fun getIotas(level: ServerLevel): List<Iota> {
        if (tag != null)
        {
            // We have a tag to use as master for the cached iota list

            var regenerateCache = (iotas == null) || (this.level != level)

            // Scan entities for changes in the loaded set
            if (tagReferencedEntityUUIDs != null)
            {
                for (i in 0 until tagReferencedEntityUUIDs!!.size)
                {
                    val uuid = tagReferencedEntityUUIDs!![i]
                    val entityIsLoaded = tagReferencedEntitiesAreLoaded!![i]
                    val entity = level.getEntity(uuid)
                    if ((entity != null) != entityIsLoaded)
                    {
                        regenerateCache = true
                        break
                    }
                }
            }

            if (regenerateCache)
            {
                tagReferencedEntityUUIDs = ArrayList()
                tagReferencedEntitiesAreLoaded = ArrayList()
                this.level = level
                iotas = tag?.toIotaList(level) ?: mutableListOf()

                // Update referencedEntityUUIDs and referencedEntitiesAreLoaded
                if (tag != null)
                {
                    for (innerTag in tag!!)
                    {
                        scanTagForEntities(innerTag.asCompound, tagReferencedEntityUUIDs!!)
                    }
                }

                for (uuid in tagReferencedEntityUUIDs!!)
                {
                    tagReferencedEntitiesAreLoaded!!.add(level.getEntity(uuid) != null)
                }
            }
        }
        else if (iotas != null)
        {
            // We have no tag, but we have iotas

            // Scan iota list for removed entities
            if (iotasReferencedEntities == null)
            {
                iotasReferencedEntities = ArrayList()
                for (iota in iotas!!)
                {
                    scanIotaForEntities(iota, iotasReferencedEntities!!)
                }
            }

            // If some entities have been removed, then force serialization so we don't loose the reference
            var forceSerialize = false;
            for (entity in iotasReferencedEntities!!)
            {
                if (entity.isRemoved)
                {
                    forceSerialize = true;
                    break;
                }
            }

            if (forceSerialize)
            {
                // Serialize so that removed entities are preserved, and deserialize to null them from the read list
                tag = iotas!!.toNbtList()
                // Abbreviated cache regeneration
                iotas = tag!!.toIotaList(level)
                this.level = level;

                // Invalidate caches
                iotasReferencedEntities = null
                tagReferencedEntityUUIDs = null
                tagReferencedEntitiesAreLoaded = null
            }
        }
        return iotas ?: mutableListOf()
    }

    fun getTag(): ListTag {
        if ((tag == null) && (iotas != null))
        {
            // We have an iota list, but no tag list. Time to promote the iotas to tags.
            tag = iotas!!.toNbtList()

            // Invalidate all other caches
            iotas = null
            level = null
            iotasReferencedEntities = null
            tagReferencedEntityUUIDs = null
            tagReferencedEntitiesAreLoaded = null
        }
        return tag ?: ListTag()
    }

    fun add(iota: Iota) {
        if (tag != null)
        {
            // Modify serialized version and invalidate cache
            val newTag = HexIotaTypes.serialize(iota)
            tag!!.add(newTag)

            // Invalidate caches
            this.level = null
            iotas = null
            tagReferencedEntityUUIDs = null
            tagReferencedEntitiesAreLoaded = null
            iotasReferencedEntities = null
        }
        else if (iotas != null)
        {
            // Modify deserialized version
            iotas!!.add(iota)

            // Invalidate cache
            iotasReferencedEntities = null
        }
        else {
            // Build new iota list
            iotas = mutableListOf(iota)

            // Everything else should be null at this point too, so no need to invalidate cache
        }
    }

    fun add(tag: CompoundTag) {
        if (this.tag != null)
        {
            // Modify serialized version and invalidate cache
            this.tag!!.add(tag)

            // Invalidate caches
            this.level = null
            iotas = null
            tagReferencedEntityUUIDs = null
            tagReferencedEntitiesAreLoaded = null
            iotasReferencedEntities = null
        }
        else if (iotas != null)
        {
            // Promote deserialized version to serialized
            this.tag = iotas!!.toNbtList()

            // Add tag
            this.tag!!.add(tag)

            // Invalidate cache
            this.level = null
            iotas = null
            iotasReferencedEntities = null
            tagReferencedEntityUUIDs = null
            tagReferencedEntitiesAreLoaded = null
        }
        else {
            // Build new serialized list
            this.tag = ListTag()
            this.tag!!.add(tag)

            // Everything else should be null at this point too, so no need to invalidate cache
        }
    }

    fun pop(level: ServerLevel): Iota? {
        if (tag != null)
        {
            // Modify serialized version and invalidate cache

            if (tag!!.size == 0)
            {
                return null
            }

            val poppedTag = tag!!.removeAt(0)

            // Invalidate caches
            this.level = null
            iotas = null
            tagReferencedEntityUUIDs = null
            tagReferencedEntitiesAreLoaded = null
            iotasReferencedEntities = null

            // If the list is now empty, decay to fully null (and un-opinionated between serialized/deserialized modes)
            if (tag!!.size == 0)
            {
                this.tag = null;
            }

            return HexIotaTypes.deserialize(poppedTag.asCompound, level)
        }
        else if (iotas != null)
        {
            if (iotas!!.size == 0)
            {
                return null
            }

            // Modify deserialized version
            val poppedIota = iotas!!.removeAt(0)

            // Invalidate cache
            iotasReferencedEntities = null

            return poppedIota
        }
        else
        {
            return null
        }
    }

    fun size(): Int {
        return if (tag != null)
        {
            tag!!.size
        }
        else if (iotas!= null)
        {
            iotas!!.size
        }
        else {
            0
        }
    }

    fun getReferencedEntities(level: ServerLevel): List<Entity> {
        if (tag != null)
        {
            if (tagReferencedEntityUUIDs == null)
            {
                tagReferencedEntityUUIDs = ArrayList()
                tagReferencedEntitiesAreLoaded = ArrayList()
                for (innerTag in tag!!)
                {
                    scanTagForEntities(innerTag.asCompound, tagReferencedEntityUUIDs!!)
                }

                for (uuid in tagReferencedEntityUUIDs!!)
                {
                    tagReferencedEntitiesAreLoaded!!.add(level.getEntity(uuid) != null)
                }
            }
            var referencedEntities: MutableList<Entity> = ArrayList()
            for (uuid in tagReferencedEntityUUIDs!!)
            {
                val entity = level.getEntity(uuid)
                if (entity!= null)
                    referencedEntities.add(entity)
            }

            return referencedEntities;
        }
        else if (iotas!= null)
        {
            if (iotasReferencedEntities == null)
            {
                iotasReferencedEntities = ArrayList()
                for (iota in iotas!!)
                {
                    scanIotaForEntities(iota, iotasReferencedEntities!!)
                }
            }
            return iotasReferencedEntities as List<Entity>;
        }
        else {
            return ArrayList()
        }
    }
/*
    fun copy(serIotaList: SerialisedIotaList) {
        tag = serIotaList.tag?.copy()

        // Invalidate cache
        level = null
        iotas = null
        tagReferencedEntityUUIDs = null
        tagReferencedEntitiesAreLoaded = null
    }*/
}

// Wrapper for SerializedIotaList (only ever used for ravenmind)
class SerialisedIota(private val iotaList: SerialisedIotaList = SerialisedIotaList(null, null)) {

    constructor(iota: Iota?) : this(SerialisedIotaList(if (iota == null) null else mutableListOf(iota)))
    constructor(tag: CompoundTag?) : this(SerialisedIotaList())
    {
        if (tag != null)
        {
            iotaList.add(tag)
        }
    }

    constructor() : this(SerialisedIotaList())

    fun getTag() : CompoundTag {
        val listTag = iotaList.getTag()
        return if (listTag.size == 0)
        {
            HexIotaTypes.serialize(NullIota())
        }
        else
        {
            listTag[0].asCompound
        }
    }

    fun getIota(level: ServerLevel) : Iota {
        val iotas = iotaList.getIotas(level)
        return if (iotas.isEmpty())
        {
            NullIota()
        }
        else
        {
            iotas[0]
        }
    }

    fun set(iota: Iota) {
        iotaList.set(mutableListOf(iota))
    }

    fun set(tag: CompoundTag) {
        val listTag = ListTag()
        listTag.add(tag)
        iotaList.set(listTag)
    }

    fun getReferencedEntities(level: ServerLevel) : List<Entity> {
        return iotaList.getReferencedEntities(level)
    }
}
