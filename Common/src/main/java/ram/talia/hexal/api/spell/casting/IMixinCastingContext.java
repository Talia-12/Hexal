package ram.talia.hexal.api.spell.casting;

import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.common.entities.BaseCastingWisp;

public interface IMixinCastingContext {
	BaseCastingWisp getWisp();
	BaseCastingWisp setWisp(BaseCastingWisp wisp);
	
	boolean hasWisp();
	
	int remainingDepth();
	
	//region Transmission
	ILinkable<?> getForwardingTo ();
	void setForwardingTo (int to);
	void resetForwardingTo ();
	//endregion
}
