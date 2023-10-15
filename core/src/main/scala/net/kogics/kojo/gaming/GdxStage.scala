package net.kogics.kojo.gaming

import com.badlogic.gdx.scenes.scene2d.Stage

class GdxStage extends Stage {

  def addEntity(ge: GameEntity): Unit = {
    addActor(ge)
  }

  def removeEntity(ge: GameEntity): Unit = {
    ge.remove()
  }
}
