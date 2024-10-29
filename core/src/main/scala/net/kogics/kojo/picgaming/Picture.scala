package net.kogics.kojo.picgaming

import java.awt.image.BufferedImage

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.utils.FloatArray
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.Gdx
import net.kogics.kojo.core.Point
import net.kogics.kojo.gaming.TextureUtils
import net.kogics.kojo.util.Vector2D

object Picture {
  var stage: PicGdxStage = _
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
  def image(fileName: String) = {
    val textureRegion = TextureUtils.loadTexture(fileName)
    new ImagePicture(textureRegion)
  }
  def image(img: BufferedImage) = {
    val texture = ImageConverter.bufferedImageToTexture(img)
    texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
    val textureRegion = new TextureRegion(texture)
    new ImagePicture(textureRegion)
  }
  def batch(pics: collection.Seq[RasterPicture]): RasterPicture = new BatchPics(pics)
}

trait PictureShowHide {
  var showing = true

  def visible(): Unit = {
    showing = true
  }

  def invisible(): Unit = {
    showing = false
  }
}

trait Picture extends PictureShowHide {
  protected var x = 0.0
  protected var y = 0.0
  protected var rotation = 0.0 // radians
  protected var scaleX = 1.0
  protected var scaleY = 1.0
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

  def rotate(a: Double): Unit = {
    rotation += a
  }

  def scale(factor: Double): Unit = {
    val sf = if (factor == 0) Double.MinPositiveValue else factor
    scaleX *= sf
    scaleY *= sf
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

  def setOpacity(f: Double): Unit = {
    // todo
  }
}

trait RasterPicture extends Picture {
  private[picgaming] def realDraw(batch: SpriteBatch): Unit
}

trait VectorPicture extends Picture {
  protected var penColor: Color = _
  protected var fillColor: Color = _
  private[picgaming] def realDrawOutlined(shapeRenderer: ShapeRenderer): Unit
  private[picgaming] def realDrawFilled(shapeRenderer: ShapeRenderer): Unit
  private[picgaming] def hasFill: Boolean = fillColor != null
  private[picgaming] def hasPen: Boolean = penColor != null
  setPenColor(java.awt.Color.RED)

  def setPenColor(c: java.awt.Color): Unit = {
    if (c == null) {
      penColor = null
    }
    else {
      penColor = if (penColor == null) new Color() else penColor
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
  private[picgaming] def realDrawOutlined(shapeRenderer: ShapeRenderer): Unit = {
    if (!showing) return
    shapeRenderer.setColor(penColor)
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
  private[picgaming] def realDrawFilled(shapeRenderer: ShapeRenderer): Unit = {
    if (!showing) return
    shapeRenderer.setColor(fillColor)
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

  val bPoly = {
    val w = width
    val h = height
    val vertices = Array(0f, 0f, w, 0, w, h, 0, h)
    new Polygon(vertices)
  }

  def setText(m: String): Unit = {
    msg = m
  }

  private[picgaming] def realDraw(batch: SpriteBatch): Unit = {
    if (!showing) return
    font.draw(batch, msg, x.toFloat, y.toFloat)
  }
}

class ImagePicture(textureRegion: TextureRegion) extends RasterPicture {
  val width = textureRegion.getRegionWidth.toFloat
  val height = textureRegion.getRegionHeight.toFloat

  val bPoly = {
    val w = width
    val h = height
    val vertices = Array(0f, 0f, w, 0, w, h, 0, h)
    new Polygon(vertices)
  }

  private[picgaming] def realDraw(batch: SpriteBatch): Unit = {
    if (!showing) return
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

  def bPoly: Polygon = new Polygon(pics(currPicIndex).boundaryPolygon.getTransformedVertices)
}
