// #exec

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
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
        new Lwjgl3Application(new LanderGame(), defaultConfig)
    }

    def defaultConfig = {
        val configuration = new Lwjgl3ApplicationConfiguration()
        configuration.setTitle("Lunar Lander")
        configuration.useVsync(true)
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate)
        configuration.setWindowedMode(1000, 800)
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png")
        configuration;
    }
}

class LanderGame extends GdxGame {
    override def create(): Unit = {
        super.create()
        setScreen(new MenuScreen(this))
    }
}

class MenuScreen(game: GdxGame) extends GdxScreen {
    val title = new GameEntity(0, 0) {
        val renderer = new SpriteRenderer(this, "lunar-lander.png")
        def update(dt: Float) {}
    }

    val startButton = new TextButton("Start", game.textButtonStyle)
    startButton.addListener {
        case e: InputEvent if e.getType == Type.touchDown =>
            game.setScreen(new LanderScreen(game))
            true
        case _ =>
            false
    }

    val quitButton = new TextButton("Quit", game.textButtonStyle)
    quitButton.addListener {
        case e: InputEvent if e.getType == Type.touchDown =>
            Gdx.app.exit()
            true
        case _ =>
            false
    }

    uiTable.add(title).colspan(2)
    uiTable.row().pad(70)
    uiTable.add(startButton)
    uiTable.add(quitButton)

    def update(dt: Float): Unit = {}
}

class LanderScreen(game: GdxGame) extends GdxScreen {
    var gameOver = false

    val space = new GameEntity(0, 0) {
        val renderer = new SpriteRenderer(this, "space.png")
        setSize(WorldBounds.width, WorldBounds.height)
        def update(dt: Float) {}
    }
    stage.addEntity(space)

    val moon = new Moon(0, 0, space)
    stage.addEntity(moon)

    val spaceShip = new SpaceShip(WorldBounds.width / 2, WorldBounds.height * 4 / 5)
    stage.addEntity(spaceShip)

    val fpsLabel = new Label("FPS:", game.smallLabelStyle)
    val velocityLabel = new Label("Velocity:", game.smallLabelStyle)
    uiTable.add(fpsLabel).width(WorldBounds.width * 0.95f)
    uiTable.row()
    uiTable
        .add(velocityLabel)
        .width(WorldBounds.width * 0.95f)
        .padBottom(WorldBounds.height * 0.9f)

    val shipCollider = spaceShip.getComponent(classOf[Collider])
    val moonCollider = moon.getComponent(classOf[Collider])
    val shipPhysics = spaceShip.getComponent(classOf[PhysicsBehavior])

    def update(dt: Float): Unit = {
        if (!gameOver) {
            fpsLabel.setText(s"FPS: ${Gdx.graphics.getFramesPerSecond}")
            if (Gdx.graphics.getFrameId % 10 == 0) {
                velocityLabel.setText(s"Velocity: ${shipPhysics.velocity.y.toInt}")
            }

            if (shipCollider.collidesWith(moonCollider)) {
                if (shipPhysics.speed > 60) {
                    val explosion = new Explosion(0, 0)
                    stage.addEntity(explosion)
                    explosion.positioner.centerAtEntity(spaceShip)
                    explosion.moveBy(0, -5)
                    explosion.scaleBy(0.1f)
                    spaceShip.remove()
                    Timer.schedule(
                        () => game.setScreen(new MessageScreen(game, "You Lost!", Color.RED)),
                        1.5f,
                        0,
                        0
                    )
                }
                else {
                    spaceShip.addAction(Actions.fadeOut(1.5f))
                    spaceShip.addAction(Actions.after(Actions.removeActor()))
                    spaceShip.addAction(Actions.after(Actions.run { () =>
                        game.setScreen(new MessageScreen(game, "You Won!", Color.GREEN))
                    }))
                    spaceShip.deactivate() // stop it from moving down further etc
                }
                gameOver = true
            }
        }
    }
}

class SpaceShip(x: Float, y: Float) extends GameEntity(x, y) {
    val renderer = new SpriteRenderer(this, "spaceship.png")

    private var active = true
    private val collider = new Collider(this)
    private val physics = new PhysicsBehavior(this)
    collider.setBoundaryPolygon(8)
    physics.setMaxSpeed(500)
    physics.setFrictionMagnitude(10)
    setRotation(90)
    moveBy(-getWidth / 2, -getHeight / 2)

    val thruster =
        if (Constants.usePE)
            new ParticleEffectRenderer("thruster.pfx", "")
        else
            new GameEntity(0, 0) {
                val renderer = new SpriteRenderer(this, "thruster.png")
                def update(dt: Float) {}
            }

    addChild(thruster)
    thruster.setPosition(-thruster.getWidth, getHeight / 2 - thruster.getHeight / 2)
    if (Constants.usePE) {
        thruster.setRotation(90)
        thruster.setScale(0.35f)
    }

    def update(dt: Float): Unit = {
        if (active) {
            if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isTouched) {
                physics.addAcceleration(300, 90)
                thruster.setVisible(true)
            }
            else {
                thruster.setVisible(false)
            }

            physics.addGravityAcceleration(100)
            physics.timeStep(dt)
        }
        else {
            thruster.setVisible(false)
        }
    }

    def deactivate(): Unit = {
        active = false
    }
}

class Moon(x: Float, y: Float, bg: GameEntity) extends GameEntity(x, y) {
    val renderer = new SpriteRenderer(this, "moon.png")
    private val collider = new Collider(this)
    collider.setBoundaryPolygon(8)
    private val positioner = new PositioningCapability(this)
    positioner.centerAtEntity(bg)
    moveBy(0, -(bg.getHeight / 2 - 0))
    def update(dt: Float) {}
}

class Explosion(x: Float, y: Float) extends GameEntity(x, y) {
    val renderer = new AnimatedSpriteRenderer(this)
    renderer.loadAnimationFromSheet("explosion.png", 5, 5, 0.03f, false)
    val positioner = new PositioningCapability(this)

    def update(dt: Float): Unit = {
        if (renderer.isAnimationFinished) {
            remove()
        }
    }
}

class MessageScreen(game: GdxGame, message: String, color: Color) extends GdxScreen {
    val okButton = new TextButton("Ok", game.textButtonStyle)
    okButton.addListener {
        case e: InputEvent if e.getType == Type.touchDown =>
            game.setScreen(new MenuScreen(game))
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

// asset sources - kenny.nl, opengameart.org, pngegg.com, pixabay.com, layer.ai