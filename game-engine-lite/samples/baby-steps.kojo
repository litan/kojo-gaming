// #include ../engine/canvas-gaming.kojo

cleari()
clearOutput()
originBottomLeft()
WorldBounds.set(cwidth, cheight)

class Player(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    val renderer = new RectRenderer(this, 50, 50)
    setColor(cm.lightBlue)
    val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(500)
    physics.setFrictionMagnitude(10)
    physics.setVelocity(50, 20)

    val wb = new WorldBoundsCapability(this)

    override def update(dt: Float) {
        super.update(dt)
        if (isKeyPressed(Kc.VK_SPACE)) {
            physics.addAcceleration(200, 30)
        }
        physics.timeStep(dt)
        wb.bounceOff()
    }
}

class GameScreen extends StageScreen {
    val player = new Player(cwidth / 2 - 25, cheight / 2 - 25)
    stage.addEntity(player)

    override def show() {
        activateCanvas()
    }

    def update(dt: Float) {
    }
}

val game = new Game()

def setup(c: CanvasDraw) {
    c.stroke(black)
    c.fill(black)
    c.rect(0, 0, cwidth, cheight)
    game.setScreen(new GameScreen)
}

animateWithSetupCanvasDraw(setup) { canvas =>
    game.render(canvas)
}
