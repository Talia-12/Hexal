package ram.talia.hexal.common.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class TestHurtingProjectile extends Projectile {
	
	public TestHurtingProjectile (EntityType<? extends TestHurtingProjectile> entityType, Level world) {
		super(entityType, world);
	}
	
	@Override
	protected void defineSynchedData () {
	
	}
}
