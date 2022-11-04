package ram.talia.hexal.xplat;

import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.network.IMessage;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.everbook.Everbook;

import javax.annotation.Nullable;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface IClientXplatAbstractions {
	void sendPacketToServer(IMessage packet);
	
	void setRenderLayer(Block block, RenderType type);
	
	void initPlatformSpecific();
	
	<T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> renderer);
	
	<T extends ParticleOptions> void registerParticleType(ParticleType<T> type,
																												Function<SpriteSet, ParticleProvider<T>> factory);
	
	<T extends ClientTooltipComponent & TooltipComponent> void registerIdentityTooltipMapping(Class<T> clazz);
	
	void registerItemProperty(Item item, ResourceLocation id, ItemPropertyFunction func);
	
	/**
	 * Gets the iota at the passed key in the client's Everbook.
	 */
	@Nullable
	CompoundTag getClientEverbookIota(HexPattern key);
	
	/**
	 * Sets the client's everbook to contain the passed iota at the passed key.
	 */
	void setClientEverbookIota(HexPattern key, CompoundTag iota);
	
	/**
	 * Removes the passed key from the client's everbook.
	 */
	void removeClientEverbookIota(HexPattern key);
	
	/**
	 * Returns the pattern at index in the client's Everbook (sorting the pattern keys alphabetically by their string representation).
	 */
	HexPattern getClientEverbookPattern(int index);
	
	/**
	 * Toggles whether the Everbook entry with the given key is a macro.
	 */
	void toggleClientEverbookMacro(HexPattern key);
	
	/**
	 * Returns true if the Everbook entry at the given key is a macro, and false otherwise.
	 */
	boolean isClientEverbookMacro(HexPattern key);
	
	// On Forge, these are already exposed; on Fabric we do a mixin
	void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap);
	
	void restoreLastFilter(AbstractTexture texture);
	
	IClientXplatAbstractions INSTANCE = find();
	
	private static IClientXplatAbstractions find() {
		var providers = ServiceLoader.load(IClientXplatAbstractions.class).stream().toList();
		if (providers.size() != 1) {
			var names = providers.stream().map(p -> p.type().getName()).collect(
							Collectors.joining(",", "[", "]"));
			throw new IllegalStateException(
							"There should be exactly one IClientXplatAbstractions implementation on the classpath. Found: " + names);
		} else {
			var provider = providers.get(0);
			HexalAPI.LOGGER.debug("Instantiating client xplat impl: " + provider.type().getName());
			return provider.get();
		}
	}
}
