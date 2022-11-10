package ram.talia.hexal.forge.xplat;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.forge.eventhandlers.EverbookEventHandler;
import ram.talia.hexal.xplat.IClientXplatAbstractions;

import java.util.function.Function;

public class ForgeClientXplatImpl implements IClientXplatAbstractions {
	@Override
	public void sendPacketToServer (IMessage packet) {
		ForgePacketHandler.getNetwork().sendToServer(packet);
	}
	
	@Override
	public void setRenderLayer (Block block, RenderType type) {
		ItemBlockRenderTypes.setRenderLayer(block, type);
	}
	
	@Override
	public void initPlatformSpecific () {
		// NO-OP
	}
	
	@Override
	public <T extends Entity> void registerEntityRenderer (EntityType<? extends T> type, EntityRendererProvider<T> renderer) {
		EntityRenderers.register(type, renderer);
	}
	
	@Override
	public <T extends ParticleOptions> void registerParticleType (ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> factory) {
		Minecraft.getInstance().particleEngine.register(type, factory::apply);
	}
	
	@Override
	public void registerItemProperty (Item item, ResourceLocation id, ItemPropertyFunction func) {
		ItemProperties.register(item, id, func);
	}
	
	@Nullable
	@Override
	public CompoundTag getClientEverbookIota (HexPattern key) {
		if (EverbookEventHandler.localEverbook == null)
			return null;
		
		return EverbookEventHandler.localEverbook.getClientIota(key);
	}
	
	@Override
	public void setClientEverbookIota (HexPattern key, CompoundTag iota) {
		if (EverbookEventHandler.localEverbook == null)
			return;
		
		EverbookEventHandler.localEverbook.setIota(key, iota);
	}
	
	@Override
	public void removeClientEverbookIota (HexPattern key) {
		if (EverbookEventHandler.localEverbook == null)
			return;
			
		EverbookEventHandler.localEverbook.removeIota(key);
	}
	
	@Override
	public HexPattern getClientEverbookPattern (int index) {
		if (EverbookEventHandler.localEverbook == null)
			return null;
		
		return EverbookEventHandler.localEverbook.getKey(index);
	}
	
	@Override
	public void toggleClientEverbookMacro (HexPattern key) {
		if (EverbookEventHandler.localEverbook == null)
			return;
		
		EverbookEventHandler.localEverbook.toggleMacro(key);
	}
	
	@Override
	public boolean isClientEverbookMacro (HexPattern key) {
		return EverbookEventHandler.localEverbook.isMacro(key);
	}
	
	@Override
	public void setFilterSave (AbstractTexture texture, boolean filter, boolean mipmap) {
		texture.setBlurMipmap(filter, mipmap);
	}
	
	@Override
	public void restoreLastFilter (AbstractTexture texture) {
		texture.restoreLastBlurMipmap();
	}
}
