package net.kogics.kojo.picgaming

import java.awt.Color

import com.badlogic.gdx.graphics.{ Color => GdxColor }
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import net.kogics.kojo.gaming.Utils
import net.kogics.kojo.gaming.WorldBounds
import net.kogics.kojo.picgaming.Builtins._

abstract class PicGdxScreen extends Screen with InputProcessor {
  WorldBounds.set(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  Builtins.screen = this
  val stage = new PicGdxStage()
  Picture.stage = stage
  val shapeRenderer = new ShapeRenderer()
  val spriteBatch = new SpriteBatch()
  var paused = false
  var bgColor: GdxColor = GdxColor.WHITE.cpy()
  val camera = new OrthographicCamera(WorldBounds.width, WorldBounds.height)
  camera.position.set(0, 0, 0)

  private var animateLoop: () => Unit = _

  def animate(fn: => Unit): Unit = {
    animateLoop = { () =>
      fn
    }
  }

  def timer(interval: Long)(fn: => Unit): Unit = {
    Timer.schedule(() => fn, 0, interval / 1000f)
  }

  def update(dt: Float): Unit = {
    if (animateLoop != null) {
      animateLoop()
    }

    // do processing for value add stuff provide by screen
    // game time on bottom left
    gameTimeInfo match {
      case Some(ginfo) =>
        gameTime += dt
        if (gameTime >= ginfo.limit) {
          ginfo.pic.erase()
          drawCenteredMessage(ginfo.endMsg, cm.green, 30)
          stopAnimation()
        }
        else {
          ginfo.pic.setText(gameTimeString)
        }
      case None =>
    }
    // fps on top left
    fpsInfo match {
      case Some(finfo) =>
        val fps = Gdx.graphics.getFramesPerSecond
        finfo.pic.setText(s"Fps: $fps")
      case None =>
    }
  }

  override def render(dt: Float): Unit = {
    if (!paused) {
      camera.update()
      shapeRenderer.setProjectionMatrix(camera.combined)
      spriteBatch.setProjectionMatrix(camera.combined)

      update(dt)

      Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

      // draw the pic scene graph
      shapeRenderer.begin(ShapeType.Filled)
      for (p <- stage.filledPictures) {
        p.realDrawFilled(shapeRenderer)
      }
      shapeRenderer.end()

      shapeRenderer.begin(ShapeType.Line)
      for (p <- stage.outlinedPictures) {
        p.realDrawOutlined(shapeRenderer)
      }
      shapeRenderer.end()

      spriteBatch.begin()
      for (p <- stage.imagePictures) {
        p.realDraw(spriteBatch)
      }
      spriteBatch.end()
    }
  }

  // methods required by Screen interface
  override def resize(width: Int, height: Int): Unit = {}

  override def pause(): Unit = {
    paused = true
  }

  override def resume(): Unit = {
    paused = false
  }

  override def dispose(): Unit = {}

  override def show(): Unit = {
    val im = Gdx.input.getInputProcessor.asInstanceOf[InputMultiplexer]
    im.addProcessor(this)
  }

  override def hide(): Unit = {
    val im = Gdx.input.getInputProcessor.asInstanceOf[InputMultiplexer]
    im.removeProcessor(this)
  }

  override def keyDown(keycode: Int) = false

  override def keyUp(keycode: Int) = false

  override def keyTyped(c: Char) = false

  override def mouseMoved(screenX: Int, screenY: Int) = false

  def scrolled(amount: Int) = false

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

  override def touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

  override def touchCancelled(x$1: Int, x$2: Int, x$3: Int, x$4: Int): Boolean = false

  override def scrolled(amountX: Float, amountY: Float) = false

  // Kojo API support

  def stopAnimation(): Unit = {
    pause()
  }

  case class GameTimeInfo(pic: TextPicture, limit: Int, endMsg: String)
  case class FpsInfo(pic: TextPicture)

  var gameTimeInfo: Option[GameTimeInfo] = None
  var gameTime = 0f
  def gameTimeString: String = gameTime.toInt.toString
  var fpsInfo: Option[FpsInfo] = None

  def showGameTime(
      limitSecs: Int,
      endMsg: => String,
      color: Color = cm.black,
      fontSize: Int = 15,
      dx: Double = 10,
      dy: Double = 50,
      countDown: Boolean = false
  ): Unit = {
    val pic = Picture.text(gameTimeString, fontSize, color)
    gameTimeInfo = Some(GameTimeInfo(pic, limitSecs, endMsg))
    val cb = canvasBounds
    pic.setPosition(cb.x + dx, cb.y + dy)
    pic.draw()
  }

  def showFps(color: Color = black, fontSize: Int = 15): Unit = {
    val fpsLabel = Picture.text("Fps: ", fontSize, color)
    fpsInfo = Some(FpsInfo(fpsLabel))
    val cb = canvasBounds
    fpsLabel.setPosition(cb.x + 10, cb.y + cb.height - 10)
    fpsLabel.draw()
  }

  def scroll(x: Double, y: Double): Unit = {
    camera.translate(x.toFloat, y.toFloat)
  }

  def canvasBounds = {
    val w = camera.viewportWidth * camera.zoom
    val h = camera.viewportHeight * camera.zoom
    val p = camera.position
    Bounds(p.x - w / 2, p.y - h / 2, w, h)
  }

  def setBackground(c: Color): Unit = {
    Utils.setGdxColorFromAwtColor(bgColor, c)
  }

  def originBottomLeft(): Unit = {
    val cb = canvasBounds
    camera.position.set(-cb.x.toFloat, -cb.y.toFloat, 0)
  }

  def erasePictures(): Unit = {
    stage.clear()
  }

  def drawCentered(pic: Picture): Unit = {
    val cb = canvasBounds; val pb = pic.boundaryPolygon.getBoundingRectangle
    val xDelta = cb.x - pb.x + (cb.width - pb.width) / 2
    val yDelta = cb.y - pb.y + (cb.height - pb.height) / 2
    pic.setPosition(xDelta, yDelta)
    pic.draw()
  }

  def drawCenteredMessage(message: String, color: Color = cm.black, fontSize: Int = 15): Unit = {
    val pic = Picture.text(message, fontSize, color)
    drawCentered(pic)
  }

}
