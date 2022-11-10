package ram.talia.hexal.common.recipe

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.HexalAPI.MOD_ID
import ram.talia.hexal.api.HexalAPI.modLoc
import java.util.function.BiConsumer

class HexalRecipeTypes {
	companion object {
		@JvmStatic
		fun registerTypes(r: BiConsumer<RecipeType<*>, ResourceLocation>) {
			for ((key, value) in TYPES) {
				r.accept(value, key)
			}
		}

		private val TYPES: MutableMap<ResourceLocation, RecipeType<*>> = LinkedHashMap()

		var FREEZE_TYPE: RecipeType<FreezeRecipe> = registerType("freeze")

		private fun <T : Recipe<*>> registerType(name: String): RecipeType<T> {
			val type: RecipeType<T> = object : RecipeType<T> {
				override fun toString(): String {
					return "$MOD_ID:$name"
				}
			}
			// never will be a collision because it's a new object
			TYPES[modLoc(name)] = type
			return type
		}
	}
}