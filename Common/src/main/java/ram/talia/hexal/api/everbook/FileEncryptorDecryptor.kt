package ram.talia.hexal.api.everbook

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import java.io.*
import java.nio.ByteBuffer
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


// https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-security/src/main/java/com/baeldung/encrypt/FileEncrypterDecrypter.java

internal class FileEncrypterDecrypter(private val secretKey: SecretKey, cipher: String) {
	private val cipher: Cipher

	init {
		this.cipher = Cipher.getInstance(cipher)
	}

	@Throws(InvalidKeyException::class, IOException::class)
	fun encrypt(content: String, file: File) {
		cipher.init(Cipher.ENCRYPT_MODE, secretKey)
		val iv = cipher.iv
		FileOutputStream(file).use { fileOut ->
			CipherOutputStream(fileOut, cipher).use { cipherOut ->
				fileOut.write(iv)
				cipherOut.write(content.toByteArray())
			}
		}
	}

	@Throws(InvalidKeyException::class, IOException::class)
	fun encrypt(content: CompoundTag, file: File) {
		cipher.init(Cipher.ENCRYPT_MODE, secretKey)
		val iv = cipher.iv
		FileOutputStream(file).use { fileOut ->
			fileOut.write(iv)

			NbtIo.writeCompressed(content, CipherOutputStream(fileOut, cipher))
		}
	}

	@Throws(InvalidAlgorithmParameterException::class, InvalidKeyException::class, IOException::class)
	fun decrypt(file: File): String {
		var content: String
		FileInputStream(file).use { fileIn ->
			val fileIv = ByteArray(16)
			fileIn.read(fileIv)
			cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(fileIv))
			CipherInputStream(fileIn, cipher).use { cipherIn ->
				InputStreamReader(cipherIn).use { inputReader ->
					BufferedReader(inputReader).use { reader ->
						val sb = StringBuilder()
						var line: String?
						while (reader.readLine().also { line = it } != null) {
							sb.append(line)
						}
						content = sb.toString()
					}
				}
			}
		}
		return content
	}

	@Throws(InvalidAlgorithmParameterException::class, InvalidKeyException::class, IOException::class)
	fun decryptCompound(file: File): CompoundTag {
		var content: CompoundTag

		FileInputStream(file).use { fileIn ->
			val fileIv = ByteArray(16)
			fileIn.read(fileIv)

			cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(fileIv))

			content = NbtIo.readCompressed(CipherInputStream(fileIn, cipher))
		}

		return content
	}

	companion object {
		fun getKey(uuid: UUID, cipher: String): SecretKey {
			return SecretKeySpec(longsToBytes(uuid.mostSignificantBits, uuid.leastSignificantBits), cipher)
		}

		fun longsToBytes(x: Long, y: Long): ByteArray {
			val buffer: ByteBuffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES)
			buffer.putLong(x)
			buffer.putLong(y)
			return buffer.array()
		}
	}
}