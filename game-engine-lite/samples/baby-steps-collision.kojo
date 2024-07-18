// #include ../engine/collisions.kojo

cleari()
clearOutput()
originBottomLeft()
WorldBounds.set(cwidth, cheight)

val speedup = 3

class Player(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    val renderer = new RectRenderer(this, 50, 50)
    val collider = new GdxCollider(this)
    setColor(cm.lightBlue)
    val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(500)
    physics.setFrictionMagnitude(10)
    physics.setVelocity(50 * speedup, 20 * speedup)

    val wb = new WorldBoundsCapability(this)

    def update(dt: Float) {
        if (isKeyPressed(Kc.VK_SPACE)) {
            physics.addAcceleration(200, 180)
        }
        physics.timeStep(dt)
        wb.bounceOff()
    }
}

class Rock(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    val renderer = new RectRenderer(this, 50, 50)
    val collider = new GdxCollider(this)
    setColor(cm.orange)
    val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(500)
    physics.setFrictionMagnitude(10)
    physics.setVelocity(-50 * speedup, 20 * speedup)

    val wb = new WorldBoundsCapability(this)

    def update(dt: Float) {
        physics.timeStep(dt)
        wb.bounceOff()
    }
}

class GameScreen extends StageScreen {
    val player = new Player(cwidth / 2, cheight / 2 - 50)
    stage.addEntity(player)

    val rock = new Rock(cwidth / 2 - 50, cheight / 2 - 50)
    stage.addEntity(rock)

    def update(dt: Float) {
        val pCollider = player.getComponent(classOf[GdxCollider])
        val rCollider = rock.getComponent(classOf[GdxCollider])
        pCollider.bounceBothOff(rCollider)
    }
}

val game = new Game()

def setup(c: CanvasDraw) {
    c.background(black)
    game.setScreen(new GameScreen)
}

animateWithSetupCanvasDraw(setup) { canvas =>
    game.render(canvas)
}
