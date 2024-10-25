// #exec

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

import net.kogics.kojo.gaming.lwjgl3.StartupHelper

object Launcher {
    def main(args: Array[String]): Unit = {
        if (StartupHelper.startNewJvmIfRequired()) return ; // This handles macOS support and helps on Windows.
        createApplication()
    }

    def createApplication(): Unit = {
        new Lwjgl3Application(new PicMain(), defaultConfig)
    }

    def defaultConfig = {
        val configuration = new Lwjgl3ApplicationConfiguration()
        configuration.setTitle("Snake Game")
        configuration.useVsync(true)
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate)
        configuration.setWindowedMode(800, 800)
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png")
        configuration;
    }
}

import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.{PicGdxScreen, Picture}
import net.kogics.kojo.util.Vector2D

import java.awt.Color

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

  val velocities = (for (i <- 1 to n) yield Vector2D(2, 1)).toBuffer

  for (player <- players) player.draw()

  def update(dt: Float): Unit = {
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
