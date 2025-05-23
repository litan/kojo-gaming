package net.kogics.kojo.example

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.{BatchPics, PicGdxScreen, Picture}
import net.kogics.kojo.util.Vector2D

class Hunted extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new HuntedScreen())
  }
}

class HuntedScreen extends PicGdxScreen {
  cleari()
  drawStage(cm.black)
  val cb = canvasBounds

  val rectCharacters = false

  def playerPic: Picture = {
    val ret = if (rectCharacters) {
      val pic = Picture.rectangle(40, 40)
      pic.setFillColor(cm.yellow)
      pic.setPenColor(cm.black)
      pic
    }
    else {
      val pic1 = Picture.image("green-sq.png")
      val pic2 = Picture.image("blue-sq.png")
      val pic = picBatch(Seq(pic1, pic2))
      pic.setPosition(cb.x + cb.width / 2, cb.y + 20)
      pic
    }
    ret.setPosition(cb.x + cb.width / 2, cb.y + 20)
    ret
  }

  def hunterPic(n: Int): Picture = {
    val ret = if (rectCharacters) {
      val pic = Picture.rectangle(40, 40)
      pic.setFillColor(cm.lightBlue)
      pic.setPenColor(cm.black)
      pic
    }
    else {
      Picture.image("blue-pentagon.png")
    }
    ret.setPosition(cb.x + cb.width / (nh + 2) * n, cb.y + randomDouble(100, cb.height - 200))
    ret
  }

  val player = playerPic
  player.draw()

  val nh = 10
  val hunters = ArrayBuffer.empty[Picture]
  val huntersVel = HashMap.empty[Picture, Vector2D]
  repeatFor(1 to nh) { n =>
    val pic = hunterPic(n)
    hunters.append(pic)
    val hv = Vector2D(random(100, 200), random(100, 200))
    huntersVel(pic) = hv
    pic.draw()
  }

  def gameLost(): Unit = {
    stopAnimation()
    drawCenteredMessage("You Lost", cm.tomato, 50)
  }

  val speed = 5
  animate {
    repeatFor(hunters) { h =>
      val dt = frameDeltaTime
      var hv = huntersVel(h)
      h.translate(hv * dt)
      if (h.collidesWith(stageBorder)) {
        hv = bouncePicOffStage(h, hv)
        huntersVel(h) = hv
      }

      if (h.collidesWith(player)) {
        gameLost()
      }
    }

    player.asInstanceOf[BatchPics].showNext(200)
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
    if (isKeyPressed(Kc.VK_W)) {
      player.rotate(3)
    }
    if (isKeyPressed(Kc.VK_S)) {
      player.scale(0.95)
    }
    if (isKeyPressed(Kc.VK_A)) {
      player.invisible()
    }
    if (isKeyPressed(Kc.VK_D)) {
      player.visible()
    }

//    if (player.collidesWith(stageBorder)) {
//      gameLost()
//    }
  }

  showGameTime(60, "You Win", cm.white, 25)
  showFps(white, 30)
  activateCanvas()
}
