package ram.talia.hexal.api.fakes;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

public class FakePlayerAdvancements extends PlayerAdvancements {
	
	public FakePlayerAdvancements (FakePlayer player) {
		super(null, null, null, null, player);
	}
	
	// this apparently didn't work, so I made a mixin to cancel the load if the player received is a fake player.
	protected void load(ServerAdvancementManager manager) {}
	
	public void setPlayer(ServerPlayer player) {}
	
	public void stopListening () {}
	
	public void reload (ServerAdvancementManager manager) {}
	
	public void save () {}
	
	public boolean award (Advancement advancement, String str) { return false; }
	
	public boolean revoke (Advancement advancement, String str) { return false; }
	
	public void flushDirty (ServerPlayer player) {}
	
	public void setSelectedTab (Advancement advancement) {}
	
	public FakeAdvancementProgress getOrStartProgress (Advancement advancement) { return new FakeAdvancementProgress(); }
}
