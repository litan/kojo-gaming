package net.kogics.kojo.picgaming

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.utils.FloatArray
import com.badlogic.gdx.Gdx
import net.kogics.kojo.core.Point
import net.kogics.kojo.util.Vector2D

object Picture {
  private var workColor = new Color()

  private[picgaming] def setGdxColorFromAwtColor(gdxColor: Color, awtColor: java.awt.Color): Color = {
    gdxColor.set(
      awtColor.getRed / 255f,
      awtColor.getGreen / 255f,
      awtColor.getBlue / 255f,
      awtColor.getAlpha / 255f
    )
  }

  def rectangle(w: Double, h: Double): VectorPicture = new RectPicture(w, h)
  def text(msg: String, size: Int, color: java.awt.Color) =
    new TextPicture(msg, size, setGdxColorFromAwtColor(workColor, color))
}

trait Picture {
  protected var x = 0.0
  protected var y = 0.0
  protected var rotation = 0.0 // radians
  protected var scaleX = 1.0
  protected var scaleY = 1.0
  def bPoly: Polygon
  def boundaryPolygon: Polygon = {
    bPoly.setPosition(x.toFloat, y.toFloat)
    //    bPoly.setOrigin(getOriginX, getOriginY)
    //    bPoly.setRotation(getRotation)
    //    bPoly.setScale(getScaleX, getScaleY)
    bPoly
  }

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
  def translate(vel: Vector2D): Unit = translate(vel.x, vel.y)

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

trait RasterPicture extends Picture {
  private[picgaming] def realDraw(batch: SpriteBatch): Unit
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
      Picture.setGdxColorFromAwtColor(penColor, c)
    }
  }
  def setFillColor(c: java.awt.Color): Unit = {
    if (c == null) {
      fillColor = null
    }
    else {
      fillColor = if (fillColor == null) new Color() else fillColor
      Picture.setGdxColorFromAwtColor(fillColor, c)
    }
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
}

object TextPicture {
  val fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans.ttf"))
  val fontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter
  val layout = new GlyphLayout()
}

class TextPicture(msg: String, size: Int, color: Color = Color.WHITE) extends RasterPicture {
  import net.kogics.kojo.picgaming.TextPicture._
  fontParameters.size = size
  fontParameters.color = color
  fontParameters.borderColor = color
  fontParameters.minFilter = Texture.TextureFilter.Linear
  fontParameters.magFilter = Texture.TextureFilter.Linear
  val font = fontGenerator.generateFont(fontParameters)
  layout.setText(font, msg)
  val width = layout.width
  val height = layout.height

  val bPoly = {
    val w = width
    val h = height
    val vertices = Array(0f, 0f, w.toFloat, 0, w.toFloat, h.toFloat, 0, h.toFloat)
    new Polygon(vertices)
  }

  def realDraw(batch: SpriteBatch): Unit = {
    font.draw(batch, msg, x.toFloat, y.toFloat)
  }
}
