package ram.talia.hexal.datagen.recipes.builders

import at.petrak.hexcasting.common.recipe.ingredient.StateIngredient
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper
import com.google.gson.JsonObject
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.CriterionTriggerInstance
import net.minecraft.advancements.RequirementsStrategy
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.state.BlockState
import ram.talia.hexal.common.recipe.HexalRecipeSerializers
import java.util.function.Consumer

class FreezeRecipeBuilder(private val blockIn: StateIngredient, private val result: BlockState) : RecipeBuilder {

	private var advancement: Advancement.Builder = Advancement.Builder.advancement()

	override fun unlockedBy(pCriterionName: String, pCriterionTrigger: CriterionTriggerInstance): RecipeBuilder {
		advancement.addCriterion(pCriterionName, pCriterionTrigger)
		return this
	}

	override fun group(pGroupName: String?) = this

	override fun getResult(): Item = result.block.asItem()

	override fun save(pFinishedRecipeConsumer: Consumer<FinishedRecipe>, pRecipeId: ResourceLocation) {
		check(advancement.criteria.isNotEmpty()) { "No way of obtaining recipe $pRecipeId" }
		advancement.parent(ResourceLocation("recipes/root"))
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId))
			.rewards(AdvancementRewards.Builder.recipe(pRecipeId))
			.requirements(RequirementsStrategy.OR)
		pFinishedRecipeConsumer.accept(
			Result(
				pRecipeId,
				blockIn, result,
				advancement,
				ResourceLocation(pRecipeId.namespace, "recipes/freeze/" + pRecipeId.path)
			)
		)
	}

	data class Result(
		val rId: ResourceLocation,
		val blockIn: StateIngredient,
		val result: BlockState,
		val advancement: Advancement.Builder,
		val advancementRId: ResourceLocation
	) : FinishedRecipe {
		override fun serializeRecipeData(json: JsonObject) {
			json.add("blockIn", this.blockIn.serialize())
			json.add("result", StateIngredientHelper.serializeBlockState(this.result))
		}

		override fun getId(): ResourceLocation {
			return rId
		}

		override fun getType(): RecipeSerializer<*> {
			return HexalRecipeSerializers.FREEZE
		}

		override fun serializeAdvancement(): JsonObject {
			return this.advancement.serializeToJson()
		}

		override fun getAdvancementId(): ResourceLocation {
			return advancementRId
		}
	}
}