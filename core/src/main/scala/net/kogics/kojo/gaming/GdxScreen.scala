package net.kogics.kojo.gaming

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen

abstract class GdxScreen extends Screen with InputProcessor {
  val entityStage = new Stage()
  private val uiStage = new Stage()
  val uiTable = new Table()
  uiTable.setFillParent(true)
  uiStage.addActor(uiTable)

  def update(dt: Float): Unit

  override def render(dt: Float): Unit = {
    uiStage.act(dt)
    entityStage.act(dt)

    update(dt)

    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    entityStage.draw()
    uiStage.draw()
  }

  // methods required by Screen interface
  override def resize(width: Int, height: Int): Unit = {}

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def dispose(): Unit = {}

  override def show(): Unit = {
    val im = Gdx.input.getInputProcessor.asInstanceOf[InputMultiplexer]
    im.addProcessor(this)
    im.addProcessor(uiStage)
    im.addProcessor(entityStage)
  }

  override def hide(): Unit = {
    val im = Gdx.input.getInputProcessor.asInstanceOf[InputMultiplexer]
    im.removeProcessor(this)
    im.removeProcessor(uiStage)
    im.removeProcessor(entityStage)
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
