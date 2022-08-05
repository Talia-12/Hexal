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
	
	public static final EntityType<TestHurtingProjectile> TEST_HURTING_PROJECTILE = register(
			"test_hurting_projectile",
			EntityType.Builder.<TestHurtingProjectile>of(TestHurtingProjectile::new,
														 MobCategory.MISC).sized(0.1f, 0.1f)
							  .clientTrackingRange(10)
							  .updateInterval(1)
							  .build(HexalAPI.MOD_ID + ":test_hurting_projectile"));
	
	private static <T extends Entity> EntityType<T> register (String id, EntityType<T> type) {
		var old = ENTITIES.put(modLoc(id), type);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate id " + id);
		}
		return type;
	}
}
