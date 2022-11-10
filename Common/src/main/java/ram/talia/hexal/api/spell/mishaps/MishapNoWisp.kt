package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper

class MishapNoWisp : Mishap() {
	override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.LIGHT_BLUE)

	override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component = error("no_wisp", actionName(errorCtx.action))

	private inline fun dropAll(player: Player, stacks: MutableList<ItemStack>, filter: (ItemStack) -> Boolean = { true }) {
		for (index in stacks.indices) {
			val item = stacks[index]
			if (!item.isEmpty && filter(item)) {
				player.drop(item, true, false)
				stacks[index] = ItemStack.EMPTY
			}
		}
	}

	override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
		dropAll(ctx.caster, ctx.caster.inventory.items)
		dropAll(ctx.caster, ctx.caster.inventory.offhand)
		dropAll(ctx.caster, ctx.caster.inventory.armor) {
			!EnchantmentHelper.hasBindingCurse(it)
		}
	}
}