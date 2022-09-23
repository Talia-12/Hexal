package ram.talia.hexal.api.nbt

import com.mojang.datafixers.util.Either
import net.minecraft.nbt.Tag

abstract class LazyLoad<L, U : Tag> {
	protected lateinit var either: Either<L, U>

	var loaded = false

	abstract fun load(unloaded: U): L?
	abstract fun unload(loaded: L): U

	fun set(it: L) {
		either = Either.left(it)
	}

	fun set(it: U) {
		either = Either.right(it)
	}

	open fun get(): L? = either.map({ it }, { load(it) })

	open fun getUnloaded(): U = either.map({ unload(it) }, { it })
}