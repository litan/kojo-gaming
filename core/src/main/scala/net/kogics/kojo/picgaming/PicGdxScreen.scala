package net.kogics.kojo.picgaming

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.kogics.kojo.gaming.WorldBounds

object PicGdxScreen {
  var stage: PicGdxStage = _

}

abstract class PicGdxScreen extends Screen with InputProcessor {
  PicGdxScreen.stage = new PicGdxStage()
  val shapeRenderer = new ShapeRenderer()
  val spriteBatch = new SpriteBatch()
  var paused = false
  WorldBounds.set(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  val camera = new OrthographicCamera(WorldBounds.width, WorldBounds.height)
  camera.position.set(0, 0, 0)
  camera.update()
  shapeRenderer.setProjectionMatrix(camera.combined)
  spriteBatch.setProjectionMatrix(camera.combined)

  def update(dt: Float): Unit

  override def render(dt: Float): Unit = {
    if (!paused) {

      update(dt)

      Gdx.gl.glClearColor(0, 0, 0, 1)
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

      // draw the pic scene graph
      shapeRenderer.begin(ShapeType.Filled)
      for (p <- PicGdxScreen.stage.filledPictures) {
        p.realDrawFilled(shapeRenderer)
      }
      shapeRenderer.end()

      shapeRenderer.begin(ShapeType.Line)
      for (p <- PicGdxScreen.stage.outlinedPictures) {
        p.realDrawOutlined(shapeRenderer)
      }
      shapeRenderer.end()

      spriteBatch.begin()
      for (p <- PicGdxScreen.stage.imagePictures) {
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

}
