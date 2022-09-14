package ram.talia.hexal.common.levelgen.feature

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.levelgen.GeodeBlockSettings
import net.minecraft.world.level.levelgen.GeodeCrackSettings
import net.minecraft.world.level.levelgen.GeodeLayerSettings
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration

class SlipwayGeodeConfiguration(
	geodeBlockSettings: GeodeBlockSettings,
	geodeLayerSettings: GeodeLayerSettings,
	geodeCrackSettings: GeodeCrackSettings,
	d0: Double,
	d1: Double,
	b: Boolean,
	provider0: IntProvider,
	provider1: IntProvider,
	provider2: IntProvider,
	int0: Int,
	int1: Int,
	d2: Double,
	int2: Int
) : GeodeConfiguration(geodeBlockSettings, geodeLayerSettings, geodeCrackSettings, d0, d1, b, provider0, provider1, provider2, int0, int1, d2, int2) {

	companion object {
		@JvmField
		val CHANCE_RANGE = Codec.doubleRange(0.0, 1.0)
		@JvmField
		val CODEC: Codec<SlipwayGeodeConfiguration> = RecordCodecBuilder.create { `$$0`: RecordCodecBuilder.Instance<SlipwayGeodeConfiguration> ->
			`$$0`.group(
				GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.geodeBlockSettings },
				GeodeLayerSettings.CODEC.fieldOf("layers").forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.geodeLayerSettings },
				GeodeCrackSettings.CODEC.fieldOf("crack").forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.geodeCrackSettings },
				CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse(0.35).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.usePotentialPlacementsChance },
				CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.useAlternateLayer0Chance },
				Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse(true).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.placementsRequireLayer0Alternate },
				IntProvider.codec(1, 20).fieldOf("outer_wall_distance").orElse(UniformInt.of(4, 5)).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.outerWallDistance },
				IntProvider.codec(1, 20).fieldOf("distribution_points").orElse(UniformInt.of(3, 4)).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.distributionPoints },
				IntProvider.codec(0, 10).fieldOf("point_offset").orElse(UniformInt.of(1, 2)).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.pointOffset },
				Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.minGenOffset },
				Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.maxGenOffset },
				CHANCE_RANGE.fieldOf("noise_multiplier").orElse(0.05).forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.noiseMultiplier },
				Codec.INT.fieldOf("invalid_blocks_threshold").forGetter { `$$0x`: SlipwayGeodeConfiguration -> `$$0x`.invalidBlocksThreshold }
			).apply(
				`$$0`
			) { `$$0`: GeodeBlockSettings, `$$1`: GeodeLayerSettings, `$$2`: GeodeCrackSettings, `$$3`: Double, `$$4`: Double, `$$5`: Boolean, `$$6`: IntProvider, `$$7`: IntProvider, `$$8`: IntProvider, `$$9`: Int, `$$10`: Int, `$$11`: Double, `$$12`: Int ->
				SlipwayGeodeConfiguration(
					`$$0`, `$$1`, `$$2`,
					`$$3`,
					`$$4`,
					`$$5`, `$$6`, `$$7`, `$$8`,
					`$$9`,
					`$$10`,
					`$$11`,
					`$$12`
				)
			}
		}
	}
}