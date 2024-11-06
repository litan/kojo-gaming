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

import java.awt.Color
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.{BatchPics, PicGdxScreen, Picture}
import net.kogics.kojo.util.Vector2D


class PicMain extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new PicGameScreen())
  }
}

class PicGameScreen extends PicGdxScreen {
${usercode}
}