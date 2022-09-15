package ram.talia.hexal.api.fakes

import net.minecraft.advancements.AdvancementProgress

class FakeAdvancementProgress : AdvancementProgress() {
	override fun isDone() = true
}