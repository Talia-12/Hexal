package ram.talia.hexal.api.nbt

import com.mojang.datafixers.util.Either
import net.minecraft.nbt.Tag

abstract class LazyLoad<L, U : Tag>(default: Either<L, U>) {
	protected var either: Either<L, U> = default

	var loaded = false

	abstract fun load(unloaded: U): L?
	abstract fun unload(loaded: L): U

	fun set(it: L) {
		either = Either.left(it)
	}

	fun set(it: U) {
		either = Either.right(it)
	}

	open fun get(): L? = either.map({ it }, { either = Either.left(load(it)); return@map either.left().get() })

	open fun getUnloaded(): U = either.map({ unload(it) }, { it })
}