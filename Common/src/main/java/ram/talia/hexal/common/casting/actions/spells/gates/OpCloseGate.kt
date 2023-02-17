package ram.talia.hexal.common.casting.actions.spells.gates

import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import at.petrak.hexcasting.common.network.MsgBlinkAck
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getGate
import ram.talia.hexal.api.minus

object OpCloseGate : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val gate = args.getGate(0, argc)
        val targetPos = args.getVec3(1, argc)

        ctx.assertVecInRange(targetPos)
        if (!ctx.isVecInWorld(targetPos.subtract(0.0, 1.0, 0.0)))
            throw MishapLocationTooFarAway(targetPos, "too_close_to_out")

        val gatees = gate.getMarked(ctx.world)
        gate.clearMarked()

        // make particle effects at every teleported entity.
        var meanEyeHeight = 0.0
        val burst = gatees.map { meanEyeHeight += it.eyeHeight; ParticleSpray.cloud(it.position().add(0.0, it.eyeHeight / 2.0, 0.0), 2.0) } as MutableList
        meanEyeHeight /= burst.size
        burst.add(ParticleSpray.burst(targetPos.add(0.0, meanEyeHeight / 2.0, 0.0), 2.0))

        return Triple(
                Spell(gatees, targetPos),
                HexalConfig.server.closeGateCost,
                burst
        )
    }

    private data class Spell(val gatees: Set<Entity>, val targetPos: Vec3) : RenderedSpell {
        // stole all this from the default teleport; sadge that it isn't accessible.

        override fun cast(ctx: CastingContext) {
            for (gatee in gatees) {
                teleport(gatee, targetPos - gatee.position(), ctx)
            }
        }

        fun teleport(teleportee: Entity, delta: Vec3, ctx: CastingContext) {
            val distance = delta.length()

            // TODO make this not a magic number (config?)
            if (distance < 32768.0) {
                teleportRespectSticky(teleportee, delta)
            }

            if (teleportee is ServerPlayer && teleportee == ctx.caster) {
                // Drop items conditionally, based on distance teleported.
                // MOST IMPORTANT: Never drop main hand item, since if it's a trinket, it will get duplicated later.

                val baseDropChance = distance / 10000.0

                // Armor and hotbar items have a further reduced chance to be dropped since it's particularly annoying
                // having to rearrange those. Also it makes sense for LORE REASONS probably, since the caster is more
                // aware of items they use often.
                for (armorItem in teleportee.inventory.armor) {
                    if (EnchantmentHelper.hasBindingCurse(armorItem))
                        continue

                    if (Math.random() < baseDropChance * 0.25) {
                        teleportee.drop(armorItem.copy(), true, false)
                        armorItem.shrink(armorItem.count)
                    }
                }

                for ((pos, invItem) in teleportee.inventory.items.withIndex()) {
                    if (invItem == teleportee.mainHandItem) continue
                    val dropChance = if (pos < 9) baseDropChance * 0.5 else baseDropChance // hotbar
                    if (Math.random() < dropChance) {
                        teleportee.drop(invItem.copy(), true, false)
                        invItem.shrink(invItem.count)
                    }
                }

                // we also don't drop the offhand just to be nice
            }
        }
    }

    fun teleportRespectSticky(teleportee: Entity, delta: Vec3) {
        val base = teleportee.rootVehicle

        val playersToUpdate = mutableListOf<ServerPlayer>()
        val indirect = base.indirectPassengers

        val sticky = indirect.any { it.type.`is`(HexTags.Entities.STICKY_TELEPORTERS) }
        val cannotSticky = indirect.none { it.type.`is`(HexTags.Entities.CANNOT_TELEPORT) }
        if (sticky && cannotSticky)
            return

        if (sticky) {
            // this handles teleporting the passengers
            val target = base.position().add(delta)
            base.teleportTo(target.x, target.y, target.z)
            indirect
                    .filterIsInstance<ServerPlayer>()
                    .forEach(playersToUpdate::add)
        } else {
            // Break it into two stacks
            teleportee.stopRiding()
            teleportee.passengers.forEach(Entity::stopRiding)
            teleportee.setPos(teleportee.position().add(delta))
            if (teleportee is ServerPlayer) {
                playersToUpdate.add(teleportee)
            }
        }

        for (player in playersToUpdate) {
            player.connection.resetPosition()
            IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, MsgBlinkAck(delta))
        }
    }
}