package net.kogics.kojo.gaming

import com.badlogic.gdx.graphics.Color

object Utils {
  def setGdxColorFromAwtColor(gdxColor: Color, awtColor: java.awt.Color): Color = {
    gdxColor.set(
      awtColor.getRed / 255f,
      awtColor.getGreen / 255f,
      awtColor.getBlue / 255f,
      awtColor.getAlpha / 255f
    )
  }

}
