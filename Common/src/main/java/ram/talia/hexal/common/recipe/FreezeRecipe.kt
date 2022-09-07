package ram.talia.hexal.common.recipe

import at.petrak.hexcasting.common.recipe.RecipeSerializerBase
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredient
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper
import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

data class FreezeRecipe(val resId: ResourceLocation, val blockIn: StateIngredient, val result: BlockState) : Recipe<Container> {
	override fun matches(pContainer: Container, pLevel: Level) = false

	fun matches(blockIn: BlockState) = this.blockIn.test(blockIn)

	override fun assemble(pContainer: Container) = ItemStack.EMPTY

	override fun canCraftInDimensions(pWidth: Int, pHeight: Int) = false

	override fun getResultItem() = ItemStack.EMPTY.copy()

	override fun getId() = resId

	override fun getSerializer() = HexalRecipeSerializers.FREEZE

	override fun getType() = HexalRecipeSerializers.FREEZE_TYPE!!

	class Serializer : RecipeSerializerBase<FreezeRecipe>() {
		override fun fromJson(recipeID: ResourceLocation, json: JsonObject): FreezeRecipe {
			val blockIn = StateIngredientHelper.deserialize(GsonHelper.getAsJsonObject(json, "blockIn"))
			val result = StateIngredientHelper.readBlockState(GsonHelper.getAsJsonObject(json, "result"))
			return FreezeRecipe(recipeID, blockIn, result)
		}

		override fun toNetwork(buf: FriendlyByteBuf, recipe: FreezeRecipe) {
			recipe.blockIn.write(buf)
			buf.writeVarInt(Block.getId(recipe.result))
		}

		override fun fromNetwork(recipeID: ResourceLocation, buf: FriendlyByteBuf): FreezeRecipe {
			val blockIn = StateIngredientHelper.read(buf)
			val result = Block.stateById(buf.readVarInt())
			return FreezeRecipe(recipeID, blockIn, result)
		}
	}
}
