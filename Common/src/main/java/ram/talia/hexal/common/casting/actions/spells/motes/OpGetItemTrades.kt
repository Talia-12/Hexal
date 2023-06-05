package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVillager
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import ram.talia.hexal.api.spell.iota.ItemTypeIota

object OpGetItemTrades : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val villager = args.getVillager(0, argc)

        villager.updateSpecialPrices(ctx.caster)
        villager.tradingPlayer = ctx.caster

        val result = villager.offers.map { offer ->
            // map each MerchantOffer to [[[desiredItem0, count], [desiredItem1, count]], [returnedItem, count]]
            val costList = mutableListOf(ListIota(listOf(ItemTypeIota(offer.costA.item), DoubleIota(offer.costA.count.toDouble()))))
            if (!offer.costB.isEmpty)
                costList.add(ListIota(listOf(ItemTypeIota(offer.costB.item), DoubleIota(offer.costB.count.toDouble()))))

            val offerList = listOf(
                    ListIota(costList as List<Iota>),
                    ListIota(listOf(ItemTypeIota(offer.result.item), DoubleIota(offer.result.count.toDouble())))
            )
            ListIota(offerList)
        }

        villager.stopTrading()

        return result.asActionResult
    }
}