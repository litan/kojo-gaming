package net.kogics.kojo.example

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import net.kogics.kojo.gaming.WorldBounds

class RawGdxShape extends ApplicationAdapter {
  var shapeRenderer: ShapeRenderer = _

  override def create(): Unit = {
    shapeRenderer = new ShapeRenderer()
    WorldBounds.set(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    val camera = new OrthographicCamera(WorldBounds.width, WorldBounds.height)
    camera.position.set(0, 0, 0)
    camera.update()
    shapeRenderer.setProjectionMatrix(camera.combined)
  }

  override def render(): Unit = {
    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)



    shapeRenderer.begin(ShapeType.Filled)
    val savedTransform = shapeRenderer.getTransformMatrix.cpy();
    shapeRenderer.translate(0, -300, 0)
    shapeRenderer.rect(0, 0, 100, 100)
    shapeRenderer.setTransformMatrix(savedTransform)
    shapeRenderer.end()
  }
}
