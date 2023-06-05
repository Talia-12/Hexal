package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getPositiveIntUnder
import at.petrak.hexcasting.api.spell.getVillager
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.stats.Stats
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getMoteOrMoteList
import ram.talia.hexal.api.mediafieditems.ItemRecord
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.VarargConstMediaAction
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.iota.MoteIota
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.spell.mishaps.MishapStorageFull

object OpTradeMote : VarargConstMediaAction {
    override val mediaCost: Int
        get() = HexalConfig.server.tradeItemCost

    override fun argc(stack: List<Iota>): Int {
        if (stack.size < 2)
            return 2
        return if (stack[0] is DoubleIota) 3 else 2
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun execute(args: List<Iota>, argc: Int, ctx: CastingContext): List<Iota> {
        val villager = args.getVillager(0, argc)
        val toTradeItemIotas = args.getMoteOrMoteList(1, argc)?.map({ listOf(it) }, { it }) ?: return emptyList<Iota>().asActionResult
        val tradeIndex = if (args.size == 3) args.getPositiveIntUnder(2, villager.offers.size, argc) else null

        if (toTradeItemIotas.isEmpty())
            throw MishapInvalidIota.of(args[1], if (args.size == 3) 1 else 0, "villager_trade")

        if (toTradeItemIotas.size > 1) {
            for (i in toTradeItemIotas.indices) {
                for (j in toTradeItemIotas.indices) {
                    if (i != j && toTradeItemIotas[i].itemIndex == toTradeItemIotas[j].itemIndex)
                        throw MishapInvalidIota.of(args[1], if (args.size == 3) 1 else 0, "mote_duplicated")
                }
            }
        }

        ctx.assertEntityInRange(villager)

        val storage = (ctx as IMixinCastingContext).boundStorage ?: throw MishapNoBoundStorage(ctx.caster.position())
        if (!MediafiedItemManager.isStorageLoaded(storage))
            throw MishapNoBoundStorage(ctx.caster.position(), "storage_unloaded")

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
            val merchantoffer = if (tradeIndex != null)
                    offers.getRecipeFor(toTrade0, toTrade1, tradeIndex) ?: offers.getRecipeFor(toTrade0, toTrade1, tradeIndex) ?: break
                else getFirstMatchingInStockOffer(offers, toTrade0, toTrade1) ?: break
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

        return outRecord?.let { record -> MoteIota.makeIfStorageLoaded(record, storage)?.let{ listOf(it) } } ?: null.asActionResult
    }

    fun getFirstMatchingInStockOffer(offers: MerchantOffers, toTrade0: ItemStack, toTrade1: ItemStack): MerchantOffer? {
        for (index in 0 until offers.size) {
            val offer = offers.getRecipeFor(toTrade0, toTrade1, index) ?: offers.getRecipeFor(toTrade1, toTrade0, index) ?: continue
            if (!offer.isOutOfStock)
                return offer
        }
        return null
    }
}