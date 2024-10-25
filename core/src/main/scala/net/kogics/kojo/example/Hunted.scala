package net.kogics.kojo.example

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.PicGdxScreen
import net.kogics.kojo.picgaming.Picture
import net.kogics.kojo.util.Vector2D

class Hunted extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new HuntedScreen())
  }
}

class HuntedScreen extends PicGdxScreen {
  cleari()
  drawStage(cm.darkGreen)
  val cb = canvasBounds

  val player = Picture.rectangle(40, 40)
  player.setFillColor(cm.yellow)
  player.setPenColor(cm.black)
  player.setPosition(cb.x + cb.width / 2, cb.y + 20)
  player.draw()

  val nh = 20
  val hunters = ArrayBuffer.empty[Picture]
  val huntersVel = mutable.HashMap.empty[Picture, Vector2D]
  repeatFor(1 to nh) { n =>
    val pic = Picture.rectangle(40, 40)
    pic.setFillColor(cm.lightBlue)
    pic.setPenColor(cm.black)
    pic.setPosition(cb.x + cb.width / (nh + 2) * n, cb.y + randomDouble(100, cb.height - 200))
    hunters.append(pic)
    val hv = Vector2D(random(1, 4), random(1, 4))
    huntersVel(pic) = hv
    pic.draw()
  }

  def gameLost() {
    stopAnimation(this)
    drawCenteredMessage("You Lost", cm.red, 30)
  }

  val speed = 5
  def update(dt: Float): Unit = {
    repeatFor(hunters) { h =>
      var hv = huntersVel(h)
      h.translate(hv)
      if (h.collidesWith(stageBorder)) {
        hv = bouncePicOffStage(h, hv)
        huntersVel(h) = hv
      }

      if (h.collidesWith(player)) {
        gameLost()
      }
    }

    if (isKeyPressed(Kc.VK_RIGHT)) {
      player.translate(speed, 0)
    }
    if (isKeyPressed(Kc.VK_LEFT)) {
      player.translate(-speed, 0)
    }
    if (isKeyPressed(Kc.VK_UP)) {
      player.translate(0, speed)
    }
    if (isKeyPressed(Kc.VK_DOWN)) {
      player.translate(0, -speed)
    }

    if (player.collidesWith(stageBorder)) {
      gameLost()
    }
  }
  showGameTime(10, "You Win", cm.black, 25)
  activateCanvas()
}
