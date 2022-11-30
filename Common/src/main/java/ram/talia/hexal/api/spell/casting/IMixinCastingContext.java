package ram.talia.hexal.api.spell.casting;

import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.entities.BaseCastingWisp;

public interface IMixinCastingContext {
	@Nullable BaseCastingWisp getWisp();
	@Nullable BaseCastingWisp setWisp(@Nullable BaseCastingWisp wisp);
	
	boolean hasWisp();
	
	int remainingDepth();

	int getConsumedMedia();

	void setConsumedMedia(int media);
}
