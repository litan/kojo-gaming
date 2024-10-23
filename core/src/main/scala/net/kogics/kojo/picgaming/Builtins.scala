package net.kogics.kojo.picgaming

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import net.kogics.kojo.gaming.WorldBounds

object Builtins {
  var stageBorder: VectorPicture = _

  def drawStage(color: java.awt.Color): Unit = {
    stageBorder = Picture.rectangle(WorldBounds.width, WorldBounds.height)
    stageBorder.setFillColor(color)
    stageBorder.setPenColor(null)
    stageBorder.setPosition(-WorldBounds.width / 2, -WorldBounds.height / 2)
    stageBorder.draw()
  }

  def avoidOverlap(p1: Picture, vel: Vector2, p2: Picture): Option[Vector2] = {
    val poly1 = p1.boundaryPolygon
    val poly2 = p2.boundaryPolygon
    if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
      val mtv = new Intersector.MinimumTranslationVector
      val polygonOverlap = Intersector.overlapConvexPolygons(poly1, poly2, mtv)
      if (polygonOverlap) {
        p1.translate(-vel.x, -vel.y)
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

  def bouncePicOffPic(p1: Picture, vel: Vector2, p2: Picture): Vector2 = {
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
