package io.krugosvet.dailydish.repository

import io.krugosvet.dailydish.config.Config
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val IMAGE_DIR = "static/"
private const val RESOURCES_DIR = "resources/$IMAGE_DIR"
private const val YIELD_SIZE: Int = 4 * 1024 * 1024

@Suppress("BlockingMethodInNonBlockingContext")
class ImageRepository(
  private val config: Config
) {

  /**
   * @return remote path to [image]
   */
  suspend fun save(image: InputStream, extension: String): String {
    val file = "${UUID.randomUUID()}.$extension".toFile()

    file.createNewFile()

    image.use { input ->
      file.outputStream().buffered().use { output ->
        input.copyToSuspend(output)
      }
    }

    return config.hostUrl + IMAGE_DIR + file.name
  }

  fun delete(image: String?) {
    image ?: return

    val file = image.split("/").last().toFile()

    file.delete()
  }

  private fun String.toFile() = File(RESOURCES_DIR + this)

  private suspend fun InputStream.copyToSuspend(out: OutputStream): Long {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var bytesCopied = 0L
    var bytesAfterYield = 0L

    while (true) {
      val bytes = read(buffer).takeIf { it >= 0 } ?: break

      out.write(buffer, 0, bytes)
      if (bytesAfterYield >= YIELD_SIZE) {
        yield()
        bytesAfterYield %= YIELD_SIZE
      }

      bytesCopied += bytes
      bytesAfterYield += bytes
    }

    return bytesCopied
  }

}
