// #include ../engine/canvas-gaming.kojo

cleari()
clearOutput()
originBottomLeft()
WorldBounds.set(cwidth, cheight)

class PlayerCar(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    def vec(x: Float, y: Float) = new Vector2(x, y)

    val renderer = new RectRenderer(this, 40, 60)
    setColor(cm.lightBlue)
    val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(300)
    //    physics.setFrictionMagnitude(100)

    val wb = new WorldBoundsCapability(this)
    val collider = new Collider(this)
    val speed = 3800

    def update(dt: Float) {
        val nv = physics.velocity
        nv.scl(0.9f)
        physics.setVelocity(nv.x, nv.y)

        if (isKeyPressed(Kc.VK_UP)) {
            physics.addVelocity(vec(0, speed * dt))
        }
        if (isKeyPressed(Kc.VK_DOWN)) {
            physics.addVelocity(vec(0, -speed * dt))
        }
        if (isKeyPressed(Kc.VK_LEFT)) {
            physics.addVelocity(vec(-speed * dt, 0))
        }
        if (isKeyPressed(Kc.VK_RIGHT)) {
            physics.addVelocity(vec(speed * dt, 0))
        }
        physics.timeStep(dt)
        wb.wrapAround()
    }
}

class OtherCar(x0: Float, y0: Float) extends GameEntity(x0, y0) {
    val renderer = new RectRenderer(this, 40, 70)
    setColor(cm.tomato)
    val physics = new PhysicsBehavior(this)
    physics.setMaxSpeed(500)
    physics.setVelocity(0, -300)

    val wb = new WorldBoundsCapability(this)
    val collider = new Collider(this)

    def update(dt: Float) {
        physics.timeStep(dt)
        //        wb.wrapAround()
    }

    def isOutsideWorld = wb.isOutside
}

class GameScreen extends StageScreen {
    val carColliders = HashSet.empty[(OtherCar, Collider)]

    def spawnCar() {
        val car = new OtherCar(player.getX + randomDouble(-20, 20).toFloat, cheight)
        stage.addEntity(car)
        val oc = car.getComponent(classOf[Collider])
        carColliders.add(car, oc)
    }

    val player = new PlayerCar(cwidth / 2 - 25, cheight / 2 - 25)
    stage.addEntity(player)

    val pc = player.getComponent(classOf[Collider])
    spawnCar()

    var spawnDelta = 0.0

    def update(dt: Float) {
        spawnDelta += dt
        if (spawnDelta > 0.7) {
            spawnDelta = 0
            spawnCar()
        }
        for ((car, oc) <- carColliders) {
            if (pc.collidesWith(oc)) {
                stopAnimation()
            }
            if (car.isOutsideWorld) {
                carColliders.remove(car, oc)
                stage.removeEntity(car)
            }
        }
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
