package ram.talia.hexal.common.casting.actions.math.matrix

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapEvalTooDeep
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.world.phys.Vec3

object OpAddMatrices : ConstManaOperator {
    override val argc = 2

    private fun addAddableLists(list0: SpellList, list1: SpellList, recCount: Int = 0): List<SpellDatum<*>> {
        if (recCount > 1) {
            throw MishapEvalTooDeep()
        }

        val _list0 = list0.toMutableList()
        val _list1 = list1.toMutableList()

        if (_list0.size != _list1.size) {
            throw MishapInvalidIota(
                _list1.asSpellResult[0],
                0,
                "hexal.mishap.invalid_value.matrix.sizes_not_equal".asTranslatedComponent(_list0.size, _list1.size)
            )
        }

        for (i in 0 until _list0.size) {
            if (_list0[i] != _list1[i]) {
                throw MishapInvalidIota(
                    _list1.asSpellResult[0],
                    0,
                    "hexal.mishap.invalid_value.matrix.mismatched_types".asTranslatedComponent(i, _list0[i].getType().serializedName, _list1[i].getType().serializedName)
                )
            }

            when (_list0[i].getType()) {
                DatumType.DOUBLE -> _list0[i] = SpellDatum.make(_list0[i].payload as Double + _list1[i].payload as Double)
                DatumType.VEC -> _list0[i] = SpellDatum.make((_list0[i].payload as Vec3).add(_list1[i].payload as Vec3))
                DatumType.LIST -> _list0[i] = addAddableLists(_list0[i] as SpellList, _list1[i] as SpellList, recCount + 1)[0]
                else -> throw MishapInvalidIota(
                    _list0[i],
                    0,
                    "hexal.mishap.invalid_value.matrix.invalid_type".asTranslatedComponent(i, _list0[i].getType().serializedName)
                )
            }
        }

        return _list0.asSpellResult
    }

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val mat0 = args.getChecked<SpellList>(0, argc)
        val mat1 = args.getChecked<SpellList>(1, argc)

        return addAddableLists(mat0, mat1)
    }
}