package ram.talia.hexal.api.spell.casting;

import ram.talia.hexal.common.entities.BaseWisp;

public interface MixinCastingContextInterface {
	BaseWisp getWisp();
	BaseWisp setWisp(BaseWisp wisp);
	
	boolean hasWisp();
}
