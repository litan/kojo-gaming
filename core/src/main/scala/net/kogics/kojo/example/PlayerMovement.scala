package net.kogics.kojo.example

import java.awt.Color

import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.PicGdxScreen
import net.kogics.kojo.picgaming.Picture

class PlayerMovement extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new PlayerMovementScreen())
  }
}

class PlayerMovementScreen extends PicGdxScreen {
  drawStage(Color.black)
//  val player = Picture.rectangle(30, 80)
  val player = Picture.ellipse(150, 200)
  player.setPenColor(cm.lightBlue)
  player.setFillColor(cm.lightBlue)
//  player.rotate(30)
//  player.scale(2)
//  val player = Picture.image("blue-sq.png")
  player.draw()

  val player2 = Picture.rectangle(150, 200)
  player2.draw()


  animate {
    //    player.showNext(200)
    if (isKeyPressed(Kc.VK_UP)) {
      player.translate(0, 5)
    }
    if (isKeyPressed(Kc.VK_DOWN)) {
      player.translate(0, -5)
    }
    if (isKeyPressed(Kc.VK_RIGHT)) {
      player.translate(5, 0)
    }
    if (isKeyPressed(Kc.VK_LEFT)) {
      player.translate(-5, 0)
    }
    if (isKeyPressed(Kc.VK_W)) {
      player.rotate(3)
    }
    if (isKeyPressed(Kc.VK_S)) {
      player.scale(0.95)
    }
  }
}
