package ram.talia.hexal.common.entities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import ram.talia.hexal.api.HexalAPI;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexalEntities {
	public static void registerEntities (BiConsumer<EntityType<?>, ResourceLocation> r) {
		for (var e : ENTITIES.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}
	
	private static final Map<ResourceLocation, EntityType<?>> ENTITIES = new LinkedHashMap<>();
	
	public static final EntityType<BaseWisp> BASE_WISP = register(
					"base_wisp",
					EntityType.Builder.of((EntityType.EntityFactory<BaseWisp>) BaseWisp::new, MobCategory.MISC)
														.sized(0.1f, 0.1f)
														.clientTrackingRange(10)
														.updateInterval(1)
														.build(HexalAPI.MOD_ID + ":base_wisp"));
	
	private static <T extends Entity> EntityType<T> register (String id, EntityType<T> type) {
		var old = ENTITIES.put(modLoc(id), type);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate id " + id);
		}
		return type;
	}
}
