package io.krugosvet.dailydish.repository

import io.krugosvet.dailydish.config.Config
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.*

private const val IMAGE_DIR = "static/"
private const val RESOURCES_DIR = "resources/$IMAGE_DIR"

@Suppress("BlockingMethodInNonBlockingContext")
class ImageRepository(
  private val config: Config
) {

  suspend fun save(image: InputStream, extension: String): URI {
    val file = "${UUID.randomUUID()}.$extension".toFile()

    file.createNewFile()

    image.use { input ->
      file.outputStream().buffered().use { output ->
        input.copyToSuspend(output)
      }
    }

    return URI.create(config.hostUrl + IMAGE_DIR + file.name)
  }

  fun delete(image: String?) {
    image ?: return

    val file = image.split("/".toPattern()).last().toFile()

    file.delete()
  }

  private fun String.toFile() = File(RESOURCES_DIR + this)

  private suspend fun InputStream.copyToSuspend(out: OutputStream): Long {
    val yieldSize: Int = 4 * 1024 * 1024
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var bytesCopied = 0L
    var bytesAfterYield = 0L

    while (true) {
      val bytes = read(buffer).takeIf { it >= 0 } ?: break

      out.write(buffer, 0, bytes)
      if (bytesAfterYield >= yieldSize) {
        yield()
        bytesAfterYield %= yieldSize
      }

      bytesCopied += bytes
      bytesAfterYield += bytes
    }

    return bytesCopied
  }

}
