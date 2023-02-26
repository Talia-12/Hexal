package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVillager
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.stats.Stats
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getItemOrItemList
import ram.talia.hexal.api.mediafieditems.ItemRecord
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.iota.ItemIota
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.spell.mishaps.MishapStorageFull

object OpTradeItem : ConstMediaAction {
    override val argc = 2
    override val mediaCost: Int
        get() = HexalConfig.server.tradeItemCost

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val villager = args.getVillager(0, argc)
        val toTradeItemIotas = args.getItemOrItemList(1, argc)?.map({ listOf(it) }, { it }) ?: return emptyList<Iota>().asActionResult
        if (toTradeItemIotas.isEmpty())
            throw MishapInvalidIota.of(args[1], 0, "villager_trade")

        ctx.assertEntityInRange(villager)

        val storage = (ctx as IMixinCastingContext).boundStorage ?: throw MishapNoBoundStorage(ctx.caster.position())
        val isFull = MediafiedItemManager.isStorageFull(storage) ?: return null.asActionResult
        if (isFull)
            throw MishapStorageFull(ctx.caster.position())


        if (villager.offers.isEmpty())
            return emptyList<Iota>().asActionResult

        villager.updateSpecialPrices(ctx.caster)
        villager.tradingPlayer = ctx.caster

        var outRecord: ItemRecord? = null

        do {
            // have to recompute this each time since the merchantoffer will eat some items from each stack.
            val toTradeItemStacks = toTradeItemIotas.map { it.record?.toStack(it.item.maxStackSize) }
            val toTrade0 = toTradeItemStacks.getOrElse(0) { ItemStack.EMPTY } ?: ItemStack.EMPTY
            val toTrade1 = toTradeItemStacks.getOrElse(1) { ItemStack.EMPTY } ?: ItemStack.EMPTY
            val offers = villager.offers
            // have to try this both ways around apparently.
            val merchantoffer = offers.getRecipeFor(toTrade0, toTrade1, 0) ?: offers.getRecipeFor(toTrade0, toTrade1, 0) ?: break
            if (merchantoffer.isOutOfStock)
                break

            if (merchantoffer.take(toTrade0, toTrade1) || merchantoffer.take(toTrade0, toTrade1)) {
                villager.notifyTrade(merchantoffer)
                ctx.caster.awardStat(Stats.TRADED_WITH_VILLAGER)

                if (outRecord == null)
                    outRecord = ItemRecord(merchantoffer.result)
                else if (outRecord.item == merchantoffer.result.item && outRecord.tag == merchantoffer.result.tag)
                    outRecord.count += merchantoffer.result.count
                else {
                    HexalAPI.LOGGER.warn("trade resulted in different Item result in different iterations of the loop, aghhhhh")
                    break
                }

                toTradeItemIotas.getOrNull(0)?.removeItems(toTradeItemIotas[0].item.maxStackSize - toTrade0.count)
                toTradeItemIotas.getOrNull(1)?.removeItems(toTradeItemIotas[1].item.maxStackSize - toTrade0.count)
            }

        } while (!merchantoffer.isOutOfStock)

        villager.stopTrading()

        return outRecord?.let { record -> ItemIota.makeIfStorageLoaded(record, storage)?.let{ listOf(it) } } ?: null.asActionResult
    }
}