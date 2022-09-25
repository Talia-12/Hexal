# Guide to Starting a Hex Casting Addon

## Environment

Fork the [XPlat environment](https://github.com/gattsuru/Forge-Fabric-XPlat-Env) created by gattsuru based on Hex Casting's structure. Using this means that your addon will work with both Fabric and Forge with minimal extra effort. Follow the steps detailed in the README of the above project to setup the environment to work on your own mod. 

## Setup Gradle:

add the below to the `gradle.properties` in the project directory (not the `gradle.properties` in the Fabric or Forge directories), replacing the Xs with whatever numbers are needed to get the latest versions of each mod (or whichever version you want to support)
```
paucalVersion=0.X.X
hexcastingVersion=0.X.X
patchouliVersion=XX
```

Then, the `build.gradle` files in Common, Fabric, and Forge need to be changed. In `Common/build.gradle` add
```
compileOnly "at.petra-k.paucal:paucal-common-$minecraftVersion:$paucalVersion"
compileOnly "at.petra-k.hexcasting:hexcasting-common-$minecraftVersion:$hexcastingVersion"
compileOnly "vazkii.patchouli:Patchouli-xplat:$minecraftVersion-$patchouliVersion"
```
to the dependencies section.

In `Fabric/build.gradle` add
```
modImplementation "at.petra-k.paucal:paucal-fabric-$minecraftVersion:$paucalVersion"
modImplementation "at.petra-k.hexcasting:hexcasting-fabric-$minecraftVersion:$hexcastingVersion"
modImplementation "vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion-FABRIC"
```
to the dependencies section.

Making the Forge dependencies work is slightly more involved. Create a folder `Forge\libs` and put the JAR for the forge version of Hex Casting inside it, `Forge\libs\hexcasting-forge-1.18.2-0.X.X.jar`. Then, in `Forge/build.gradle` add
```
 flatDir {
     dir 'libs'
 }
```
to the repositories section and
```
compileOnly fg.deobf("at.petra-k.paucal:paucal-forge-$minecraftVersion:$paucalVersion")
runtimeOnly fg.deobf("at.petra-k.paucal:paucal-forge-$minecraftVersion:$paucalVersion")
compileOnly fg.deobf("${modID}:hexcasting-forge-$minecraftVersion:$hexcastingVersion")
runtimeOnly fg.deobf("${modID}:hexcasting-forge-$minecraftVersion:$hexcastingVersion")

compileOnly fg.deobf("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion")
runtimeOnly fg.deobf("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion")

testCompileOnly fg.deobf("at.petra-k.paucal:paucal-forge-$minecraftVersion:$paucalVersion")
testCompileOnly fg.deobf("${modID}:hexcasting-forge-$minecraftVersion:$hexcastingVersion")
testCompileOnly fg.deobf("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion")
```
to the dependencies section.

## Adding RegisterPatterns

Mimicking Hex Casting's project structure, in `Common\src\main\java\your\mod\here` (replacing `your\mod\here` with your mod), create a package `your.mod.here.common.casting`. In this package create a class `RegisterPatterns`. This class will have a single method, `public static void registerPatterns ()`.
```java
public class RegisterPatterns {
	public static void registerPatterns () {
		try {
			// TODO: Add some patterns
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
```

Add `RegisterPatterns.registerPatterns()` after `initRegistries()` in `FabricInitializer`, and add
```
modBus.addListener((FMLCommonSetupEvent evt) -> evt.enqueueWork(RegisterPatterns::registerPatterns));
```
to `ForgeInitializer.initListeners()`.

This will result in any patterns that you add inside the try statement in `registerPatterns()` to be usable in game.

## Creating an Operator

again in Common, create a new package `your.mod.here.common.casting.operators`. Create a Kotlin Object called `OpExample`.
```kotlin
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.entity.Entity

object OpExample : ConstManaOperator {
	override val argc = 2

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		return (args[0].getType() == args[1].getType()).asSpellResult
	}
}
```
This operator takes two arguments and returns 1 if they're the same type, 0 otherwise (OpCompareTypes) from my mod. Feel free to rename the operator, change what it does, whatever.

To register this operator it needs to be added in RegisterPatterns:
```java
import static your.mod.here.api.YourAPI.modLoc;

public class RegisterPatterns {
	public static void registerPatterns () {
		try {
			PatternRegistry.mapPattern(
				HexPattern.fromAngles("awd", HexDir.SOUTH_WEST), 
				modLoc("example"),
				OpExample.INSTANCE
			);
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
```

This will register `OpExample` with the pattern `awd`, meaning whenever that pattern is drawn `OpExample.execute` will be called.

## Operator Types

`Operator` is the class that everything which all your operators have to extend. However, there are two Subclasses of Operator, `ConstManaOperator` and `SpellOperator`, which are what you'll be extending for most of your patterns. `ConstManaOperator` should generally be used for things which don't affect the world, while `SpellOperator` should be used for things that do. `Operator` should only be extended if the pattern you're making won't work with the simpler (but easier to use) features available to the subclasses (for example, if you need to manipulate a variable number of elements of the stack depending on data input you'll need to extend `Operator` directly).

### SpellOperator

Things extending `SpellOperator` should (generally) be placed in `your.mod.here.common.casting.operators.spells` (or a subpackage thereof), and all have some common features.
```kotlin
object OpExampleSpell : SpellOperator {
	const val EXAMPLE_COST = (0.75 * ManaConstants.DUST_UNIT).toInt()

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val pos = args.getChecked<Vec3>(0, argc)
		
		ctx.assertVecInRange(pos)
		
		return Triple(
			Spell(pos),
			EXAMPLE_COST,
			listOf(ParticleSpray.burst(pos, 1.0))
		)
	}

	private data class Spell(val pos: Vec3) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			ctx.world.setBlockAndUpdate(BlockPos(pos), Blocks.PUMPKIN.defaultBlockState())
		}
	}
}
```

In the example spell above you can see how the `execute` method takes in the args that the player passed the operator, execute does data processing on the args, and then returns a triple with a Spell, a cost to cast that spell, and a list of particle effects to play when the spell resolves. The Spell meanwhile is responsible for actually enacting changes on the world (such as setting blocks). This Spell will only be executed if the caster has enough mana to cover the cost (and, if it's a great spell, if the caster is enlightened).