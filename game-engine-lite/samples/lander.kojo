// #include ../engine/canvas-gaming.kojo

cleari()
clearOutput()
originBottomLeft()
WorldBounds.set(cwidth, cheight)

class SpaceShip(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    val renderer = new RectRenderer(this, 40, 60)
    private val collider = new Collider(this)
    setColor(cm.red)
    private val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(500)
    physics.setFrictionMagnitude(10)

    val thruster = new Thruster()
    addChild(thruster)

    def update(dt: Float) {
        if (isKeyPressed(Kc.VK_UP)) {
            physics.addAcceleration(300, 90)
            thruster.setVisible(true)
        }
        else {
            thruster.setVisible(false)
        }
        physics.addGravityAcceleration(100)
        physics.timeStep(dt)
    }
}

class Thruster extends GameEntity(10, -30) {
    val renderer = new RectRenderer(this, 20, 30)
    setColor(ColorMaker.hsl(45, 1.00, 0.50))
    def update(dt: Float): Unit = {}
}

class Moon(x: Float, y: Float, bg: GameEntity) extends GameEntity(x, y) {
    val renderer = new RectRenderer(this, 300, 50)
    setColor(cm.lightBlue)
    private val collider = new Collider(this)
    private val positioner = new PositioningCapability(this)
    positioner.centerAtActor(bg)
    moveBy(0, -(bg.getHeight / 2 - 0))
    def update(dt: Float): Unit = {}
}

class Meteor(x: Float, y: Float) extends GameEntity(x, y) {
    val renderer = new RectRenderer(this, 20, 20)
    setColor(ColorMaker.hsl(131, 0.6, 0.5))
    private val physics = new PhysicsBehavior(this)
    physics.setVelocity(150, random(10))
    private val wb = new WorldBoundsCapability(this)
    private val collider = new Collider(this)

    def update(dt: Float) {
        physics.timeStep(dt)
        wb.bounceOff()
    }
}

class LanderScreen extends StageScreen {
    val space = new GameEntity(0, 0) {
        val renderer = new RectRenderer(this, cwidth, cheight)
        setColor(ColorMaker.hsl(300, 1.00, 0.07))
        def update(dt: Float): Unit = {}
    }
    stage.addEntity(space)

    val spaceShip = new SpaceShip(cwidth / 2 - 25, cheight / 2 - 25)
    stage.addEntity(spaceShip)

    val moon = new Moon(0, 0, space)
    stage.addEntity(moon)

    val meteors = for (n <- 1 to 5) yield {
        val meteor = new Meteor(random(cwidth), random(200))
        stage.addEntity(meteor)
        meteor
    }

    val shipCollider = spaceShip.getComponent(classOf[Collider])
    val moonCollider = moon.getComponent(classOf[Collider])
    val meteorColliders = for (meteor <- meteors) yield {
        meteor.getComponent(classOf[Collider])
    }
    val shipPhysics = spaceShip.getComponent(classOf[PhysicsBehavior])

    def update(dt: Float) {
        for (meteorCollider <- meteorColliders) {
            if (shipCollider.collidesWith(meteorCollider)) {
                game.setScreen(new DoneScreen("You Lose"))
            }
        }

        if (shipCollider.collidesWith(moonCollider)) {
            val msg = if (shipPhysics.speed > 60)
                "You Lose" else "You Win"
            game.setScreen(new DoneScreen(msg))
        }
    }

}

class StartScreen extends PicScreen {
    val btn = Button("Click to Start") {
        game.setScreen(new LanderScreen())
    }
    val pic = Picture.widget(btn)
    def render(canvas: CanvasDraw, dt: Float) {}
}

class DoneScreen(msg: String) extends PicScreen {
    val pic1 = Picture.text(msg, 30).withPenColor(white)
    val btn = Button("Play Again") {
        game.setScreen(new LanderScreen())
    }
    val pic = picColCentered(
        Picture.widget(btn),
        Picture.vgap(20),
        pic1
    )
    def render(canvas: CanvasDraw, dt: Float) {}
}

val game = new Game()

def setup(c: CanvasDraw) {
    c.background(black)
    game.setScreen(new StartScreen)
}

animateWithSetupCanvasDraw(setup) { canvas =>
    game.render(canvas)
}
