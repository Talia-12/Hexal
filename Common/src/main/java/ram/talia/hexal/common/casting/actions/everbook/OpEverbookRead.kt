package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.xplat.IXplatAbstractions

object OpEverbookRead : SpellOperator {
	override val argc = 2

	override val isGreat = true
	override val alwaysProcessGreatSpell = false
	override val causesBlindDiversion = false

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val pos = BlockPos(args.getChecked<Vec3>(0, OpEverbookDelete.argc))
		val key = args.getChecked<HexPattern>(1, OpEverbookDelete.argc)

		ctx.assertVecInRange(Vec3.atCenterOf(pos))

		val tile = ctx.world.getBlockEntity(pos)
		if (tile !is BlockEntityAkashicRecord) {
			throw MishapNoAkashicRecord(pos)
		}

		val iota = IXplatAbstractions.INSTANCE.getEverbookIota(ctx.caster, key)

		val trueName = MishapOthersName.getTrueNameFromDatum(iota, ctx.caster)
		if (trueName != null)
			throw MishapOthersName(trueName)

		return Triple(
			Spell(tile, key, iota),
			0,
			listOf()
		)
	}

	private data class Spell(val record: BlockEntityAkashicRecord, val key: HexPattern, val datum: SpellDatum<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			record.addNewDatum(key, datum)

			ctx.world.playSound(null, record.blockPos, HexSounds.SCROLL_SCRIBBLE, SoundSource.BLOCKS, 1f, 0.8f)
		}
	}
}