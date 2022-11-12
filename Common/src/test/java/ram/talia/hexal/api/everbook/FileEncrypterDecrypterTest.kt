package ram.talia.hexal.api.everbook

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.nbt.CompoundTag
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

internal class FileEncrypterDecrypterTest {

	lateinit var fileEncrypterDecrypter: FileEncrypterDecrypter

	@BeforeEach
	fun setUp() {
		val secretKey = FileEncrypterDecrypter.getKey(UUID.randomUUID(), "AES")
		fileEncrypterDecrypter = FileEncrypterDecrypter(secretKey, "AES/CBC/PKCS5Padding")
	}

	@AfterEach
	fun tearDown() {
		File("baz.enc").delete() // cleanup
	}

	@Test
	fun testEncryptDecrypt() {
		val originalContent = "foobar"

		fileEncrypterDecrypter.encrypt(originalContent, File("baz.enc"))
		val decryptedContent = fileEncrypterDecrypter.decrypt(File("baz.enc"))

		assertEquals(originalContent, decryptedContent)
	}

	@Test
	fun testEncryptDecryptCompound() {
		val originalContent = CompoundTag()
		originalContent.putString("testKey", "testVal")

		fileEncrypterDecrypter.encrypt(originalContent, File("baz.enc"))
		val decryptedContent = fileEncrypterDecrypter.decryptCompound(File("baz.enc"))

		assertEquals(originalContent, decryptedContent)
	}

	@Test
	fun testEncryptDecryptEverbook() {
		val everbook = Everbook(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"))

		everbook.setIota(HexPattern.fromAngles("a", HexDir.EAST), SpellDatum.make(15.0))

		fileEncrypterDecrypter.encrypt(everbook.serialiseToNBT(), File("baz.enc"))
		val decryptedContent = fileEncrypterDecrypter.decryptCompound(File("baz.enc"))

		assertEquals(everbook.serialiseToNBT(), decryptedContent)
	}
}