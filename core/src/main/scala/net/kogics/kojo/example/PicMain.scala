package net.kogics.kojo.example

import java.awt.Color

import com.badlogic.gdx.math.Vector2
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
  val n = 10
  val players = for (i <- 1 to n) yield {
    val player = Picture.rectangle(30, 80)
    player.setPosition(-400 + 140 * i, 0)
//    player.setPenColor(Color.red)
    player.setFillColor(Color.green)
    player
  }

  val velocities = for (i <- 1 to n) yield new Vector2(2, 1)

  for (player <- players) player.draw()

  def update(dt: Float): Unit = {
    for (idx <- players.indices) {
      val player = players(idx)
      var vel = velocities(idx)

      player.translate(vel.x, vel.y)
      if (player.collidesWith(stageBorder)) {
        vel = bouncePicOffPic(player, vel, stageBorder)
        velocities(idx).set(vel)
      }
    }

    for (idx <- 0 until players.length - 1) {
      for (idx2 <- idx + 1 until players.length) {
        val player = players(idx)
        var vel = velocities(idx)
        val player2 = players(idx2)
        if (player.collidesWith(player2)) {
          vel = bouncePicOffPic(player, vel, player2)
          velocities(idx).set(vel)
          velocities(idx2).set(-vel.x, -vel.y)
        }
      }
    }
  }
}
