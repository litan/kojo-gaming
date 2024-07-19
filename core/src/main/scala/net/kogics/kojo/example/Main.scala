package net.kogics.kojo.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import net.kogics.kojo.example.Constants.speedup
import net.kogics.kojo.gaming._

object Constants {
  val speedup = 3f
}

class Main extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new GameScreen())
  }
}

class Player(x0: Float, y0: Float) extends GameEntity(x0, y0) {
  val renderer = new SpriteRenderer(this, "blue-pentagon.png")
  val collider = new Collider(this)
//  collider.setBoundaryPolygon(Array(30.0f, 1.0f, 7.0f, 2.0f, -1.0f, 27.0f, -1.0f, 38.0f, 8.0f, 63.0f, 27.0f, 63.0f, 47.0f, 37.0f, 47.0f, 28.0f))
  collider.setBoundaryPolygon(
    Array(0.69503317177944f, 12.332247706143217f, 0.9846975515161277f, 52.305932109806115f, 39.2203956767589f,
      65.05116481822037f, 62.683210435430595f, 33.47774742692142f, 38.06173815781215f, 0.7456725166757117f)
  )
  val physics = new PhysicsBehavior(this)
  physics.setMaxSpeed(500)
  physics.setVelocity(50 * speedup, 0 * speedup)

  val wb = new WorldBoundsCapability(this)

  def update(dt: Float): Unit = {
    if (Gdx.input.isKeyPressed(Keys.SPACE)) {
      physics.addAcceleration(200, 135)
    }
    physics.timeStep(dt)
    wb.bounceOff()
  }
}

class Rock(x0: Float, y0: Float) extends GameEntity(x0, y0) {
  val renderer = new SpriteRenderer(this, "green-pentagon.png")
  val collider = new Collider(this)
//  collider.setBoundaryPolygon(Array(25.0f, 1.0f, 13.0f, 1.0f, 0.0f, 16.0f, 5.0f, 84.0f, 15.0f, 98.0f, 33.0f, 98.0f, 48.0f, 84.0f, 49.0f, 18.0f, 39.0f, 3.0f))
  collider.setBoundaryPolygon(
    Array(0.69503317177944f, 12.332247706143217f, 0.9846975515161277f, 52.305932109806115f, 39.2203956767589f,
      65.05116481822037f, 62.683210435430595f, 33.47774742692142f, 38.06173815781215f, 0.7456725166757117f)
  )
  val physics = new PhysicsBehavior(this)
  physics.setMaxSpeed(500)
  physics.setVelocity(20 * speedup, 0 * speedup)

  val wb = new WorldBoundsCapability(this)

  def update(dt: Float): Unit = {
    physics.timeStep(dt)
    wb.bounceOff()
  }
}

class GameScreen extends GdxScreen {
  val player = new Player(WorldBounds.width / 2 - 200, WorldBounds.height / 2)
  stage.addEntity(player)

  val rock = new Rock(WorldBounds.width / 2 - 100, WorldBounds.height / 2)
  stage.addEntity(rock)

  def update(dt: Float): Unit = {
    val pCollider = player.getComponent(classOf[Collider])
    val rCollider = rock.getComponent(classOf[Collider])
//    if (pCollider.collidesWith(rCollider)) {
//      pause()
//    }
    pCollider.bounceBothOff(rCollider)
  }
}
