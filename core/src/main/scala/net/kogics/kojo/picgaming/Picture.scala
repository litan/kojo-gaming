package net.kogics.kojo.picgaming

import java.awt.image.BufferedImage
import java.awt.Image

import scala.collection.mutable.ArrayBuffer

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.utils.FloatArray
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.Gdx
import net.kogics.kojo.core.Point
import net.kogics.kojo.gaming.TextureUtils
import net.kogics.kojo.gaming.Utils
import net.kogics.kojo.util.Vector2D

object Picture {
  var stage: PicGdxStage = _
  private var workColor = new Color()

  def rectangle(w: Double, h: Double): VectorPicture = new RectPicture(w, h)
  def ellipse(rx: Double, ry: Double): VectorPicture = new EllipsePicture(rx, ry)
  def circle(r: Double): VectorPicture = ellipse(r, r)
  def text(msg: String, size: Int, color: java.awt.Color = java.awt.Color.RED) =
    new TextPicture(msg, size, Utils.setGdxColorFromAwtColor(workColor, color))
  def image(fileName: String) = {
    val textureRegion = TextureUtils.loadTexture(fileName)
    new ImagePicture(textureRegion, None)
  }
  def image(fileName: String, boundary: collection.Seq[Double]) = {
    val textureRegion = TextureUtils.loadTexture(fileName)
    new ImagePicture(textureRegion, Some(boundary))
  }
  def image(img: Image) = {
    val textureRegion = ImageConverter.textureFromBufferedImage(img.asInstanceOf[BufferedImage])
    new ImagePicture(textureRegion, None)
  }
  def image(img: Image, boundary: collection.Seq[Double]) = {
    val textureRegion = ImageConverter.textureFromBufferedImage(img.asInstanceOf[BufferedImage])
    new ImagePicture(textureRegion, Some(boundary))
  }

  def batch(pics: RasterPicture*): BatchPics = new BatchPics(pics)
  def batch(pics: collection.Seq[RasterPicture]): BatchPics = new BatchPics(pics)
}

trait Picture {
  protected var x = 0.0
  protected var y = 0.0
  protected var rotation = 0.0 // radians
  protected var scaleX = 1.0
  protected var scaleY = 1.0
  protected var opacity = 1.0
  protected var showing = true

  def bPoly: Polygon
  def boundaryPolygon: Polygon = {
    val bP = bPoly
    bP.setPosition(x.toFloat, y.toFloat)
    //    bPoly.setOrigin(getOriginX, getOriginY)
    bP.setRotation(rotation.toFloat)
    bP.setScale(scaleX.toFloat, scaleY.toFloat)
    bP
  }

  def draw(): Unit = {
    Picture.stage.addPicture(this)
  }

  def visible(): Unit = {
    showing = true
  }

  def invisible(): Unit = {
    showing = false
  }

  def drawAndHide(): Unit = {
    draw()
    invisible()
  }

  def erase(): Unit = {
    Picture.stage.removePicture(this)
  }

  def setPosition(x0: Double, y0: Double): Unit = {
    x = x0
    y = y0
  }
  def setPosition(pos: Point): Unit = setPosition(pos.x, pos.y)

  def position: Point = Point(x, y)
  def translate(dx: Double, dy: Double): Unit = {
    x += dx
    y += dy
  }
  def translate(vel: Vector2D): Unit = translate(vel.x, vel.y)
  def offset(dx: Double, dy: Double): Unit = translate(dx, dy)
  def offset(vel: Vector2D): Unit = translate(vel.x, vel.y)

  def rotate(a: Double): Unit = {
    rotation += a
  }

  def scale(factor: Double): Unit = {
    val sf = if (factor == 0) Double.MinPositiveValue else factor
    scaleX *= sf
    scaleY *= sf
  }

  def collidesWith(other: Picture): Boolean = {
    if (other == this) {
      false
    }
    else if (other == Builtins.stageBorder)
      collidesWithStage()
    else {
      val poly1 = boundaryPolygon
      val poly2 = other.boundaryPolygon
      if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle))
        Intersector.overlapConvexPolygons(poly1, poly2)
      else
        false
    }
  }

  def collidesWithStage(): Boolean = {
    Intersector.intersectPolygonEdges(
      new FloatArray(boundaryPolygon.getTransformedVertices),
      new FloatArray(Builtins.stageBorder.boundaryPolygon.getTransformedVertices)
    )
  }

  def setOpacity(o: Double): Unit = {
    opacity = o
  }

  protected def closenessScaleFactor(bp: Polygon, distance: Double): (Float, Float) = {
    val bounds = bp.getBoundingRectangle
    val w = bounds.width
    val h = bounds.height
    val scaleX = (w + distance.toFloat) / w
    val scaleY = (h + distance.toFloat) / h
    (scaleX, scaleY)
  }

  def isCloser(other: Picture, distance: Double): Boolean = {
    val poly1 = boundaryPolygon
    val (scaleX, scaleY) = closenessScaleFactor(poly1, distance)
    poly1.setScale(poly1.getScaleX * scaleX, poly1.getScaleY * scaleY)
    val poly2 = other.boundaryPolygon
    if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
      Intersector.overlapConvexPolygons(poly1, poly2)
    }
    else {
      false
    }
  }
}

trait RasterPicture extends Picture {
  private[picgaming] def realDraw(batch: SpriteBatch): Unit
}

trait VectorPicture extends Picture {
  protected var penColor: Color = _
  protected var fillColor: Color = _
  protected var penThickness = 2

  private[picgaming] def drawVectorShape(renderer: ShapeRenderer): Unit

  private[picgaming] def realDrawOutlined(shapeRenderer: ShapeRenderer): Unit = {
    if (!showing) return
    if (opacity < 1) {
      if (!Gdx.gl.glIsEnabled(GL20.GL_BLEND)) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
      }
      shapeRenderer.setColor(penColor.r, penColor.g, penColor.b, opacity.toFloat)
    }
    else {
      shapeRenderer.setColor(penColor)
    }
    Gdx.gl.glLineWidth(penThickness.toFloat)
    drawVectorShape(shapeRenderer)
    // consider resetting shapeRenderer color and gl line width
  }

  private[picgaming] def realDrawFilled(shapeRenderer: ShapeRenderer): Unit = {
    if (!showing) return
    if (opacity < 1) {
      if (!Gdx.gl.glIsEnabled(GL20.GL_BLEND)) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
      }
      shapeRenderer.setColor(fillColor.r, fillColor.g, fillColor.b, opacity.toFloat)
    }
    else {
      shapeRenderer.setColor(fillColor)
    }
    drawVectorShape(shapeRenderer)
    // consider resetting shapeRenderer color
  }

  private[picgaming] def hasFill: Boolean = fillColor != null
  private[picgaming] def hasPen: Boolean = penColor != null
  setPenColor(java.awt.Color.RED)

  def setPenColor(c: java.awt.Color): Unit = {
    if (c == null) {
      penColor = null
    }
    else {
      penColor = if (penColor == null) new Color() else penColor
      Utils.setGdxColorFromAwtColor(penColor, c)
    }
  }
  def setFillColor(c: java.awt.Color): Unit = {
    if (c == null) {
      fillColor = null
    }
    else {
      fillColor = if (fillColor == null) new Color() else fillColor
      Utils.setGdxColorFromAwtColor(fillColor, c)
    }
  }
  def setPenThickness(t: Int): Unit = {
    penThickness = t
  }
}

class RectPicture(w: Double, h: Double) extends VectorPicture {
  lazy val bPoly = {
    val vertices = Array(0, 0, w.toFloat, 0, w.toFloat, h.toFloat, 0, h.toFloat)
    new Polygon(vertices)
  }

  private[picgaming] def drawVectorShape(shapeRenderer: ShapeRenderer): Unit = {
    shapeRenderer.rect(
      x.toFloat,
      y.toFloat,
      0,
      0,
      w.toFloat,
      h.toFloat,
      scaleX.toFloat,
      scaleY.toFloat,
      rotation.toFloat
    )
  }
}

class EllipsePicture(rx: Double, ry: Double) extends VectorPicture {
  lazy val bPoly = {
    val vertices = ArrayBuffer.empty[Float]
    def x(t: Double) = rx * math.cos(t.toRadians)
    def y(t: Double) = ry * math.sin(t.toRadians)
    for (i <- 1 to 360) {
      vertices.append(x(i).toFloat)
      vertices.append(y(i).toFloat)
    }
    new Polygon(vertices.toArray)
  }

  private[picgaming] def drawVectorShape(shapeRenderer: ShapeRenderer): Unit = {
    val savedTransform = shapeRenderer.getTransformMatrix.cpy()
    shapeRenderer.translate(x.toFloat, y.toFloat, 0)
    shapeRenderer.rotate(0, 0, 1, rotation.toFloat)
    shapeRenderer.scale(scaleX.toFloat, scaleY.toFloat, 1)
    shapeRenderer.ellipse(
      -rx.toFloat,
      -ry.toFloat,
      rx.toFloat * 2,
      ry.toFloat * 2
    )
    shapeRenderer.setTransformMatrix(savedTransform)
  }

  protected override def closenessScaleFactor(bp: Polygon, distance: Double): (Float, Float) = {
    val bounds = bp.getBoundingRectangle
    val w = bounds.width
    val h = bounds.height
    val scaleX = (w + 2 * distance.toFloat) / w
    val scaleY = (h + 2 * distance.toFloat) / h
    (scaleX, scaleY)
  }
}

object TextPicture {
  val fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans.ttf"))
  val fontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter
  val layout = new GlyphLayout()
}

class TextPicture(var msg: String, size: Int, color: Color = Color.WHITE) extends RasterPicture {
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

  lazy val bPoly = {
    val w = width
    val h = height
    val vertices = Array(0f, 0f, w, 0, w, -h, 0, -h)
    new Polygon(vertices)
  }

  def setText(m: String): Unit = {
    msg = m
  }

  private[picgaming] def realDraw(batch: SpriteBatch): Unit = {
    if (!showing) return
    var clr: Color = null
    if (opacity < 1) {
      clr = font.getColor.cpy()
      font.setColor(clr.r, clr.g, clr.b, opacity.toFloat)
    }

    val savedTransform = batch.getTransformMatrix.cpy();
    batch.getTransformMatrix.translate(x.toFloat, y.toFloat, 0f)
    batch.getTransformMatrix.rotate(0, 0, 1, rotation.toFloat)
    batch.getTransformMatrix.scale(scaleX.toFloat, scaleY.toFloat, 1)
    batch.setTransformMatrix(batch.getTransformMatrix)
    font.draw(batch, msg, 0, 0)
    batch.setTransformMatrix(savedTransform)

    if (opacity < 1) {
      font.setColor(clr)
    }
  }
}

class ImagePicture(textureRegion: TextureRegion, boundary: Option[collection.Seq[Double]]) extends RasterPicture {
  val width = textureRegion.getRegionWidth.toFloat
  val height = textureRegion.getRegionHeight.toFloat

  lazy val bPoly = boundary match {
    case None =>
      val w = width
      val h = height
      val vertices = Array(0, 0, w, 0, w, h, 0, h)
      new Polygon(vertices)
    case Some(b) =>
      new Polygon(b.map(_.toFloat).toArray)
  }

  private[picgaming] def realDraw(batch: SpriteBatch): Unit = {
    if (!showing) return
    var clr: Color = null
    if (opacity < 1) {
      clr = batch.getColor.cpy()
      batch.setColor(clr.r, clr.g, clr.b, opacity.toFloat)
    }

    batch.draw(
      textureRegion,
      x.toFloat,
      y.toFloat,
      0,
      0,
      width,
      height,
      scaleX.toFloat,
      scaleY.toFloat,
      rotation.toFloat
    )

    if (opacity < 1) {
      batch.setColor(clr)
    }
  }
}

class StubRasterPicture(w: Double, h: Double) extends RasterPicture {
  lazy val bPoly = {
    val vertices = Array(0, 0, w.toFloat, 0, w.toFloat, h.toFloat, 0, h.toFloat)
    new Polygon(vertices)
  }

  private[picgaming] def realDraw(batch: SpriteBatch): Unit = {
    // do nothing
  }
}


class BatchPics(pics: collection.Seq[RasterPicture]) extends RasterPicture {
  private var currPicIndex = 0
  private var lastIndexChange = TimeUtils.millis()

  private[picgaming] def realDraw(batch: SpriteBatch): Unit = {
    if (!showing) return
    val currPic = pics(currPicIndex)
    val savedTransform = batch.getTransformMatrix.cpy();
    batch.getTransformMatrix.translate(x.toFloat, y.toFloat, 0f)
    batch.getTransformMatrix.rotate(0, 0, 1, rotation.toFloat)
    batch.getTransformMatrix.scale(scaleX.toFloat, scaleY.toFloat, 1)
    batch.setTransformMatrix(batch.getTransformMatrix)
    currPic.realDraw(batch)
    batch.setTransformMatrix(savedTransform)
  }

  def showNext(gap: Long): Unit = {
    val currTime = TimeUtils.millis()
    if (currTime - lastIndexChange > gap) {
      currPicIndex += 1
      if (currPicIndex == pics.size) {
        currPicIndex = 0
      }
      lastIndexChange = currTime
    }
  }

  def showNext(): Unit = showNext(100)

  def bPoly: Polygon = new Polygon(pics(currPicIndex).boundaryPolygon.getTransformedVertices)
}
