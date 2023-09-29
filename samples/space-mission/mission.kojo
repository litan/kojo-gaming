// #exec

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import net.kogics.kojo.gaming._
import net.kogics.kojo.gaming.lwjgl3.StartupHelper

object Constants {
    val initialDirection = 0f
    val speedupFactor = 1f
}

object Launcher {
    def main(args: Array[String]): Unit = {
        if (StartupHelper.startNewJvmIfRequired()) return ; // This handles macOS support and helps on Windows.
        createApplication()
    }

    def createApplication(): Unit = {
        new Lwjgl3Application(new SpaceMission(), defaultConfig)
    }

    def defaultConfig = {
        val configuration = new Lwjgl3ApplicationConfiguration()
        configuration.setTitle("Space Mission")
        configuration.useVsync(true)
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate)
        configuration.setWindowedMode(1100, 900)
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png")
        configuration
    }
}

class SpaceMission extends GdxGame {
    override def create(): Unit = {
        super.create()
        setScreen(new GameScreen(this))
    }
}

class GameScreen(game: GdxGame) extends GdxScreen {

    def initialize(): Unit = {
        val orbitRadius = 400
        val gravityConstant = 1500 * 40
        val planetMass = 200

        WorldBounds.set(Gdx.graphics.getWidth, Gdx.graphics.getHeight)

        val space = new GameEntity(0, 0) {
            val renderer = new SpriteRenderer(this, "space.png")
            setSize(WorldBounds.width, WorldBounds.height)
        }
        entityStage.addActor(space)

        val planet = new GameEntity(0, 0) {
            val renderer = new SpriteRenderer(this, "planet.png")
            setScale(0.4f)
            val positioner = new PositioningCapability(this)
        }
        entityStage.addActor(planet)
        
        planet.positioner.centerAtActor(space)
        val ship = new GameEntity(0, 0) {
            val renderer = new SpriteRenderer(this, "spaceship.png")
            setScale(0.4f)
            val positioner = new PositioningCapability(this)
            //            this.setOrigin(0, -300)
            val physics = new PhysicsBehavior(this)

            val thruster =
                new GameEntity(0, 0) {
                    val renderer = new SpriteRenderer(this, "thruster.png")
                }
            addActor(thruster)
            thruster.setPosition(-thruster.getWidth, getHeight / 2 - thruster.getHeight / 2)

            val idealVel = math.sqrt(gravityConstant * planetMass / orbitRadius).toFloat
            val vel = idealVel * Constants.speedupFactor
            physics.setVelocityMagnitude(vel)
            physics.setVelocityDirection(Constants.initialDirection)

            override def act(dt: Float) {
                var cnt = 5
                val dtn = dt / cnt
                var thrusterOn = false
                if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isTouched) {
                    thrusterOn = true
                }

                while (cnt > 0) {
                    val avec = new Vector2(
                        getX + getOriginX - planet.getX - planet.getOriginX,
                        getY + getOriginY - planet.getY - planet.getOriginY
                    )
                    val radialAcc = gravityConstant * planetMass / avec.len2
                    avec.nor().scl(-radialAcc)
                    physics.addAcceleration(avec)

                    if (thrusterOn) {
                        physics.addAcceleration(300, physics.velocityDirection)
                    }

                    physics.timeStep(dtn)
                    cnt -= 1
                }

                setRotation(physics.velocityDirection)
                if (thrusterOn) {
                    thruster.setVisible(true)
                }
                else {
                    thruster.setVisible(false)
                }
            }
        }
        entityStage.addActor(ship)
        ship.positioner.centerAtActor(space)
        ship.moveBy(0, orbitRadius)
    }

    def update(dt: Float): Unit = {}
}

// asset sources - kenny.nl, opengameart.org, pngegg.com, pixabay.com, layer.ai