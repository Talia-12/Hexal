package ram.talia.hexal.common.lib;

import at.petrak.hexcasting.common.lib.HexSounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Vec3i;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexalSounds {
	public static void registerSounds(BiConsumer<SoundEvent, ResourceLocation> r) {
		for (var e : SOUNDS.entrySet()) {
			e.getValue().register(r);
		}
	}
	
	private static final Map<ResourceLocation, SoundEntry> SOUNDS = new LinkedHashMap<>();
	
	public static final SoundEntry WISP_CASTING_START = create("wisp_casting_start").subtitle("Wisp starts casting")
					.playExisting(HexSounds.ACTUALLY_CAST, 0.5f, 1)
					.category(SoundSource.PLAYERS)
					.attenuationDistance(64)
					.build();
	
	public static final SoundEntry WISP_CASTING_CONTINUE = create("wisp_casting_continue").subtitle("Wisp continues casting")
					.playExisting(SoundEvents.AMETHYST_BLOCK_STEP)
					.category(SoundSource.PLAYERS)
					.attenuationDistance(64)
					.build();
	
	
	// Everything below here is from
	// https://github.com/Creators-of-Create/Create/blob/aeee9f8793c660e0a8f619f5bd2f8c52be55e4ce/src/main/java/com/simibubi/create/AllSoundEvents.java#L393
	
	public static SoundEntryBuilder create(String id) {
		return create(modLoc(id));
	}
	
	public static SoundEntryBuilder create(ResourceLocation id) {
		return new SoundEntryBuilder(id);
	}
	
	public static JsonObject provideLangEntries() {
		JsonObject object = new JsonObject();
		for (SoundEntry entry : SOUNDS.values())
			if (entry.hasSubtitle())
				object.addProperty(entry.getSubtitleKey(), entry.getSubtitle());
		return object;
	}
	
	public static SoundEntryProvider provider(DataGenerator generator) {
		return new SoundEntryProvider(generator);
	}
	
	private record SoundEntryProvider(DataGenerator generator) implements DataProvider {
		
		@Override
			public void run (@NotNull HashCache cache) throws IOException {
				generate(generator.getOutputFolder(), cache);
			}
			
			@Override
			public @NotNull String getName () {
				return "Hexal's Custom Sounds";
			}
			
			public void generate (Path path, HashCache cache) {
				Gson GSON = (new GsonBuilder()).setPrettyPrinting()
																			 .disableHtmlEscaping()
																			 .create();
				path = path.resolve("assets/hexal");
				
				try {
					JsonObject json = new JsonObject();
					SOUNDS.entrySet()
								.stream()
								.sorted(Map.Entry.comparingByKey())
								.forEach(entry -> entry.getValue().write(json));
					DataProvider.save(GSON, cache, json, path.resolve("sounds.json"));
					
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	
	public record ConfiguredSoundEvent(Supplier<SoundEvent> event, float volume, float pitch) {}
	
	public static class SoundEntryBuilder {
		
		protected ResourceLocation id;
		protected String subtitle = "unregistered";
		protected SoundSource category = SoundSource.BLOCKS;
		protected List<ConfiguredSoundEvent> wrappedEvents;
		protected List<ResourceLocation> variants;
		protected int attenuationDistance;
		
		public SoundEntryBuilder (ResourceLocation id) {
			wrappedEvents = new ArrayList<>();
			variants = new ArrayList<>();
			this.id = id;
		}
		
		public SoundEntryBuilder subtitle (String subtitle) {
			this.subtitle = subtitle;
			return this;
		}
		
		public SoundEntryBuilder attenuationDistance (int distance) {
			this.attenuationDistance = distance;
			return this;
		}
		
		public SoundEntryBuilder noSubtitle () {
			this.subtitle = null;
			return this;
		}
		
		public SoundEntryBuilder category (SoundSource category) {
			this.category = category;
			return this;
		}
		
		public SoundEntryBuilder addVariant (String name) {
			return addVariant(modLoc(name));
		}
		
		public SoundEntryBuilder addVariant (ResourceLocation id) {
			variants.add(id);
			return this;
		}
		
		public SoundEntryBuilder playExisting (Supplier<SoundEvent> event, float volume, float pitch) {
			wrappedEvents.add(new ConfiguredSoundEvent(event, volume, pitch));
			return this;
		}
		
		public SoundEntryBuilder playExisting (SoundEvent event, float volume, float pitch) {
			return playExisting(() -> event, volume, pitch);
		}
		
		public SoundEntryBuilder playExisting (SoundEvent event) {
			return playExisting(event, 1, 1);
		}
		
		public SoundEntry build () {
			SoundEntry entry =
							wrappedEvents.isEmpty() ? new CustomSoundEntry(id, variants, subtitle, category, attenuationDistance)
											: new WrappedSoundEntry(id, subtitle, wrappedEvents, category, attenuationDistance);
			SOUNDS.put(entry.getId(), entry);
			return entry;
		}
		
	}
	
	public static abstract class SoundEntry {
		
		protected ResourceLocation id;
		protected String subtitle;
		protected SoundSource category;
		protected int attenuationDistance;
		
		public SoundEntry (ResourceLocation id, String subtitle, SoundSource category, int attenuationDistance) {
			this.id = id;
			this.subtitle = subtitle;
			this.category = category;
			this.attenuationDistance = attenuationDistance;
		}
		
		public abstract void register (BiConsumer<SoundEvent, ResourceLocation> registry);
		
		public abstract void write (JsonObject json);
		
		public abstract SoundEvent getMainEvent ();
		
		public String getSubtitleKey () {
			return id.getNamespace() + ".subtitle." + id.getPath();
		}
		
		public ResourceLocation getId () {
			return id;
		}
		
		public boolean hasSubtitle () {
			return subtitle != null;
		}
		
		public String getSubtitle () {
			return subtitle;
		}
		
		public SoundSource getCategory () {
			return category;
		}
		
		public void playOnServer (Level world, Vec3i pos) {
			playOnServer(world, pos, 1, 1);
		}
		
		public void playOnServer (Level world, Vec3i pos, float volume, float pitch) {
			play(world, null, pos, volume, pitch);
		}
		
		public void play (Level world, Player entity, Vec3i pos) {
			play(world, entity, pos, 1, 1);
		}
		
		public void playFrom (Entity entity) {
			playFrom(entity, 1, 1);
		}
		
		public void playFrom (Entity entity, float volume, float pitch) {
			if (!entity.isSilent()) {play(entity.level, null, entity.blockPosition(), volume, pitch);}
		}
		
		public void play (Level world, Player entity, Vec3i pos, float volume, float pitch) {
			play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
		}
		
		public void play (Level world, Player entity, Vec3 pos, float volume, float pitch) {
			play(world, entity, pos.x(), pos.y(), pos.z(), volume, pitch);
		}
		
		public abstract void play (Level world, Player entity, double x, double y, double z, float volume, float pitch);
		
		public void playAt (Level world, Vec3i pos, float volume, float pitch, boolean fade) {
			playAt(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch, fade);
		}
		
		public void playAt (Level world, Vec3 pos, float volume, float pitch, boolean fade) {
			playAt(world, pos.x(), pos.y(), pos.z(), volume, pitch, fade);
		}
		
		public abstract void playAt (Level world, double x, double y, double z, float volume, float pitch, boolean fade);
		
	}
	
	private static class WrappedSoundEntry extends SoundEntry {
		
		private final List<ConfiguredSoundEvent> wrappedEvents;

		public WrappedSoundEntry (ResourceLocation id, String subtitle,
															List<ConfiguredSoundEvent> wrappedEvents, SoundSource category, int attenuationDistance) {
			super(id, subtitle, category, attenuationDistance);
			this.wrappedEvents = wrappedEvents;
		}
		
		@Override
		public void register (BiConsumer<SoundEvent, ResourceLocation> registry) {
			for (int i = 0; i < wrappedEvents.size(); i++) {
				ConfiguredSoundEvent wrapped = wrappedEvents.get(i);
				ResourceLocation location = getIdOf(i);
				registry.accept(new SoundEvent(location), location);
			}
		}
		
		@Override
		public SoundEvent getMainEvent () {
			return wrappedEvents.get(0).event().get();
		}
		
		protected ResourceLocation getIdOf (int i) {
			return new ResourceLocation(id.getNamespace(), i == 0 ? id.getPath() : id.getPath() + "_compounded_" + i);
		}
		
		@Override
		public void write (JsonObject json) {
			for (int i = 0; i < wrappedEvents.size(); i++) {
				ConfiguredSoundEvent event = wrappedEvents.get(i);
				JsonObject entry = new JsonObject();
				JsonArray list = new JsonArray();
				JsonObject s = new JsonObject();
				s.addProperty("name", event.event()
																	 .get()
																	 .getLocation()
																	 .toString());
				s.addProperty("type", "event");
				if (attenuationDistance != 0) {s.addProperty("attenuation_distance", attenuationDistance);}
				list.add(s);
				entry.add("sounds", list);
				if (i == 0 && hasSubtitle()) {entry.addProperty("subtitle", getSubtitleKey());}
				json.add(getIdOf(i).getPath(), entry);
			}
		}
		
		@Override
		public void play (Level world, Player entity, double x, double y, double z, float volume, float pitch) {
			for (ConfiguredSoundEvent event : wrappedEvents) {
				world.playSound(entity, x, y, z, event.event().get(), category, event.volume() * volume, event.pitch() * pitch);
			}
		}
		
		@Override
		public void playAt (Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
			for (ConfiguredSoundEvent event : wrappedEvents) {
				world.playLocalSound(x, y, z, event.event().get(), category, event.volume() * volume,
														 event.pitch() * pitch, fade);
			}
		}
	}
	
	private static class CustomSoundEntry extends SoundEntry {
		
		protected List<ResourceLocation> variants;
		protected SoundEvent event;
		
		public CustomSoundEntry (ResourceLocation id, List<ResourceLocation> variants, String subtitle,
														 SoundSource category, int attenuationDistance) {
			super(id, subtitle, category, attenuationDistance);
			this.variants = variants;
		}
		
		@Override
		public void register (BiConsumer<SoundEvent, ResourceLocation> registry) {
			registry.accept(new SoundEvent(id), id);
		}
		
		@Override
		public SoundEvent getMainEvent () {
			return event;
		}
		
		@Override
		public void write (JsonObject json) {
			JsonObject entry = new JsonObject();
			JsonArray list = new JsonArray();
			
			JsonObject s = new JsonObject();
			s.addProperty("name", id.toString());
			s.addProperty("type", "file");
			if (attenuationDistance != 0) {s.addProperty("attenuation_distance", attenuationDistance);}
			list.add(s);
			
			for (ResourceLocation variant : variants) {
				s = new JsonObject();
				s.addProperty("name", variant.toString());
				s.addProperty("type", "file");
				if (attenuationDistance != 0) {s.addProperty("attenuation_distance", attenuationDistance);}
				list.add(s);
			}
			
			entry.add("sounds", list);
			if (hasSubtitle()) {entry.addProperty("subtitle", getSubtitleKey());}
			json.add(id.getPath(), entry);
		}
		
		@Override
		public void play (Level world, Player entity, double x, double y, double z, float volume, float pitch) {
			world.playSound(entity, x, y, z, event, category, volume, pitch);
		}
		
		@Override
		public void playAt (Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
			world.playLocalSound(x, y, z, event, category, volume, pitch, fade);
		}
	}
}
