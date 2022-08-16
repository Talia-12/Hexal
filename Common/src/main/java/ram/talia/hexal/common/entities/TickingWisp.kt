package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.spell.casting.WispCastingManager

class TickingWisp : BaseWisp {
	var lasting = false

	private var stack: MutableList<SpellDatum<*>> = mutableListOf(SpellDatum.make(this))
	private var ravenmind: SpellDatum<*> = SpellDatum.make(Widget.NULL)

	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)
	constructor(
		entityType: EntityType<out TickingWisp>,
		world: Level,
		pos: Vec3,
		caster: Player,
		media: Int,
		lasting: Boolean
	) : super(entityType, world, pos, caster, media) {
		this.lasting = lasting
	}

	constructor(world: Level, pos: Vec3, caster: Player, media: Int, lasting: Boolean) : super(HexalEntities.PROJECTILE_WISP, world, pos, caster, media) {
		this.lasting = lasting
	}

	override fun childTick() {
		scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, stack, ravenmind)
	}

	override fun move() {}

	override fun castCallback(result: WispCastingManager.WispCastResult) {}

	override fun load(compound: CompoundTag) {
		super.load(compound)

		lasting = compound.getBoolean(LASTING_TAG)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)
		compound.putBoolean(LASTING_TAG, lasting)
	}

	companion object {
		const val LASTING_TAG = "lasting"

		const val CASTING_SCHEDULE_PRIORITY = 1
	}
}