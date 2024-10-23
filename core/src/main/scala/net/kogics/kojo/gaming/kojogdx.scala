/*
 * Copyright (C) 2023 Lalit Pant <pant.lalit@gmail.com>
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */
package net.kogics.kojo.gaming

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.math._
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.{ Array => GdxArray }
import com.badlogic.gdx.Gdx

object TextureUtils {
  def loadAnimationFromFiles(fileNames: Seq[String], frameDuration: Float, loop: Boolean): Animation[TextureRegion] = {
    val textureArray = new GdxArray[TextureRegion]
    for (fileName <- fileNames) {
      val texture = new Texture(Gdx.files.internal(fileName))
      texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
      textureArray.add(new TextureRegion(texture))
    }
    val anim = new Animation[TextureRegion](frameDuration, textureArray)
    if (loop) {
      anim.setPlayMode(PlayMode.LOOP)
    }
    else {
      anim.setPlayMode(PlayMode.NORMAL)
    }
    anim
  }

  def loadAnimationFromSheet(
      fileName: String,
      rows: Int,
      cols: Int,
      frameDuration: Float,
      loop: Boolean
  ): Animation[TextureRegion] = {
    val texture = new Texture(Gdx.files.internal(fileName), true)
    texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
    val imgWidth = texture.getWidth / cols
    val imgHeight = texture.getHeight / rows
    val images = TextureRegion.split(texture, imgWidth, imgHeight)
    val textureArray = new GdxArray[TextureRegion]
    for (r <- 0 until rows) {
      for (c <- 0 until cols) {
        textureArray.add(images(r)(c))
      }
    }
    val anim = new Animation[TextureRegion](frameDuration, textureArray)
    if (loop) {
      anim.setPlayMode(PlayMode.LOOP)
    }
    else {
      anim.setPlayMode(PlayMode.NORMAL)
    }
    anim
  }

  def loadTexture(fileName: String): TextureRegion = {
    val texture = new Texture(Gdx.files.internal(fileName))
    texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
    new TextureRegion(texture)
  }
}

trait Behavior {
  def timeStep(dt: Float): Unit
}

// Renderer Components

trait Renderer extends Behavior {
  def draw(batch: Batch, parentAlpha: Float): Unit
}

class NoOpRenderer extends Renderer {
  def draw(batch: Batch, parentAlpha: Float): Unit = {}
  def timeStep(dt: Float): Unit = {}
}

class SpriteRenderer(ge: GameEntity, fileName: String) extends Renderer {
  private val textureRegion: TextureRegion = TextureUtils.loadTexture(fileName)
  init()

  private def init(): Unit = {
    val w = textureRegion.getRegionWidth.toFloat
    val h = textureRegion.getRegionHeight.toFloat
    ge.setSize(w, h)
    ge.setOrigin(w / 2, h / 2)
  }

  def timeStep(dt: Float): Unit = {}

  def draw(batch: Batch, parentAlpha: Float): Unit = {
    import ge._
    batch.draw(
      textureRegion,
      getX,
      getY,
      getOriginX,
      getOriginY,
      getWidth,
      getHeight,
      getScaleX,
      getScaleY,
      getRotation
    )
  }
}

class AnimatedSpriteRenderer(ge: GameEntity) extends Renderer {
  private var animation: Animation[TextureRegion] = _
  private var elapsedTime = 0f

  def loadAnimationFromFiles(fileNames: Seq[String], frameDuration: Float, loop: Boolean): Unit = {
    require(animation == null, "You can load the animation only once")
    val anim = TextureUtils.loadAnimationFromFiles(fileNames, frameDuration, loop)
    init(anim)
  }

  def loadAnimationFromSheet(fileName: String, rows: Int, cols: Int, frameDuration: Float, loop: Boolean): Unit = {
    require(animation == null, "You can load the animation only once")
    val anim = TextureUtils.loadAnimationFromSheet(fileName, rows, cols, frameDuration, loop)
    init(anim)
  }

  def isAnimationFinished: Boolean = animation.isAnimationFinished(elapsedTime)

  def inited: Boolean = animation != null

  private def init(anim: Animation[TextureRegion]): Unit = {
    animation = anim
    val tr = animation.getKeyFrame(0)
    val w = tr.getRegionWidth.toFloat
    val h = tr.getRegionHeight.toFloat
    ge.setSize(w, h)
    ge.setOrigin(w / 2, h / 2)
  }

  private def currentFrame: TextureRegion = {
    animation.getKeyFrame(elapsedTime)
  }

  def timeStep(dt: Float): Unit = {
    elapsedTime += dt
  }

  def draw(batch: Batch, parentAlpha: Float): Unit = {
    import ge._
    if (inited) {
      batch.draw(
        currentFrame,
        getX,
        getY,
        getOriginX,
        getOriginY,
        getWidth,
        getHeight,
        getScaleX,
        getScaleY,
        getRotation
      )
    }
  }
}

class ParticleEffectRenderer(effectFile: String, imagesDir: String) extends Group with Renderer {
  val effect = new ParticleEffect()
  effect.load(Gdx.files.internal(effectFile), Gdx.files.internal(imagesDir))
  private val renderingActor = new ParticleEffectHolder(effect)
  addActor(renderingActor)

  def timeStep(dt: Float): Unit = {
    // the time step happens via act
    // make sure that is hooked up correctly
    require(hasParent, "ParticleEffectRenderer needs a parent game object")
  }

  override def act(dt: Float): Unit = {
    super.act(dt)
    effect.update(dt)
    if (effect.isComplete && !effect.getEmitters.first.isContinuous) {
      effect.dispose()
      this.remove()
    }
  }

  override def draw(batch: Batch, parentAlpha: Float): Unit = {
    super.draw(batch, parentAlpha)
  }

  private class ParticleEffectHolder(effect: ParticleEffect) extends Actor {
    override def draw(batch: Batch, parentAlpha: Float): Unit = {
      effect.draw(batch)
    }
  }
}

// Behavior Components

class PhysicsBehavior(ge: GameEntity) extends Behavior {
  ge.addComponent(this)
  private val currVelocity = new Vector2(0, 0)
  private val accumulatedAcceleration = new Vector2(0, 0)
  private var maxSpeed = 1000f
  private var frictionMagnitude = 0f

  def setFrictionMagnitude(dec: Float): Unit = {
    frictionMagnitude = dec
  }

  def setMaxSpeed(ms: Float): Unit = {
    maxSpeed = ms
  }

  def speed: Float = currVelocity.len()

  def isMoving: Boolean = speed > 0

  def velocity: Vector2 = currVelocity

  def setVelocity(vel: Vector2): Unit = {
    currVelocity.set(vel)
  }

  def setVelocity(vx: Float, vy: Float): Unit = {
    currVelocity.set(vx, vy)
  }

  def setVelocityMagnitude(speed: Float): Unit = {
    if (currVelocity.len == 0) currVelocity.set(speed, 0)
    else currVelocity.setLength(speed)
  }

  def setVelocityDirection(angle: Float): Unit = {
    currVelocity.setAngleDeg(angle)
  }

  def velocityDirection: Float = currVelocity.angleDeg

  def addVelocity(vel: Vector2): Unit = {
    currVelocity.add(vel.x, vel.y)
  }

  def scaleVelocity(factor: Float): Unit = {
    currVelocity.scl(factor)
  }

  def applyAcceleration(ax: Float, ay: Float): Unit = {
    accumulatedAcceleration.add(ax, ay)
  }

  def addAcceleration(acc: Vector2): Unit = applyAcceleration(acc)
  def applyAcceleration(acc: Vector2): Unit = {
    accumulatedAcceleration.add(acc)
  }

  val workVec = new Vector2(100, 100)
  def addAcceleration(magnitude: Float, direction: Float): Unit = applyAccelerationMagDir(magnitude, direction)
  def applyAccelerationMagDir(magnitude: Float, direction: Float): Unit = {
    accumulatedAcceleration.add(workVec.setLength(magnitude).setAngleDeg(direction))
  }

  def addAcceleration(magnitude: Float): Unit = applyAccelerationMag(magnitude)
  def applyAccelerationMag(magnitude: Float): Unit = {
    addAcceleration(magnitude, ge.getRotation)
  }

  def timeStep(dt: Float): Unit = {
    currVelocity.add(accumulatedAcceleration.x * dt, accumulatedAcceleration.y * dt)
    var currSpeed = currVelocity.len()
    if (accumulatedAcceleration.len == 0) {
      currSpeed -= frictionMagnitude * dt
    }
    currSpeed = MathUtils.clamp(currSpeed, 0, maxSpeed)
    setVelocityMagnitude(currSpeed)
    ge.moveBy(currVelocity.x * dt, currVelocity.y * dt)
    accumulatedAcceleration.set(0, 0)
  }

  def addGravityAcceleration(g: Float): Unit = applyGravityAcceleration(g)
  def applyGravityAcceleration(g: Float): Unit = {
    accumulatedAcceleration.add(new Vector2(g, 0).setAngleDeg(270))
  }
}

// Capability Components

class Collider(val ge: GameEntity) {
  ge.addComponent(this)
  private var bPoly: Polygon = _
  setBoundaryRectangle()

  def setBoundaryRectangle(): Unit = {
    val w = ge.getWidth
    val h = ge.getHeight
    val vertices = Array(0, 0, w, 0, w, h, 0, h)
    setBoundaryPolygon(vertices)
  }

  def setBoundaryPolygon(numSides: Int): Unit = {
    val w = ge.getWidth
    val h = ge.getHeight
    val vertices = ArrayBuffer.empty[Float]
    val twoPi = 2 * 3.14f
    for (i <- 0 until numSides) {
      val angle = i * twoPi / numSides
      vertices.append(w / 2 * MathUtils.cos(angle) + w / 2)
      vertices.append(h / 2 * MathUtils.sin(angle) + h / 2)
    }
    setBoundaryPolygon(vertices)
  }

  def setBoundaryPolygon(vertices: collection.Seq[Float]): Unit = {
    bPoly = new Polygon(vertices.toArray)
  }

  def boundaryPolygon: Polygon = {
    import ge._
    bPoly.setPosition(getX, getY)
    bPoly.setOrigin(getOriginX, getOriginY)
    bPoly.setRotation(getRotation)
    bPoly.setScale(getScaleX, getScaleY)
    bPoly
  }

  def collidesWith(other: Collider): Boolean = {
    val poly1 = boundaryPolygon
    val poly2 = other.boundaryPolygon
    if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
      Intersector.overlapConvexPolygons(poly1, poly2)
    }
    else {
      false
    }
  }

  def avoidOverlap(other: Collider): Option[Vector2] = {
    val poly1 = this.boundaryPolygon
    val poly2 = other.boundaryPolygon
    if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
      val mtv = new Intersector.MinimumTranslationVector
      val polygonOverlap = Intersector.overlapConvexPolygons(poly1, poly2, mtv)
      if (polygonOverlap) {
        ge.moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth)
        Some(mtv.normal)
      }
      else {
        None
      }
    }
    else {
      None
    }
  }

  def isCloser(other: Collider, distance: Float): Boolean = {
    import ge._
    val poly1 = boundaryPolygon
    val scaleX = (getWidth + 2 * distance) / getWidth
    val scaleY = (getHeight + 2 * distance) / getHeight
    poly1.setScale(scaleX, scaleY)
    val poly2 = other.boundaryPolygon
    if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
      Intersector.overlapConvexPolygons(poly1, poly2)
    }
    else {
      false
    }
  }

  val incident = new Vector2()
  val normal = new Vector2()

  def bounceOff(other: Collider): Unit = {
    avoidOverlap(other).foreach { mtvNormal =>
      val p = ge.getComponent(classOf[PhysicsBehavior])
      incident.set(p.velocity)
      normal.set(mtvNormal)
      val velAlongNormal = incident.dot(normal)
      val impulse = normal.scl(velAlongNormal * 2)
      val reflection = incident.sub(impulse)
      p.velocity.set(reflection)
    }
  }

  def bounceBothOff(other: Collider): Unit = {
    avoidOverlap(other).foreach { mtvNormal =>
      val restitution = 1f
      val p1 = ge.getComponent(classOf[PhysicsBehavior])
      val p2 = other.ge.getComponent(classOf[PhysicsBehavior])
      incident.set(p1.velocity)
      normal.set(mtvNormal)
      val relativeVelocity = incident.sub(p2.velocity)
      val relativeVelAlongNormal = relativeVelocity.dot(mtvNormal)
      val impulse = normal.scl(relativeVelAlongNormal * restitution)
      p1.velocity.sub(impulse)
      p2.velocity.add(impulse)
    }
  }
}

object WorldBounds {
  var boundsRect: Rectangle = _
  def set(width: Float, height: Float): Unit = {
    boundsRect = new Rectangle(0, 0, width, height)
  }

  def set(width: Int, height: Int): Unit = {
    boundsRect = new Rectangle(0, 0, width.toFloat, height.toFloat)
  }

  def set(ge: GameEntity): Unit = {
    set(ge.getWidth, ge.getHeight)
  }

  def width: Float = boundsRect.width
  def height: Float = boundsRect.height
}

class WorldBoundsCapability(ge: GameEntity) {
  ge.addComponent(this)
  import WorldBounds.boundsRect
  val pc = ge.getComponent(classOf[PhysicsBehavior])

  def wrapAround(): Unit = {
    if (ge.getX + ge.getWidth < 0) {
      ge.setX(boundsRect.width)
    }
    if (ge.getX > boundsRect.width) {
      ge.setX(-ge.getWidth)
    }
    if (ge.getY + ge.getHeight < 0) {
      ge.setY(boundsRect.height)
    }
    if (ge.getY > boundsRect.height) {
      ge.setY(-ge.getHeight)
    }
  }

  def keepWithin(): Unit = {
    import ge._
    if (getX < 0) setX(0)
    if (getX + getWidth > boundsRect.width) setX(boundsRect.width - getWidth)
    if (getY < 0) setY(0)
    if (getY + getHeight > boundsRect.height) setY(boundsRect.height - getHeight)
  }

  def bounceOff(): Unit = {
    import ge._

    def flipXVel(): Unit = {
      val vel = pc.velocity
      pc.setVelocity(-vel.x, vel.y)
    }

    def flipYVel(): Unit = {
      val vel = pc.velocity
      pc.setVelocity(vel.x, -vel.y)
    }

    if (getX < 0) {
      setX(0)
      flipXVel()
    }

    if (getX + getWidth > boundsRect.width) {
      setX(boundsRect.width - getWidth)
      flipXVel()
    }
    if (getY < 0) {
      setY(0)
      flipYVel()
    }
    if (getY + getHeight > boundsRect.height) {
      setY(boundsRect.height - getHeight)
      flipYVel()
    }
  }

  def isOutside: Boolean = {
    import ge._
    (getX + getWidth < 0) ||
    (getX > boundsRect.width) ||
    (getY + getHeight < 0) ||
    (getY > boundsRect.height)
  }
}

class PositioningCapability(ge: GameEntity) {
  ge.addComponent(this)
  import WorldBounds.boundsRect

  def centerAtPosition(x: Float, y: Float): Unit = {
    ge.setPosition(x - ge.getWidth / 2, y - ge.getHeight / 2)
  }

  def centerAtEntity(other: GameEntity): Unit = {
    centerAtPosition(other.getX + other.getWidth / 2, other.getY + other.getHeight / 2)
  }

  def alignCamera(): Unit = {
    import ge._
    val cam = getStage.getCamera
    val v = getStage.getViewport
    // center camera on actor
    cam.position.set(getX + getOriginX, getY + getOriginY, 0)
    // bound camera to layout
    cam.position.x = MathUtils.clamp(cam.position.x, cam.viewportWidth / 2, boundsRect.width - cam.viewportWidth / 2)
    cam.position.y = MathUtils.clamp(cam.position.y, cam.viewportHeight / 2, boundsRect.height - cam.viewportHeight / 2)
    cam.update()
  }
}

// The core game entity

abstract class GameEntity(x: Float, y: Float) extends Group {
  protected def renderer: Renderer

  private var components = new mutable.HashMap[String, Any]
  setPosition(x, y)

  def addComponent(c: AnyRef): Unit = {
    val cls = c.getClass
    components.put(cls.getName, c)
  }

  def getComponent[T](cls: Class[T]): T = {
    components(cls.getName).asInstanceOf[T]
  }

  def getComponentOption[T](cls: Class[T]): Option[T] = {
    components.get(cls.getName).asInstanceOf[Option[T]]
  }

  def addChild(ge: Actor): Unit = {
    addActor(ge)
  }

  def setOpacity(opacity: Float): Unit = {
    this.getColor.a = opacity
  }

  override def act(dt: Float): Unit = {
    super.act(dt) // runs actions and calls act on children
    update(dt)
    renderer.timeStep(dt)
  }

  def update(dt: Float): Unit

  override def draw(batch: Batch, parentAlpha: Float): Unit = {
    val c = getColor
    batch.setColor(c.r, c.g, c.b, c.a)
    if (isVisible) {
      renderer.draw(batch, parentAlpha)
    }
    super.draw(batch, parentAlpha) // draws children
  }
}
