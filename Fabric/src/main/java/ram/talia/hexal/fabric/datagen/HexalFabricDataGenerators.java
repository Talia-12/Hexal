package ram.talia.hexal.fabric.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.datagen.IXplatIngredients;
import at.petrak.hexcasting.datagen.recipe.HexplatRecipes;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import ram.talia.hexal.datagen.recipes.HexalplatRecipes;

public class HexalFabricDataGenerators implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator (FabricDataGenerator fabricDataGenerator) {
		HexAPI.LOGGER.info("Starting Fabric-specific datagen");
		
		fabricDataGenerator.addProvider(new HexalplatRecipes(fabricDataGenerator));
	}
	
	private static TagKey<Item> tag(String s) {
		return tag("c", s);
	}
	private static TagKey<Item> tag(String namespace, String s) {
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(namespace, s));
	}
}
