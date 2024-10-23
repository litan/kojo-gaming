package net.kogics.kojo.picgaming

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.utils.FloatArray

object Picture {
  def rectangle(w: Double, h: Double): VectorPicture = new RectPicture(w, h)
}

case class Point(x: Double, y: Double)

trait Picture {
  protected var x = 0.0
  protected var y = 0.0
  protected var rotation = 0.0 // radians
  protected var scaleX = 1.0
  protected var scaleY = 1.0
  def boundaryPolygon: Polygon

  def draw(): Unit = {
    PicGdxScreen.stage.addPicture(this)
  }
  def setPosition(x0: Double, y0: Double): Unit = {
    x = x0
    y = y0
  }

  def position: Point = Point(x, y)
  def translate(dx: Double, dy: Double): Unit = {
    x += dx
    y += dy
  }

  def collidesWith(other: Picture): Boolean = {
    if (other == Builtins.stageBorder)
      collidesWithStage()
    else
      Intersector.overlapConvexPolygons(boundaryPolygon, other.boundaryPolygon)
  }

  def collidesWithStage(): Boolean = {
    Intersector.intersectPolygonEdges(
      new FloatArray(boundaryPolygon.getTransformedVertices),
      new FloatArray(Builtins.stageBorder.boundaryPolygon.getTransformedVertices)
    )
  }

}

trait VectorPicture extends Picture {
  protected var penColor: Color = Color.RED
  protected var fillColor: Color = _
  private[picgaming] def realDrawOutlined(shapeRenderer: ShapeRenderer): Unit
  private[picgaming] def realDrawFilled(shapeRenderer: ShapeRenderer): Unit
  private[picgaming] def hasFill: Boolean = fillColor != null
  private[picgaming] def hasPen: Boolean = penColor != null

  def setPenColor(c: java.awt.Color): Unit = {
    if (c == null) {
      penColor = null
    }
    else {
      setGdxColorFromAwtColor(penColor, c)
    }
  }
  def setFillColor(c: java.awt.Color): Unit = {
    if (c == null) {
      fillColor = null
    }
    else {
      fillColor = if (fillColor == null) new Color() else fillColor
      setGdxColorFromAwtColor(fillColor, c)
    }
  }

  private def setGdxColorFromAwtColor(gdxColor: Color, awtColor: java.awt.Color): Unit = {
    gdxColor.set(
      awtColor.getRed / 255f,
      awtColor.getGreen / 255f,
      awtColor.getBlue / 255f,
      awtColor.getAlpha / 255f
    )
  }

}

class RectPicture(w: Double, h: Double) extends VectorPicture {
  lazy val bPoly = {
    val vertices = Array(0f, 0f, w.toFloat, 0, w.toFloat, h.toFloat, 0, h.toFloat)
    new Polygon(vertices)
  }
  def realDrawOutlined(shapeRenderer: ShapeRenderer): Unit = {
    shapeRenderer.setColor(penColor)
    shapeRenderer.rect(x.toFloat, y.toFloat, w.toFloat, h.toFloat)
  }
  def realDrawFilled(shapeRenderer: ShapeRenderer): Unit = {
    shapeRenderer.setColor(fillColor)
    shapeRenderer.rect(x.toFloat, y.toFloat, w.toFloat, h.toFloat)
  }

  def boundaryPolygon: Polygon = {
    bPoly.setPosition(x.toFloat, y.toFloat)
//    bPoly.setOrigin(getOriginX, getOriginY)
//    bPoly.setRotation(getRotation)
//    bPoly.setScale(getScaleX, getScaleY)
    bPoly
  }
}
