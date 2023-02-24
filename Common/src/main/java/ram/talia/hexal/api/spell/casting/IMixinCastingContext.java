package ram.talia.hexal.api.spell.casting;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.entities.BaseCastingWisp;

import java.util.UUID;

public interface IMixinCastingContext {
	@Nullable BaseCastingWisp getWisp();
	@Nullable BaseCastingWisp setWisp(@Nullable BaseCastingWisp wisp);
	
	boolean hasWisp();
	
	int remainingDepth();

	int getConsumedMedia();

	void setConsumedMedia(int media);

	int getTimesTicked(BlockPos pos);

	void incTimesTicked(BlockPos pos);

	@Nullable UUID getBoundStorage();

	void setTemporaryBoundStorage(@Nullable UUID temporaryStorage);
}
