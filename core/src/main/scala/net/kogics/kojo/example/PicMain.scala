package net.kogics.kojo.example

import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.{PicGdxScreen, Picture}
import net.kogics.kojo.util.Vector2D

import java.awt.Color

class PicMain extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new PicMainScreen())
  }
}

class PicMainScreen extends PicGdxScreen {
  drawStage(Color.blue)
  val n = 10
  val players = for (i <- 1 to n) yield {
    val player = Picture.rectangle(30, 80)
    player.setPosition(-400 + 140 * i, 0)
//    player.setPenColor(Color.red)
    player.setFillColor(Color.green)
    player
  }

  val velocities = (for (i <- 1 to n) yield Vector2D(2, 1)).toBuffer

  for (player <- players) player.draw()

  animate {
    for (idx <- players.indices) {
      val player = players(idx)
      var vel = velocities(idx)

      player.translate(vel.x, vel.y)
      if (player.collidesWith(stageBorder)) {
        vel = bouncePicOffPic(player, vel, stageBorder)
        velocities(idx) = vel
      }
    }

    for (idx <- 0 until players.length - 1) {
      for (idx2 <- idx + 1 until players.length) {
        val player = players(idx)
        val vel = velocities(idx)
        val player2 = players(idx2)
        val vel2 = velocities(idx2)
        if (player.collidesWith(player2)) {
//          val nvel = bouncePicOffPic(player, vel, player2)
          val (nvel, nvel2) = bouncePicOffPicBoth(player, vel, player2, vel2)
          velocities(idx) = nvel
//          velocities(idx2).set(-nvel.x, -nvel.y)
          velocities(idx2) = nvel2
        }
      }
    }
  }
}
