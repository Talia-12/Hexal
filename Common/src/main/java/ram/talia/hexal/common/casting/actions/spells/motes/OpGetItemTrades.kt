package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import ram.talia.hexal.api.getVillager
import ram.talia.moreiotas.api.casting.iota.ItemTypeIota

object OpGetItemTrades : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val villager = args.getVillager(0, argc)

        env.caster?.let { villager.updateSpecialPrices(it) }
        villager.tradingPlayer = env.caster

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