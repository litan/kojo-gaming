package net.kogics.kojo.example

import java.awt.image.BufferedImage

import net.kogics.kojo.core.Point
import net.kogics.kojo.gaming.GdxGame
import net.kogics.kojo.gaming.KojoUtils._
import net.kogics.kojo.picgaming.tiles.SpriteSheet
import net.kogics.kojo.picgaming.tiles.TileWorld
import net.kogics.kojo.picgaming.tiles.TileXY
import net.kogics.kojo.picgaming.BatchPics
import net.kogics.kojo.picgaming.Builtins._
import net.kogics.kojo.picgaming.PicGdxScreen
import net.kogics.kojo.picgaming.Picture

class Platformer extends GdxGame {
  override def create(): Unit = {
    super.create()
    setScreen(new PlatformerScreen())
  }
}

class PlatformerScreen extends PicGdxScreen {
  cleari()
  val cb = canvasBounds
  scroll(-cb.x, cb.y)
  // Tiled map layer of tiles that you collide with
  val collisionLayer = 1

  class Player(tx: Int, ty: Int, world: TileWorld) {
    val playerPos = world.tileToKojo(TileXY(tx, ty))
    val sheet = SpriteSheet("player.png", 30, 42)

    // player images are 30x40
    // scale the player down to fit into a 24 pixel wide tile
    def playerPicture(img: BufferedImage) = {
      val pic = Picture.image(img)
      pic.scale(0.8)
//      pic.setOpacity(0.1)
      pic
    }

    val stillRight = picBatch(playerPicture(sheet.imageAt(0, 0)))
    val stillLeft = picBatch(playerPicture(sheet.imageAt(0, 1)))

    val runningRight = picBatch(
      List(
        sheet.imageAt(0, 2),
        sheet.imageAt(1, 2),
        sheet.imageAt(2, 2),
        sheet.imageAt(3, 2),
        sheet.imageAt(4, 2)
      ).map(playerPicture)
    )

    val runningLeft = picBatch(
      List(
        sheet.imageAt(0, 3),
        sheet.imageAt(1, 3),
        sheet.imageAt(2, 3),
        sheet.imageAt(3, 3),
        sheet.imageAt(4, 3)
      ).map(playerPicture)
    )

    val jumpingRight = picBatch(
      List(
        sheet.imageAt(0, 0),
        sheet.imageAt(1, 0),
        sheet.imageAt(2, 0),
        sheet.imageAt(3, 0)
      ).map(playerPicture)
    )

    val jumpingLeft = picBatch(
      List(
        sheet.imageAt(0, 1),
        sheet.imageAt(1, 1),
        sheet.imageAt(2, 1),
        sheet.imageAt(3, 1)
      ).map(playerPicture)
    )

    var currentPic = stillRight
    currentPic.setPosition(playerPos)

    var facingRight = true
    val gravity = -0.1
    val speedX = 3.0
    var speedY = -1.0
    var inJump = false

    def step(): Unit = {
      stepCollisions()
      stepFood()
    }

    var goalEnabled = false
    def stepFood(): Unit = {
      if (currentPic.collidesWith(halfwayGoal)) {
        halfwayGoal.erase()
        goal.setOpacity(1)
        goalEnabled = true
      }
      if (goalEnabled) {
        if (currentPic.collidesWith(goal)) {
          goal.erase()
          stopAnimation()
          drawCenteredMessage("You Win!", cm.white, 30)
        }
      }
    }

    def stepCollisions(): Unit = {
      if (isKeyPressed(Kc.VK_RIGHT)) {
        facingRight = true
        updateImage(runningRight)
        currentPic.translate(speedX, 0)
        if (world.hasTileAtRight(currentPic, collisionLayer)) {
          world.moveToTileLeft(currentPic)
        }
      }
      else if (isKeyPressed(Kc.VK_LEFT)) {
        facingRight = false
        updateImage(runningLeft)
        currentPic.translate(-speedX, 0)
        if (world.hasTileAtLeft(currentPic, collisionLayer)) {
          world.moveToTileRight(currentPic)
        }
      }
      else {
        if (facingRight) {
          updateImage(stillRight)
        }
        else {
          updateImage(stillLeft)
        }
      }

      if (isKeyPressed(Kc.VK_UP)) {
        if (!inJump) {
          speedY = 5
        }
      }

      speedY += gravity
      speedY = math.max(speedY, -10)
      currentPic.translate(0, speedY)

      if (world.hasTileBelow(currentPic, collisionLayer)) {
        inJump = false
        world.moveToTileAbove(currentPic)
        speedY = 0
      }
      else {
        inJump = true
        if (world.hasTileAbove(currentPic, collisionLayer)) {
          world.moveToTileBelow(currentPic)
          speedY = -1
        }
      }

      if (inJump) {
        if (facingRight) {
          updateImage(jumpingRight)
        }
        else {
          updateImage(jumpingLeft)
        }
        currentPic.showNext(200)
      }
      else {
        currentPic.showNext(200)
      }
      scrollIfNeeded()
    }

    var cb = canvasBounds
    def scrollIfNeeded(): Unit = {
      val threshold = 200
      val pos = currentPic.position
      if (cb.x + cb.width - pos.x < threshold) {
        scroll(speedX, 0)
        cb = canvasBounds
      }
      else if (pos.x - cb.x < threshold) {
        scroll(-speedX, 0)
        cb = canvasBounds
      }
    }

    def updateImage(newPic: BatchPics): Unit = {
      if (newPic != currentPic) {
        currentPic.invisible()
        newPic.visible()
        newPic.setPosition(currentPic.position)
        currentPic = newPic
      }
    }

    def draw(): Unit = {
      stillLeft.drawAndHide()
      runningLeft.drawAndHide()
      runningRight.drawAndHide()
      jumpingLeft.drawAndHide()
      jumpingRight.drawAndHide()
      currentPic.draw()
    }
  }

  class AttackerUpDown(tx: Int, ty: Int, world: TileWorld) {
    val playerPos = world.tileToKojo(TileXY(tx, ty))
    val sheet = SpriteSheet("tiles.png", 24, 24)
    // make attacker slighty smaller than a tile - to prevent picture based collision
    // with the player in an adjacent tile
    def attackerPicture(img: BufferedImage) = {
      val pic = Picture.image(img)
      // scale(0.98) * trans(0.2, 0.2) ->
      pic.scale(0.98)
      pic.translate(0.2, 0.2)
      pic
    }

    var currentPic = picBatch(
      List(
        sheet.imageAt(0, 6),
        sheet.imageAt(1, 6)
      ).map(attackerPicture)
    )

    currentPic.setPosition(playerPos)

    val gravity = -0.03
    //    var speedX = 0.0
    var speedY = -2.0

    def step(): Unit = {
      speedY += gravity
      speedY = math.max(speedY, -10)
      currentPic.translate(0, speedY)
      currentPic.showNext(200)
      if (world.hasTileBelow(currentPic, collisionLayer)) {
        world.moveToTileAbove(currentPic)
        speedY = 5
      }
      else if (world.hasTileAbove(currentPic, collisionLayer)) {
        world.moveToTileBelow(currentPic)
        speedY = -2
      }
    }

    def updateImage(newPic: BatchPics): Unit = {
      if (newPic != currentPic) {
        currentPic.invisible()
        newPic.visible()
        newPic.setPosition(currentPic.position)
        currentPic = newPic
      }
    }

    def draw(): Unit = {
      currentPic.draw()
    }
  }

  val tileWorld =
    new TileWorld("level1.tmx")

  // Create a player object and set the level it is in
  val player = new Player(9, 5, tileWorld)
  val attackers = List(
    new AttackerUpDown(14, 2, tileWorld),
    new AttackerUpDown(17, 3, tileWorld),
    new AttackerUpDown(22, 9, tileWorld),
    new AttackerUpDown(32, 2, tileWorld),
    new AttackerUpDown(35, 3, tileWorld)
  )

  tileWorld.draw()
  player.draw()
  attackers.foreach { attacker =>
    attacker.draw()
  }

  val goal = Picture.rectangle(10, 10)
  goal.setPosition(tileWorld.tileToKojo(TileXY(9, 2)) + Point(12, 12))
  goal.setFillColor(cm.greenYellow)
  goal.setPenColor(cm.black)
  goal.setOpacity(0.2)
  goal.draw()

  val halfwayGoal = Picture.rectangle(10, 10)
  halfwayGoal.setPosition(tileWorld.tileToKojo(TileXY(41, 15)) + Point(12, 12))
  halfwayGoal.setFillColor(cm.red)
  halfwayGoal.setPenColor(cm.black)
  halfwayGoal.draw()

  animate {
    tileWorld.step()
    player.step()
    attackers.foreach { attacker =>
      attacker.step()
      if (player.currentPic.collidesWith(attacker.currentPic)) {
        player.currentPic.rotate(30)
        stopAnimation()
      }
    }
  }

  activateCanvas()

  // game resources sourced from: https://github.com/pricheal/pygame-tiled-demo
}
