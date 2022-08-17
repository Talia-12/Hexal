package ram.talia.hexal.api.spell.casting;

import ram.talia.hexal.common.entities.BaseLemma;

public interface MixinCastingContextInterface {
	BaseLemma getLemma ();
	BaseLemma setLemma (BaseLemma lemma);
	
	boolean hasLemma ();
}
