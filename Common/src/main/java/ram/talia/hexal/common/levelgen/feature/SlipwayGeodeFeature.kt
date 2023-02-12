package ram.talia.hexal.common.levelgen.feature

import com.google.common.collect.Lists
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BuddingAmethystBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.LegacyRandomSource
import net.minecraft.world.level.levelgen.WorldgenRandom
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.synth.NormalNoise
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.div
import ram.talia.hexal.api.plus
import ram.talia.hexal.common.lib.HexalBlocks

@Suppress("MoveLambdaOutsideParentheses")
class SlipwayGeodeFeature(codec: Codec<SlipwayGeodeConfiguration>) : Feature<SlipwayGeodeConfiguration>(codec) {
	private val DIRECTIONS = Direction.values()

	override fun place(ctx: FeaturePlaceContext<SlipwayGeodeConfiguration>): Boolean {
		// TODO: SUPER HACKY. figure out a better way to do this
		if (!HexalConfig.server.generateSlipwayGeodes)
			return false

		val config = ctx.config() as SlipwayGeodeConfiguration
		val random = ctx.random()
		val origin = ctx.origin()
		val level = ctx.level()
		val minOffset = config.minGenOffset
		val maxOffset = config.maxGenOffset
		val `$$7`: MutableList<Pair<BlockPos, Int>> = Lists.newLinkedList()
		val distributionPoints = config.distributionPoints.sample(random)
		val worldgenRandom = WorldgenRandom(LegacyRandomSource(level.seed))
		val normalNoise = NormalNoise.create(worldgenRandom, -4, *doubleArrayOf(1.0))
		val `$$11`: MutableList<BlockPos> = Lists.newLinkedList()
		val `$$12` = distributionPoints.toDouble() / config.outerWallDistance.maxValue.toDouble()
		val layerSettings = config.geodeLayerSettings
		val blockSettings = config.geodeBlockSettings
		val crackSettings = config.geodeCrackSettings
		val `$$16` = 1.0 / Math.sqrt(layerSettings.filling)
		val `$$17` = 1.0 / Math.sqrt(layerSettings.innerLayer + `$$12`)
		val `$$18` = 1.0 / Math.sqrt(layerSettings.middleLayer + `$$12`)
		val `$$19` = 1.0 / Math.sqrt(layerSettings.outerLayer + `$$12`)
		val `$$20` = 1.0 / Math.sqrt(crackSettings.baseCrackSize + random.nextDouble() / 2.0 + if (distributionPoints > 3) `$$12` else 0.0)
		val `$$21` = random.nextFloat().toDouble() < crackSettings.generateCrackChance
		var `$$22` = 0
		var `$$29`: Int
		var `$$30`: Int
		var `$$44`: BlockPos
		var `$$45`: BlockState
		`$$29` = 0
		while (`$$29` < distributionPoints) {
			`$$30` = config.outerWallDistance.sample(random)
			val `$$25` = config.outerWallDistance.sample(random)
			val `$$26` = config.outerWallDistance.sample(random)
			`$$44` = origin.offset(`$$30`, `$$25`, `$$26`)
			`$$45` = level.getBlockState(`$$44`)
			if (`$$45`.isAir || `$$45`.`is`(BlockTags.GEODE_INVALID_BLOCKS)) {
				++`$$22`
				if (`$$22` > config.invalidBlocksThreshold) {
					return false
				}
			}
			`$$7`.add(Pair.of(`$$44`, config.pointOffset.sample(random)))
			++`$$29`
		}
		if (`$$21`) {
			`$$29` = random.nextInt(4)
			`$$30` = distributionPoints * 2 + 1
			if (`$$29` == 0) {
				`$$11`.add(origin.offset(`$$30`, 7, 0))
				`$$11`.add(origin.offset(`$$30`, 5, 0))
				`$$11`.add(origin.offset(`$$30`, 1, 0))
			} else if (`$$29` == 1) {
				`$$11`.add(origin.offset(0, 7, `$$30`))
				`$$11`.add(origin.offset(0, 5, `$$30`))
				`$$11`.add(origin.offset(0, 1, `$$30`))
			} else if (`$$29` == 2) {
				`$$11`.add(origin.offset(`$$30`, 7, `$$30`))
				`$$11`.add(origin.offset(`$$30`, 5, `$$30`))
				`$$11`.add(origin.offset(`$$30`, 1, `$$30`))
			} else {
				`$$11`.add(origin.offset(0, 7, 0))
				`$$11`.add(origin.offset(0, 5, 0))
				`$$11`.add(origin.offset(0, 1, 0))
			}
		}
		val `$$31`: MutableList<BlockPos> = Lists.newArrayList()
		val cannotReplace = isReplaceable(config.geodeBlockSettings.cannotReplace)

		//added to track the centre of the positions that are air so the slipway can generate there.
		var centre: Vec3 = Vec3.ZERO
		var innerCount = 0

		val var48: Iterator<*> = BlockPos.betweenClosed(origin.offset(minOffset, minOffset, minOffset), origin.offset(maxOffset, maxOffset, maxOffset)).iterator()
		while (true) {
			while (true) {
				var `$$35`: Double
				var `$$36`: Double
				var `$$33`: BlockPos
				do {
					if (!var48.hasNext()) {
						val `$$43` = blockSettings.innerPlacements
						val var51: Iterator<*> = `$$31`.iterator()
						while (true) {
							while (var51.hasNext()) {
								`$$44` = var51.next() as BlockPos
								`$$45` = Util.getRandom(`$$43`, random) as BlockState
								val var53 = DIRECTIONS
								val var37 = var53.size
								for (var54 in 0 until var37) {
									val `$$46` = var53[var54]
									if (`$$45`.hasProperty(BlockStateProperties.FACING)) {
										`$$45` = `$$45`.setValue(BlockStateProperties.FACING, `$$46`) as BlockState
									}
									val `$$47` = `$$44`.relative(`$$46`)
									val `$$48` = level.getBlockState(`$$47`)
									if (`$$45`.hasProperty(BlockStateProperties.WATERLOGGED)) {
										`$$45` = `$$45`.setValue(BlockStateProperties.WATERLOGGED, `$$48`.fluidState.isSource) as BlockState
									}
									if (BuddingAmethystBlock.canClusterGrowAtState(`$$48`)) {
										safeSetBlock(level, `$$47`, `$$45`, cannotReplace)
										break
									}
								}
							}

							// put a slipway in the middle of the geode, this is the only change I've made to the generation
							centre /= innerCount.toDouble()

							HexalAPI.LOGGER.info("making a slipway at ${BlockPos(centre)}, origin is $origin")
							safeSetBlock(level, BlockPos(centre), HexalBlocks.SLIPWAY.defaultBlockState(), { true })

							return true
						}
					}
					`$$33` = var48.next() as BlockPos
					val `$$34` = normalNoise.getValue(`$$33`.x.toDouble(), `$$33`.y.toDouble(), `$$33`.z.toDouble()) * config.noiseMultiplier
					`$$35` = 0.0
					`$$36` = 0.0
					var var40: Iterator<*>
					var `$$37`: Pair<*, *>
					var40 = `$$7`.iterator()
					while (var40.hasNext()) {
						`$$37` = var40.next()
						`$$35` += Mth.fastInvSqrt(`$$33`.distSqr(`$$37`.first as Vec3i) + `$$37`.second) + `$$34`
					}
					var `$$38`: BlockPos
					var40 = `$$11`.iterator()
					while (var40.hasNext()) {
						`$$38` = var40.next()
						`$$36` += Mth.fastInvSqrt(`$$33`.distSqr(`$$38`) + crackSettings.crackPointOffset.toDouble()) + `$$34`
					}
				} while (`$$35` < `$$19`)
				if (`$$21` && `$$36` >= `$$20` && `$$35` < `$$16`) {
					safeSetBlock(level, `$$33`, Blocks.AIR.defaultBlockState(), cannotReplace)
					val var56 = DIRECTIONS
					val var59 = var56.size
					for (var42 in 0 until var59) {
						val `$$39` = var56[var42]
						val `$$40` = `$$33`.relative(`$$39`)
						val `$$41` = level.getFluidState(`$$40`)
						if (!`$$41`.isEmpty) {
							level.scheduleTick(`$$40`, `$$41`.type, 0)
						}
					}
				} else if (`$$35` >= `$$16`) {
					safeSetBlock(level, `$$33`, blockSettings.fillingProvider.getState(random, `$$33`), cannotReplace)

					// added to track which positions are air so the slipway can generate at the centre of them. `$$33` is apparently a *mutable* block pos so the immutable is
					// necessary to make it do what you'd expect.
					innerCount++
					centre += Vec3.atCenterOf(`$$33`)
				} else if (`$$35` >= `$$17`) {
					val `$$42` = random.nextFloat().toDouble() < config.useAlternateLayer0Chance
					if (`$$42`) {
						safeSetBlock(level, `$$33`, blockSettings.alternateInnerLayerProvider.getState(random, `$$33`), cannotReplace)
					} else {
						safeSetBlock(level, `$$33`, blockSettings.innerLayerProvider.getState(random, `$$33`), cannotReplace)
					}
					if ((!config.placementsRequireLayer0Alternate || `$$42`) && random.nextFloat().toDouble() < config.usePotentialPlacementsChance) {
						`$$31`.add(`$$33`.immutable())
					}
				} else if (`$$35` >= `$$18`) {
					safeSetBlock(level, `$$33`, blockSettings.middleLayerProvider.getState(random, `$$33`), cannotReplace)
				} else if (`$$35` >= `$$19`) {
					safeSetBlock(level, `$$33`, blockSettings.outerLayerProvider.getState(random, `$$33`), cannotReplace)
				}
			}
		}

//		return true
	}
}