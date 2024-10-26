package net.kogics.kojo.example

import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.{BatchPics, PicGdxScreen, Picture}
import net.kogics.kojo.util.Vector2D

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Dino extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new DinoScreen())
  }
}

class DinoScreen extends PicGdxScreen {
  cleari()
  drawStage(cm.black)
  val cb = canvasBounds

  val us = Picture.rectangle(30, 60)
  us.setFillColor(cm.blue)
  us.setPenColor(cm.blue)
  us.setPosition(cb.x + 150, cb.y + 0)

  us.draw()
  def ob = {
    val obe = Picture.rectangle(40, 50)
    obe.setFillColor(cm.red)
    obe.setPosition(cb.x + cb.width - 40, cb.y)
    obe
  }

  val obsticles = mutable.HashSet.empty[Picture]

  timer(1000) {
    val obs = ob
    obs.draw()
    obsticles.add(obs)
  }


  var jump = Vector2D(0, 0)
  val gravity = Vector2D(0, -0.5)
  var ovel = Vector2D(-8, 0)

  val ground = stageBot

  animate {
    jump = (jump + gravity).limit(10)
    us.translate(jump)
    if (us.collidesWith(ground)) {
      us.setPosition(us.position.x, ground.position.y)
      if (isKeyPressed(Kc.VK_SPACE)) {
        jump = Vector2D(0, 10)
      }
    }
    repeatFor(obsticles) { o =>
      o.translate(ovel)
      if (o.collidesWith(us)) {
        stopAnimation(this)
      }
      if (o.collidesWith(stageLeft)) {
        o.erase()
        obsticles.remove(o)
      }

    }
  }

  activateCanvas()
}
