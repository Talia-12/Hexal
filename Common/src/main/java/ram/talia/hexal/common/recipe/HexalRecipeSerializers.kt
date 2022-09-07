package ram.talia.hexal.common.recipe

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import ram.talia.hexal.api.HexalAPI
import java.util.function.BiConsumer

class HexalRecipeSerializers {
	companion object {
		@JvmStatic
		fun registerSerializers(r: BiConsumer<RecipeSerializer<*>, ResourceLocation>) {
			for ((key, value) in SERIALIZERS) {
				r.accept(value, key)
			}
		}

		private val SERIALIZERS: MutableMap<ResourceLocation, RecipeSerializer<*>> = LinkedHashMap()

		val FREEZE: RecipeSerializer<*> = register("freeze", FreezeRecipe.Serializer())

		var FREEZE_TYPE: RecipeType<FreezeRecipe>? = null

		private fun <T : Recipe<*>?> register(name: String, rs: RecipeSerializer<T>): RecipeSerializer<T> {
			val old = SERIALIZERS.put(HexalAPI.modLoc(name), rs)
			require(old == null) { "Typo? Duplicate id $name" }
			return rs
		}

		// Like in the statistics, gotta register it at some point
		@JvmStatic
		fun registerTypes() {
			HexalRecipeSerializers.FREEZE_TYPE = RecipeType.register(HexalAPI.MOD_ID + ":freeze")
		}
	}
}