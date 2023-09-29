// #exec

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import net.kogics.kojo.gaming._
import net.kogics.kojo.gaming.lwjgl3.StartupHelper

object Constants {
    val usePE = true
}

object Launcher {
    def main(args: Array[String]): Unit = {
        if (StartupHelper.startNewJvmIfRequired()) return ; // This handles macOS support and helps on Windows.
        createApplication()
    }

    def createApplication(): Unit = {
        new Lwjgl3Application(new BabySteps(), defaultConfig)
    }

    def defaultConfig = {
        val configuration = new Lwjgl3ApplicationConfiguration()
        configuration.setTitle("Baby Steps")
        configuration.useVsync(true)
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate)
        configuration.setWindowedMode(1000, 800)
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png")
        configuration;
    }
}

class BabySteps extends GdxGame {
    override def create(): Unit = {
        super.create()
        setScreen(new PlayScreen())
    }
}

class PlayScreen extends GdxScreen {
    var player: Player = _

    def initialize(): Unit = {
        WorldBounds.set(Gdx.graphics.getWidth, Gdx.graphics.getHeight)

        player = new Player(WorldBounds.width / 2 - 25, WorldBounds.height / 2 - 25)
        entityStage.addActor(player)
    }

    def update(dt: Float): Unit = {
    }
}

class Player(x: Float, y: Float) extends GameEntity(x, y) {
    val renderer = new SpriteRenderer(this, "spaceship.png")
    private val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(500)
    physics.setFrictionMagnitude(10)
    setRotation(90)
    physics.setVelocity(50, 20)
    val wb = new WorldBoundsCapability(this)

    override def act(dt: Float): Unit = {
        super.act(dt)
        if (Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isTouched) {
            physics.addAcceleration(200, 180)
        }
        physics.timeStep(dt)
        wb.bounceOff()
    }
}

