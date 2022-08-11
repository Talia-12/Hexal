package ram.talia.hexal.api.casting;

import ram.talia.hexal.common.entities.BaseWisp;

public interface MixinCastingContextInterface {
	BaseWisp getWisp();
	BaseWisp setWisp(BaseWisp wisp);
}
