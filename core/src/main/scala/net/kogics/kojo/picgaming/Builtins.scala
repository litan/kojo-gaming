package net.kogics.kojo.picgaming
import java.awt.Color

import scala.language.implicitConversions

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import net.kogics.kojo.doodle
import net.kogics.kojo.gaming.WorldBounds
import net.kogics.kojo.staging
import net.kogics.kojo.util.Utils
import net.kogics.kojo.util.Vector2D

object Builtins {
  var stageBorder: VectorPicture = _
  var stageBot: VectorPicture = _
  var stageTop: VectorPicture = _
  var stageLeft: VectorPicture = _
  var stageRight: VectorPicture = _

  private val bottomLeft = new Vector2(-WorldBounds.width / 2, -WorldBounds.height / 2)
  private val topLeft = new Vector2(-WorldBounds.width / 2, WorldBounds.height / 2)
  private val topRight = new Vector2(WorldBounds.width / 2, WorldBounds.height / 2)
  private val bottomRight = new Vector2(WorldBounds.width / 2, -WorldBounds.height / 2)

  def drawStage(color: java.awt.Color): Unit = {
    stageBorder = Picture.rectangle(WorldBounds.width, WorldBounds.height)
    stageBorder.setFillColor(color)
    stageBorder.setPenColor(null)
    stageBorder.setPosition(bottomLeft.x, bottomLeft.y)
    stageBorder.draw()

    // the 0 width/height of these rects can be made 1e-6 or something if 0 causes a problem
    stageBot = Picture.rectangle(WorldBounds.width, 0)
    stageBot.setPosition(bottomLeft.x, bottomLeft.y)

    stageTop = Picture.rectangle(WorldBounds.width, 0)
    stageTop.setPosition(topLeft.x, topLeft.y)

    stageLeft = Picture.rectangle(0, WorldBounds.height)
    stageLeft.setPosition(bottomLeft.x, bottomLeft.y)

    stageRight = Picture.rectangle(0, WorldBounds.height)
    stageRight.setPosition(bottomRight.x, bottomRight.y)
  }

  private def avoidOverlap(p1: Picture, p2: Picture): Option[Vector2] = {
    val poly1 = p1.boundaryPolygon
    val poly2 = p2.boundaryPolygon
    if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
      val mtv = new Intersector.MinimumTranslationVector
      val polygonOverlap = Intersector.overlapConvexPolygons(poly1, poly2, mtv)
      if (polygonOverlap) {
        p1.translate(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth)
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

  def bouncePicOffStage(p: Picture, vel: Vector2D): Vector2D = {
    val bPoly = p.boundaryPolygon
    val nvel = new Vector2(vel.x.toFloat, vel.y.toFloat)
    if (Intersector.intersectSegmentPolygon(bottomRight, topRight, bPoly)) {
      nvel.set(-nvel.x, nvel.y)
    }
    if (Intersector.intersectSegmentPolygon(topRight, topLeft, bPoly)) {
      nvel.set(nvel.x, -nvel.y)
    }
    if (Intersector.intersectSegmentPolygon(topLeft, bottomLeft, bPoly)) {
      nvel.set(-nvel.x, nvel.y)
    }
    if (Intersector.intersectSegmentPolygon(bottomLeft, bottomRight, bPoly)) {
      nvel.set(nvel.x, -nvel.y)
    }
    Vector2D(nvel.x, nvel.y)
  }

  private val incident = new Vector2()
  private val normal = new Vector2()

  def bouncePicOffPic(p1: Picture, vel: Vector2D, p2: Picture): Vector2D = {
    if (p2 == stageBorder) bouncePicOffStage(p1, vel)
    else {
      avoidOverlap(p1, p2).map { mtvNormal =>
        incident.set(vel.x.toFloat, vel.y.toFloat)
        normal.set(mtvNormal)
        val velAlongNormal = incident.dot(normal)
        val impulse = normal.scl(velAlongNormal * 2)
        val reflection = incident.sub(impulse)
        Vector2D(reflection.x, reflection.y)
      } match {
        case Some(newVel) => newVel
        case None         => vel
      }
    }
  }

  def bouncePicOffPicBoth(p1: Picture, vel: Vector2D, p2: Picture, vel2: Vector2D): (Vector2D, Vector2D) = {
    avoidOverlap(p1, p2).map { mtvNormal =>
      val restitution = 1f
      incident.set(vel.x.toFloat, vel.y.toFloat)
      normal.set(mtvNormal)
      val relativeVelocity = incident.sub(vel2.x.toFloat, vel2.y.toFloat)
      val relativeVelAlongNormal = relativeVelocity.dot(mtvNormal)
      val impulse = normal.scl(relativeVelAlongNormal * restitution)
      Vector2D(impulse.x, impulse.y)
    } match {
      case Some(impulse) => (vel - impulse, vel2 + impulse)
      case None          => (vel, vel2)
    }
  }

  val Kc = new KeyCodes

//  def isKeyPressed(key: Int): Boolean = {
//    Gdx.input.isKeyPressed(key)
//  }

  def cleari(): Unit = {}

  val ColorMaker = doodle.Color
  val cm = doodle.Color
  implicit def rc2c(rc: doodle.Color): Color = rc.toAwt
  implicit def c2rc(c: Color): doodle.Color = Utils.awtColorToDoodleColor(c)
  implicit def rcSeq2cSeq(rcs: collection.Seq[doodle.Color]): collection.Seq[Color] = rcs.map(rc2c)

  val noColor = staging.KColor.noColor

  case class Bounds(x: Double, y: Double, width: Double, height: Double)
  def canvasBounds = Bounds(bottomLeft.x, bottomLeft.y, WorldBounds.width, WorldBounds.height)

  def drawCenteredMessage(message: String, color: Color = cm.black, fontSize: Int = 15): Unit = {
    val pic = Picture.text(message, fontSize, color)
    pic.translate(-pic.width.toDouble / 2, pic.height.toDouble / 2)
    pic.draw()
  }

  def activateCanvas(): Unit = {}
}
