package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.ai.attributes.Attributes

object OpGetToughness : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        return args.getLivingEntityButNotArmorStand(0, argc).getAttributeValue(Attributes.ARMOR_TOUGHNESS).asActionResult
    }
}