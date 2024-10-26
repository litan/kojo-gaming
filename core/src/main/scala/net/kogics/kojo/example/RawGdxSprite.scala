package net.kogics.kojo.example

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import net.kogics.kojo.gaming.TextureUtils
import net.kogics.kojo.gaming.WorldBounds

class RawGdxSprite extends ApplicationAdapter {
  var batch: SpriteBatch = _
  var textureRegion: TextureRegion = _
  var width = 0f
  var height = 0f

  override def create(): Unit = {
    batch = new SpriteBatch()
    WorldBounds.set(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    val camera = new OrthographicCamera(WorldBounds.width, WorldBounds.height)
    camera.setToOrtho(false, WorldBounds.width, WorldBounds.height)
    camera.position.set(0, 0, 0)
    camera.update()
    batch.setProjectionMatrix(camera.combined)

    textureRegion = TextureUtils.loadTexture("blue-sq.png")
    width = textureRegion.getRegionWidth.toFloat
    height = textureRegion.getRegionHeight.toFloat
  }

  override def render(): Unit = {
    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    batch.begin()
    val savedTransform = batch.getTransformMatrix.cpy();
    batch.getTransformMatrix.translate(0, -300, 0)
    batch.setTransformMatrix(batch.getTransformMatrix)
    batch.draw(textureRegion, 0, 0, width, height)
    batch.setTransformMatrix(savedTransform)
    batch.end()
  }
}
