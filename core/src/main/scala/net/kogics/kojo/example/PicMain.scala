package net.kogics.kojo.example

import java.awt.Color

import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.PicGdxScreen
import net.kogics.kojo.picgaming.Picture

class PicMain extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new PicGameScreen())
  }
}

class PicGameScreen extends PicGdxScreen {
  drawStage(Color.blue)

  val players = for (i <- 1 to 1) yield {
    val player = Picture.rectangle(30, 50)
    player.setPosition(-200 + 40 * i, 0)
//    player.setPenColor(Color.red)
    player.setFillColor(Color.green)
    player
  }

  for (player <- players) player.draw()
  def update(dt: Float): Unit = {
    for (player <- players) player.translate(2, 1)
  }
}
