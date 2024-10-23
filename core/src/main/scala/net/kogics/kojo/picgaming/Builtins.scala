package net.kogics.kojo.picgaming

import net.kogics.kojo.gaming.WorldBounds

object Builtins {
  var stageBorder: VectorPicture = _

  def drawStage(color: java.awt.Color): Unit = {
    stageBorder = Picture.rectangle(WorldBounds.width, WorldBounds.height)
    stageBorder.setFillColor(color)
    stageBorder.setPenColor(null)
    stageBorder.setPosition(-WorldBounds.width / 2, -WorldBounds.height / 2)
    stageBorder.draw()
  }

}
