package ram.talia.hexal.fabric.datagen;

import at.petrak.hexcasting.api.HexAPI;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import ram.talia.hexal.datagen.HexalBlockTagProvider;
import ram.talia.hexal.datagen.HexalLootTables;
import ram.talia.hexal.datagen.recipes.HexalplatRecipes;
import ram.talia.hexal.datagen.tag.HexalActionTagProvider;

import java.util.List;
import java.util.Set;

public class HexalFabricDataGenerators implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator (FabricDataGenerator gen) {
		HexAPI.LOGGER.info("Starting Fabric-specific datagen");

		var pack = gen.createPack();

		pack.addProvider((FabricDataGenerator.Pack.Factory<HexalplatRecipes>) HexalplatRecipes::new);

		var btagProviderWrapper = new BlockTagProviderWrapper(); // CURSED
		pack.addProvider((output, lookup) -> {
			btagProviderWrapper.provider = new HexalBlockTagProvider(output, lookup);
			return btagProviderWrapper.provider;
		});
//		pack.addProvider((output, lookup) -> new HexItemTagProvider(output, lookup, btagProviderWrapper.provider, xtags));

		pack.addProvider(HexalActionTagProvider::new);

		pack.addProvider((FabricDataGenerator.Pack.Factory<LootTableProvider>) (output) -> new LootTableProvider(
			output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(HexalLootTables::new, LootContextParamSets.ALL_PARAMS))
		));
	}

	private static class BlockTagProviderWrapper {
		HexalBlockTagProvider provider;
	}
	
	private static TagKey<Item> tag(String s) {
		return tag("c", s);
	}
	private static TagKey<Item> tag(String namespace, String s) {
		return TagKey.create(Registries.ITEM, new ResourceLocation(namespace, s));
	}
}
