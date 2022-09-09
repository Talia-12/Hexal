package ram.talia.hexal.api.spell.casting;

import ram.talia.hexal.common.entities.BaseCastingWisp;

public interface MixinCastingContextInterface {
	BaseCastingWisp getWisp();
	BaseCastingWisp setWisp(BaseCastingWisp wisp);
	
	boolean hasWisp();
	
	int remainingDepth();
}
