package net.kogics.kojo.gaming

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer

class GdxGame extends Game {
  var largeLabelStyle: Label.LabelStyle = _
  var smallLabelStyle: Label.LabelStyle = _
  var textButtonStyle: TextButton.TextButtonStyle = _

  def create(): Unit = {
    val im = new InputMultiplexer
    Gdx.input.setInputProcessor(im)

    val fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans.ttf"))
    val fontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter

    fontParameters.size = 48
    fontParameters.color = Color.WHITE
    fontParameters.borderWidth = 2
    fontParameters.borderColor = Color.BLACK
    fontParameters.borderStraight = true
    fontParameters.minFilter = Texture.TextureFilter.Linear
    fontParameters.magFilter = Texture.TextureFilter.Linear
    val customLargeFont = fontGenerator.generateFont(fontParameters)

    fontParameters.size = 15
    fontParameters.borderWidth = 0
    val customSmallFont = fontGenerator.generateFont(fontParameters)

    largeLabelStyle = new Label.LabelStyle(customLargeFont, null)
    smallLabelStyle = new Label.LabelStyle(customSmallFont, null)

    val buttonTexture = new Texture(Gdx.files.internal("button.png"))
    val buttonPatch = new NinePatch(buttonTexture, 24, 24, 24, 24)
    textButtonStyle = new TextButton.TextButtonStyle
    textButtonStyle.up = new NinePatchDrawable(buttonPatch)
    textButtonStyle.font = customLargeFont
    textButtonStyle.fontColor = Color.GRAY
  }
}
