package ram.talia.hexal.common.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BaseWisp extends Projectile {
	
	public BaseWisp (EntityType<? extends BaseWisp> entityType, Level world) {
		// error here isn't actually a problem
		super(entityType, world);
	}
	
	public BaseWisp (Level world, Vec3 pos) {
		super(HexalEntities.BASE_WISP, world);
		setPos(pos);
	}
	
	public BaseWisp (Level world, Vec3 pos, LivingEntity shooter) {
		super(HexalEntities.BASE_WISP, world);
		setPos(pos);
		setOwner(shooter);
	}
	
	@Override
	protected void defineSynchedData () {
	
	}
}
