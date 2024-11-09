package net.kogics.kojo.example

import java.awt.Color

import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.PicGdxScreen
import net.kogics.kojo.picgaming.Picture

class PlayerCloseness extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new PlayerClosenessScreen())
  }
}

class PlayerClosenessScreen extends PicGdxScreen {
  cleari()
  val pic1 = Picture.rectangle(50, 50)
//  pic1.scale(0.9)
  pic1.rotate(3)
  val pic2 = Picture.rectangle(50, 50)
  pic2.setPosition(100, 0)

  pic1.draw()
  pic2.draw()

  println(pic1.isCloser(pic2, 51))
}
