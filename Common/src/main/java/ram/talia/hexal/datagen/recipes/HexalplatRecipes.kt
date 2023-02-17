package ram.talia.hexal.datagen.recipes

import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper
import at.petrak.paucal.api.datagen.PaucalRecipeProvider
import net.minecraft.data.DataGenerator
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.level.block.Blocks
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.datagen.recipes.builders.FreezeRecipeBuilder
import java.util.function.Consumer

class HexalplatRecipes(generator: DataGenerator) : PaucalRecipeProvider(generator, HexalAPI.MOD_ID) {


	override fun makeRecipes(recipes: Consumer<FinishedRecipe>) {
		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.ICE), Blocks.PACKED_ICE.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/packed_ice"))
		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.PACKED_ICE), Blocks.BLUE_ICE.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/blue_ice"))
		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.WATER_CAULDRON), Blocks.POWDER_SNOW_CAULDRON.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/powder_snow_cauldron"))

//		val enlightenment = OvercastTrigger.Instance(
//			EntityPredicate.Composite.ANY,
//			MinMaxBounds.Ints.ANY,  // add a little bit of slop here
//			MinMaxBounds.Doubles.atLeast(0.8),
//			MinMaxBounds.Doubles.between(0.1, 2.05)
//		)
	}
}