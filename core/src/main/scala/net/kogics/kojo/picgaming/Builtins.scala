package net.kogics.kojo.picgaming

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import net.kogics.kojo.gaming.WorldBounds

object Builtins {
  var stageBorder: VectorPicture = _
  var stageBot: VectorPicture = _
  var stageTop: VectorPicture = _
  var stageLeft: VectorPicture = _
  var stageRight: VectorPicture = _

  val bottomLeft = new Vector2(-WorldBounds.width / 2, -WorldBounds.height / 2)
  val topLeft = new Vector2(-WorldBounds.width / 2, WorldBounds.height / 2)
  val topRight = new Vector2(WorldBounds.width / 2, WorldBounds.height / 2)
  val bottomRight = new Vector2(WorldBounds.width / 2, -WorldBounds.height / 2)

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

  def avoidOverlap(p1: Picture, vel: Vector2, p2: Picture): Option[Vector2] = {
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

  private val incident = new Vector2()
  private val normal = new Vector2()

  def bouncePicOffStage(p: Picture, vel: Vector2): Vector2 = {
    val bPoly = p.boundaryPolygon
    val nvel = new Vector2(vel)
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
    nvel
  }

  def bouncePicOffPic(p1: Picture, vel: Vector2, p2: Picture): Vector2 = {
    if (p2 == stageBorder) bouncePicOffStage(p1, vel)
    else {
      avoidOverlap(p1, vel, p2).map { mtvNormal =>
        incident.set(vel)
        normal.set(mtvNormal)
        val velAlongNormal = incident.dot(normal)
        val impulse = normal.scl(velAlongNormal * 2)
        val reflection = incident.sub(impulse)
        reflection.cpy()
      } match {
        case Some(newVel) => newVel
        case None         => vel
      }
    }
  }

  def bouncePicOffPicBoth(p1: Picture, vel: Vector2, p2: Picture, vel2: Vector2): (Vector2, Vector2) = {
    avoidOverlap(p1, vel, p2).map { mtvNormal =>
      val restitution = 1f
      incident.set(vel)
      normal.set(mtvNormal)
      val relativeVelocity = incident.sub(vel2)
      val relativeVelAlongNormal = relativeVelocity.dot(mtvNormal)
      val impulse = normal.scl(relativeVelAlongNormal * restitution)
      impulse
    } match {
      case Some(impulse) => (vel.cpy().sub(impulse), vel2.cpy().add(impulse))
      case None          => (vel, vel2)
    }
  }
}
