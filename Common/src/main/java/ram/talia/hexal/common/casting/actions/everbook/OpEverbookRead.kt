package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.spell.mishaps.MishapIllegalInterworldIota
import ram.talia.hexal.xplat.IXplatAbstractions

object OpEverbookRead : SpellAction {
	override val argc = 2

	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val pos = args.getBlockPos(0, argc)
		val key = args.getPattern(1, argc)

		env.assertVecInRange(Vec3.atCenterOf(pos))

		val record = env.world.getBlockState(pos).block
		if (record !is BlockAkashicRecord) {
			throw MishapNoAkashicRecord(pos)
		}

		val iota = IXplatAbstractions.INSTANCE.getEverbookIota(env.caster, key)

		val trueName = MishapOthersName.getTrueNameFromDatum(iota, env.caster)
		if (trueName != null)
			throw MishapOthersName(trueName)
		val illegalInterworldIota = MishapIllegalInterworldIota.getFromNestedIota(iota)
		if (illegalInterworldIota != null)
			throw MishapIllegalInterworldIota(illegalInterworldIota)

		return SpellAction.Result(
			Spell(record, pos, key, iota),
			0,
			listOf()
		)
	}

	private data class Spell(val record: BlockAkashicRecord, val recordPos: BlockPos, val key: HexPattern, val datum: Iota) : RenderedSpell {
		override fun cast(env: CastingEnvironment) {
			record.addNewDatum(recordPos, env.world, key, datum)

			env.world.playSound(null, recordPos, HexSounds.SCROLL_SCRIBBLE, SoundSource.BLOCKS, 1f, 0.8f)
		}
	}
}