package ram.talia.hexal.common.casting.arithmetics.operator.mote

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate.all
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate.ofType
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.api.toIntCapped
import ram.talia.hexal.common.lib.hex.HexalIotaTypes.MOTE
import ram.talia.moreiotas.api.casting.iota.ItemStackIota
import java.util.function.UnaryOperator

object OperatorMoteExtractItem : OperatorUnary(all(ofType(MOTE)), OperatorMoteExtractItem), UnaryOperator<Iota> {
    override fun apply(iota: Iota): Iota {
        val mote = Operator.downcast(iota, MOTE)
        val stack = ItemStack(mote.item, mote.count.toIntCapped())
        stack.tag = mote.tag

        return ItemStackIota.createFiltered(stack)
    }
}