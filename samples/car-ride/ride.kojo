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
import com.badlogic.gdx.math.Vector2
import scala.collection.mutable.HashSet
import KojoUtils._

object Launcher {
    def main(args: Array[String]): Unit = {
        if (StartupHelper.startNewJvmIfRequired()) return ; // This handles macOS support and helps on Windows.
        createApplication()
    }

    def createApplication(): Unit = {
        new Lwjgl3Application(new CarRide(), defaultConfig)
    }

    def defaultConfig = {
        val configuration = new Lwjgl3ApplicationConfiguration()
        configuration.setTitle("Car Ride")
        configuration.useVsync(true)
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate)
        configuration.setWindowedMode(1000, 800)
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png")
        configuration;
    }
}

class CarRide extends GdxGame {
    override def create(): Unit = {
        super.create()
        setScreen(new PlayScreen(this))
    }
}

class MessageScreen(game: GdxGame, message: String, color: Color) extends GdxScreen {
    val okButton = new TextButton("Ok", game.textButtonStyle)
    okButton.addListener {
        case e: InputEvent if e.getType == Type.touchDown =>
            Gdx.app.exit()
            true
        case _ =>
            false
    }

    game.largeLabelStyle.fontColor = color
    uiTable.add(new Label(message, game.largeLabelStyle))
    uiTable.row().pad(70f)
    uiTable.add(okButton)

    def update(dt: Float): Unit = {}
}

class PlayScreen(game: GdxGame) extends GdxScreen {
    val carColliders = HashSet.empty[(OtherCar, Collider)]
    def spawnCar() {
        val car = new OtherCar(player.getX + randomDouble(-20, 20).toFloat, WorldBounds.height)
        stage.addEntity(car)
        val oc = car.getComponent(classOf[Collider])
        carColliders.add(car, oc)
    }

    val player = new PlayerCar(WorldBounds.width / 2 - 25, WorldBounds.height / 2 - 25)
    stage.addEntity(player)

    val pc = player.getComponent(classOf[Collider])
    spawnCar()

    var spawnDelta = 0.0

    val carSound = Gdx.audio.newMusic(Gdx.files.internal("car-move.mp3"))
    carSound.setLooping(true)

    val carCrashSound = Gdx.audio.newSound(Gdx.files.internal("car-crash.mp3"))

    override def show() {
        carSound.play()
    }

    override def hide() {
        carSound.stop()
    }

    def update(dt: Float) {
        spawnDelta += dt
        if (spawnDelta > 0.75) {
            spawnDelta = 0
            spawnCar()
        }
        for ((car, oc) <- carColliders) {
            if (pc.collidesWith(oc)) {
                carCrashSound.play
                game.setScreen(new MessageScreen(game, "You Lost", Color.RED))
            }
            if (car.isOutsideWorld) {
                carColliders.remove(car, oc)
                stage.removeEntity(car)
            }
        }
    }
}

class PlayerCar(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    def vec(x: Float, y: Float) = new Vector2(x, y)

    val renderer = new SpriteRenderer(this, "car1.png")
    val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(300)
    //    physics.setFrictionMagnitude(100)

    val wb = new WorldBoundsCapability(this)
    val collider = new Collider(this)
    val speed = 3800

    def update(dt: Float) {
        val nv = physics.velocity
        nv.scl(0.9f)
        physics.setVelocity(nv.x, nv.y)

        if (isKeyPressed(Keys.UP)) {
            physics.addVelocity(vec(0, speed * dt))
        }
        if (isKeyPressed(Keys.DOWN)) {
            physics.addVelocity(vec(0, -speed * dt))
        }
        if (isKeyPressed(Keys.LEFT)) {
            physics.addVelocity(vec(-speed * dt, 0))
        }
        if (isKeyPressed(Keys.RIGHT)) {
            physics.addVelocity(vec(speed * dt, 0))
        }
        physics.timeStep(dt)
        wb.wrapAround()
    }
}

class OtherCar(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    val renderer = new SpriteRenderer(this, "car2.png")
    val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(500)
    physics.setVelocity(0, -300)

    val wb = new WorldBoundsCapability(this)
    val collider = new Collider(this)

    def update(dt: Float) {
        physics.timeStep(dt)
        //        wb.wrapAround()
    }

    def isOutsideWorld = wb.isOutside
}
