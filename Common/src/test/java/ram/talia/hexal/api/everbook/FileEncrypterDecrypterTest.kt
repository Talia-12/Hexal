package ram.talia.hexal.api.everbook

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
}