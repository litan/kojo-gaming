package net.kogics.kojo.example

import java.awt.Color
import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.{BatchPics, ImagePicture, PicGdxScreen, Picture}

import java.io.File
import javax.imageio.ImageIO

class PlayerMovement extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new PlayerMovementScreen())
  }
}

class PlayerMovementScreen extends PicGdxScreen {
  drawStage(Color.black)
//  val player = Picture.rectangle(30, 80)
//  val img = ImageIO.read(new File("blue-sq.png"))
//  val player = Picture.image(img)

  val rightMovement = picBatch(Picture.image("blue-sq.png"), Picture.image("green-sq.png"))
//  val rightMovement = Picture.image("blue-sq.png")
  val leftMovement = picBatch(Picture.image("blue-pentagon.png"), Picture.image("green-pentagon.png"))
//  val leftMovement = Picture.image("green-pentagon.png")
  var player = rightMovement
  rightMovement.draw()
  leftMovement.drawAndHide()

  def updateImage(newPic: BatchPics) {
    if (newPic != player) {
      player.invisible()
      newPic.visible()
      newPic.setPosition(player.position)
      player = newPic
    }
  }

  animate {
    player.showNext(200)
    if (isKeyPressed(Kc.VK_UP)) {
      player.translate(0, 5)
    }
    if (isKeyPressed(Kc.VK_DOWN)) {
      player.translate(0, -5)
    }
    if (isKeyPressed(Kc.VK_RIGHT)) {
      updateImage(rightMovement)
      player.translate(5, 0)
    }
    if (isKeyPressed(Kc.VK_LEFT)) {
      updateImage(leftMovement)
      player.translate(-5, 0)
    }
    if (isKeyPressed(Kc.VK_W)) {
      player.invisible()
    }
    if (isKeyPressed(Kc.VK_S)) {
      player.visible()
    }
  }
}
