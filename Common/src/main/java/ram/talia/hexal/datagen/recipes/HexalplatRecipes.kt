package ram.talia.hexal.datagen.recipes

import at.petrak.hexcasting.api.advancements.OvercastTrigger
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder
import at.petrak.paucal.api.datagen.PaucalRecipeProvider
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.data.DataGenerator
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.lib.HexalBlocks
import ram.talia.hexal.common.lib.HexalItems
import ram.talia.hexal.datagen.recipes.builders.FreezeRecipeBuilder
import java.util.function.Consumer

class HexalplatRecipes(generator: DataGenerator) : PaucalRecipeProvider(generator, HexalAPI.MOD_ID) {


	override fun makeRecipes(recipes: Consumer<FinishedRecipe>) {
		ShapedRecipeBuilder.shaped(HexalItems.RELAY)
			.define('C', HexItems.CHARGED_AMETHYST)
			.define('S', HexBlocks.SLATE_BLOCK)
			.define('A', Items.AMETHYST_BLOCK)
			.pattern(" C ")
			.pattern("SSS")
			.pattern("SAS")
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes)

		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.ICE), Blocks.PACKED_ICE.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/packed_ice"))
		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.PACKED_ICE), Blocks.BLUE_ICE.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/blue_ice"))
		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.WATER_CAULDRON), Blocks.POWDER_SNOW_CAULDRON.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/powder_snow_cauldron"))

		val enlightenment = OvercastTrigger.Instance(
			EntityPredicate.Composite.ANY,
			MinMaxBounds.Ints.ANY,  // add a little bit of slop here
			MinMaxBounds.Doubles.atLeast(0.8),
			MinMaxBounds.Doubles.between(0.1, 2.05)
		)

		BrainsweepRecipeBuilder(StateIngredientHelper.of(Blocks.SHULKER_BOX),
				VillagerIngredient(ResourceLocation("cartographer"), null, 2),
				HexalBlocks.MEDIAFIED_STORAGE.defaultBlockState())
				.unlockedBy("enlightenment", enlightenment)
				.save(recipes, modLoc("brainsweep/mediafied_storage"))
	}
}