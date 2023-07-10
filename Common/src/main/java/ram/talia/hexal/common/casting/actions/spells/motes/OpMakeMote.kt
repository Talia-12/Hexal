package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.*
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.casting.castables.VarargSpellAction
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.api.casting.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.casting.mishaps.MishapStorageFull
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getMote
import ram.talia.hexal.api.getItemEntityOrItemFrame
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import java.util.UUID


object OpMakeMote : VarargSpellAction {
    override fun argc(stack: List<Iota>): Int {
        if (stack.isEmpty())
            return 1
        val top = stack[0]
        if (top is EntityIota)
            return 1
        return 2
    }

    override fun execute(args: List<Iota>, argc: Int, env: CastingEnvironment): SpellAction.Result {
        throw IllegalStateException("call executeWithUserdata instead.")
    }

    override fun executeWithUserdata(args: List<Iota>, argc: Int, env: CastingEnvironment, userData: CompoundTag): SpellAction.Result {
        val iEtityEither = args.getItemEntityOrItemFrame(0, argc)
        val iEntity = iEtityEither.map({ it }, { it })

        env.assertEntityInRange(iEntity)

        val itemStack = iEtityEither.map( { it.item }, { it.item })
        val mote = if (argc == 2) args.getMote(1, argc) else null

        val storage = if (mote == null) {
            if (userData.contains(MoteIota.TAG_TEMP_STORAGE))
                userData.getUUID(MoteIota.TAG_TEMP_STORAGE)
            else
                env.caster?.let { MediafiedItemManager.getBoundStorage(it) }
                        ?: throw MishapNoBoundStorage()
        } else null

        return SpellAction.Result(
            Spell(itemStack, iEtityEither, mote, storage),
            HexalConfig.server.makeItemCost,
            listOf(ParticleSpray.burst(iEntity.position(), 0.4))
        )
    }

    private data class Spell(val itemStack: ItemStack, val iEntityEither: Either<ItemEntity, ItemFrame>, val mote: MoteIota?, val storage: UUID?) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            throw IllegalStateException("call cast(env, image) instead.")
        }

        override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage {
            val stack = image.stack.toMutableList()

            if (!itemStack.isEmpty) {
                if (mote != null) {
                    if (!mote.typeMatches(itemStack))
                        throw MishapInvalidIota.of(mote, 0, "cant_combine_motes")
                    val countRemaining = mote.absorb(itemStack)
                    if (countRemaining == 0)
                        iEntityEither.map( { it.discard() }, { it.item = ItemStack.EMPTY } )
                    else
                        iEntityEither.map( { it.item.count = countRemaining }, { it.item.count = countRemaining } )
                    stack.add(mote)
                } else {
                    val storage = storage ?: throw MishapNoBoundStorage()
                    if (!MediafiedItemManager.isStorageLoaded(storage))
                        throw MishapNoBoundStorage("storage_unloaded")
                    if (MediafiedItemManager.isStorageFull(storage) != false) // if this is somehow null we should still throw an error here, things have gone pretty wrong
                        throw MishapStorageFull(storage)

                    val itemIota = itemStack.asActionResult(storage)[0]

                    if (itemIota !is NullIota)
                        iEntityEither.map( { it.discard() }, { it.item = ItemStack.EMPTY } )

                    stack.add(itemIota)
                }
            }

            return image.copy(stack = stack)
        }
    }
}