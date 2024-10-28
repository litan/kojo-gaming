package net.kogics.kojo.picgaming

import java.awt.image.BufferedImage

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture

object ImageConverter {
  def bufferedImageToTexture(bufferedImage: BufferedImage): Texture = {
    val width = bufferedImage.getWidth
    val height = bufferedImage.getHeight

    // Get pixel data
    val pixels = new Array[Int](width * height)
    bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

    // Create LibGDX Pixmap
    val pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888)
    val buffer = pixmap.getPixels

    // Convert ARGB to RGBA and put into ByteBuffer
    pixels.foreach { pixel =>
      val alpha = (pixel >> 24) & 0xff
      val red = (pixel >> 16) & 0xff
      val green = (pixel >> 8) & 0xff
      val blue = pixel & 0xff

      buffer.put(red.toByte)
      buffer.put(green.toByte)
      buffer.put(blue.toByte)
      buffer.put(alpha.toByte)
    }
    buffer.flip()

    // Create and return texture (Pixmap will be disposed)
    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }
}
