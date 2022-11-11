package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.xplat.IXplatAbstractions

object OpEverbookWrite : ConstMediaAction {
	override val argc = 2

	override val isGreat = true
	override val alwaysProcessGreatSpell = false
	override val causesBlindDiversion = false

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val pos = args.getBlockPos(0, argc)
		val key = args.getPattern(1, argc)

		ctx.assertVecInRange(Vec3.atCenterOf(pos))

		val record = ctx.world.getBlockState(pos).block
		if (record !is BlockAkashicRecord) {
			throw MishapNoAkashicRecord(pos)
		}

		val iota = record.lookupPattern(pos, key, ctx.world) ?: NullIota()

		val trueName = MishapOthersName.getTrueNameFromDatum(iota, ctx.caster)
		if (trueName != null)
			throw MishapOthersName(trueName)

		IXplatAbstractions.INSTANCE.setEverbookIota(ctx.caster, key, iota)

		return listOf()
	}
}