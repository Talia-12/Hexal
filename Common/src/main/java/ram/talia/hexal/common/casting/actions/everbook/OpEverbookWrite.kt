package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.xplat.IXplatAbstractions

object OpEverbookWrite : ConstManaOperator {
	override val argc = 2

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val pos = BlockPos(args.getChecked<Vec3>(0, argc))
		val key = args.getChecked<HexPattern>(1, argc)

		ctx.assertVecInRange(Vec3.atCenterOf(pos))

		val tile = ctx.world.getBlockEntity(pos)
		if (tile !is BlockEntityAkashicRecord) {
			throw MishapNoAkashicRecord(pos)
		}

		val iota = tile.lookupPattern(key, ctx.world) ?: SpellDatum.make(Widget.NULL)

		val trueName = MishapOthersName.getTrueNameFromDatum(iota, ctx.caster)
		if (trueName != null)
			throw MishapOthersName(trueName)

		IXplatAbstractions.INSTANCE.setEverbookIota(ctx.caster, key, iota)

		return listOf()
	}
}