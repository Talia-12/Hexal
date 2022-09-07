package ram.talia.hexal.api.nbt

import com.mojang.datafixers.util.Either
import net.minecraft.nbt.Tag
import java.util.*

class LazyLoad<L, U : Tag> {

	constructor(loaded: L) {
		either = Either.left(loaded)
		loader = Optional.empty()
	}

	constructor(unloaded: U, loader: (unloaded: U) -> L) {
		either = Either.right(unloaded)
		this.loader = Optional.of(loader)
	}

	private val either: Either<L, U>
	private val loader: Optional<(unloaded: U) -> L>

	fun get(): L {
		return either.map({ it }, { loader.get()(it) })
	}
}