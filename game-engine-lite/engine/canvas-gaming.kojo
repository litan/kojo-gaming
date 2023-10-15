// A simple game engine that uses the Kojo Drawing Canvas
// This is meant to be very similar to the Kojo game engine for libGDX
// The idea with this is to provide
// a smooth path from gaming within Kojo to libGDX based gaming

case class Point(x: Float, y: Float)
case class Rectangle(x: Float, y: Float, width: Float, height: Float) {
    def overlaps(r: Rectangle): Boolean = {
        x < r.x + r.width && x + width > r.x &&
            y < r.y + r.height && y + height > r.y
    }
}

// This is similar to the libGDX Vector2 class, for easy migration
// to libGDX from the drawing canvas.
class Vector2(x0: Float, y0: Float) {
    var x = x0
    var y = y0
    def set(nx: Float, ny: Float): Unit = {
        x = nx
        y = ny
    }
    def scl(scalar: Float): Unit = {
        x *= scalar
        y *= scalar
    }
    def len = math.sqrt(x * x + y * y).toFloat
    def len2 = x * x + y * y
    def setLength(length: Float): Unit = {
        setLength2(length * length)
    }
    def setLength2(length2: Float): Unit = {
        val oldLen2 = len2
        if (oldLen2 == 0 || oldLen2 == length2) {
            return
        }
        scl(math.sqrt(length2 / oldLen2).toFloat)
    }
    def rotateRad(radians: Float): Unit = {
        val cos = Math.cos(radians).toFloat
        val sin = Math.sin(radians).toFloat
        val newX = x * cos - y * sin
        val newY = x * sin + y * cos
        x = newX
        y = newY
    }
    def setAngleRad(radians: Float): Unit = {
        set(len, 0f);
        rotateRad(radians)
    }

    def setAngleDeg(degrees: Float): Unit = {
        setAngleRad(degrees.toRadians)
    }

    def angleDeg(): Float = {
        var angle = math.atan2(y, x).toDegrees.toFloat
        if (angle < 0) angle += 360f;
        angle
    }

    def add(v: Vector2): Unit = {
        x += v.x
        y += v.y
    }
    def add(dx: Float, dy: Float): Unit = {
        x += dx
        y += dy
    }
    override def toString = s"Vector2($x, $y)"
}

object GameUtils {
    var t0 = epochTime
    def deltaTime = {
        val t1 = epochTime
        val delta = t1 - t0
        t0 = t1
        delta.toFloat
    }
}

// the screen in the game
trait Screen {
    def show()
    def render(canvas: CanvasDraw, delta: Float)
    def hide()
}

// the game
class Game {
    private var screen: Screen = _
    def render(canvas: CanvasDraw): Unit = {
        if (screen != null) {
            screen.render(canvas, GameUtils.deltaTime)
        }
    }

    def setScreen(newScreen: Screen): Unit = {
        if (screen != null) {
            screen.hide()
        }
        screen = newScreen
        if (screen != null) {
            screen.show()
        }
    }
}

abstract class StageScreen extends Screen {
    val stage = new Stage()
    def update(dt: Float): Unit
    var active = true

    def show() {
        activateCanvas()
    }
    def hide() {
        active = false
    }
    def render(canvas: CanvasDraw, dt: Float) {
        stage.act(dt)
        update(dt)
        canvas.background(black)
        if (active) {
            stage.draw(canvas)
        }
    }
}

abstract class PicScreen extends Screen {
    def pic: Picture
    def show() {
        drawCentered(pic)
    }
    def hide() {
        pic.erase
    }
}

// the stage where all the game objects live
// meant to be inside a screen
class Stage {
    val root = new GameEntity(0, 0) {
        val renderer = new NoOpRenderer(this)
        def update(dt: Float): Unit = {}
    }

    def addEntity(ge: GameEntity) {
        root.addChild(ge)
    }

    def removeEntity(ge: GameEntity) {
        root.removeChild(ge)
    }

    def act(dt: Float) {
        root.act(dt)
    }

    def draw(canvas: CanvasDraw) {
        root.draw(canvas)
    }
}

trait Behavior {
    def timeStep(dt: Float): Unit
}

trait Renderer extends Behavior {
    def draw(canvas: CanvasDraw): Unit
}

class RectRenderer(ge: GameEntity, w: Float, h: Float) extends Renderer {
    ge.setSize(w, h)
    def timeStep(dt: Float): Unit = {}
    def draw(canvas: CanvasDraw): Unit = {
        import ge._
        canvas.rect(0, 0, getWidth, getHeight)
    }
}

class NoOpRenderer(ge: GameEntity) extends Renderer {
    def timeStep(dt: Float): Unit = {}
    def draw(canvas: CanvasDraw): Unit = {}
}

class EllipseRenderer(ge: GameEntity) extends Renderer {
    def timeStep(dt: Float): Unit = {}
    def draw(canvas: CanvasDraw): Unit = {
        import ge._
        canvas.ellipse(getX + getWidth / 2, getY + getHeight / 2, getWidth, getHeight)
    }
}

// game object or entity or node
abstract class GameEntity(x0: Float, y0: Float) {
    private var x = x0
    private var y = y0
    private var width = 0.0f
    private var height = 0.0f
    private val children = ArrayBuffer.empty[GameEntity]
    private val parent: GameEntity = null
    private var visible = true
    private var color = noColor
    private var components = new HashMap[String, Any]
    def addComponent(c: AnyRef): Unit = {
        val cls = c.getClass
        components.put(cls.getName, c)
    }

    def getComponent[T](cls: Class[T]): T = {
        components(cls.getName).asInstanceOf[T]
    }

    protected def renderer: Renderer

    def position: Point = Point(x, y)
    def setPosition(nx: Float, ny: Float): Unit = {
        x = nx
        y = ny
    }
    def getX: Float = x
    def getY: Float = y
    def getWidth: Float = width
    def getHeight: Float = height

    def setX(nx: Float): Unit = {
        x = nx
    }
    def setY(ny: Float): Unit = {
        y = ny
    }
    def setSize(w: Float, h: Float): Unit = {
        width = w
        height = h
    }

    def act(dt: Float): Unit = {
        children.foreach { child =>
            child.act(dt)
        }
        update(dt)
    }

    def update(dt: Float): Unit

    def draw(canvas: CanvasDraw): Unit = {
        if (visible) {
            canvas.pushMatrix()
            canvas.translate(x, y)
            canvas.fill(color)
            canvas.stroke(color)
            renderer.draw(canvas)
            children.foreach { child =>
                child.draw(canvas)
            }
            canvas.popMatrix()
        }
    }

    def moveBy(dx: Float, dy: Float): Unit = {
        x += dx
        y += dy
    }
    def moveBy(vec: Vector2): Unit = {
        moveBy(vec.x, vec.y)
    }
    def addChild(c: GameEntity): Unit = {
        if (c.parent != null) {
            parent.removeChild(c)
        }
        children.append(c)
    }
    def removeChild(c: GameEntity): Unit = {
        children -= c
    }
    def isVisible = visible
    def setVisible(v: Boolean): Unit = {
        visible = v
    }
    def getColor = color
    def setColor(c: Color): Unit = {
        color = c
    }
}

class PhysicsBehavior(ge: GameEntity) extends Behavior {
    ge.addComponent(this)
    private val currVelocity = new Vector2(0, 0)
    private val accumulatedAcceleration = new Vector2(0, 0)
    private var maxSpeed = 1000f
    private var frictionMagnitude = 0f

    def gameEntity = ge
    def setFrictionMagnitude(dec: Float): Unit = {
        frictionMagnitude = dec
    }

    def setMaxSpeed(ms: Float): Unit = {
        maxSpeed = ms
    }

    def speed: Float = currVelocity.len

    def isMoving: Boolean = speed > 0

    def velocity: Vector2 = currVelocity

    def setVelocity(vx: Float, vy: Float): Unit = {
        currVelocity.set(vx, vy)
    }

    def setVelocityMagnitude(speed: Float): Unit = {
        if (currVelocity.len == 0) currVelocity.set(speed, 0)
        else currVelocity.setLength(speed)
    }

    def setVelocityDirection(angle: Float): Unit = {
        currVelocity.setAngleDeg(angle)
    }

    def velocityDirection: Float = currVelocity.angleDeg

    def addVelocity(vel: Vector2): Unit = {
        currVelocity.add(vel.x, vel.y)
    }

    def addAcceleration(acc: Vector2): Unit = {
        accumulatedAcceleration.add(acc)
    }

    def addAcceleration(magnitude: Float, angle: Float): Unit = {
        val acc = new Vector2(magnitude, 0)
        acc.setAngleDeg(angle)
        accumulatedAcceleration.add(acc)
    }

    def addAcceleration(magnitude: Float): Unit = {
        addAcceleration(magnitude, 0)
    }

    def timeStep(dt: Float): Unit = {
        currVelocity.add(accumulatedAcceleration.x * dt, accumulatedAcceleration.y * dt)
        var currSpeed = currVelocity.len
        if (accumulatedAcceleration.len == 0) {
            currSpeed -= frictionMagnitude * dt
        }
        currSpeed = mathx.constrain(currSpeed, 0, maxSpeed).toFloat
        setVelocityMagnitude(currSpeed)
        ge.moveBy(currVelocity.x * dt, currVelocity.y * dt)
        accumulatedAcceleration.set(0, 0)
    }

    def addGravityAcceleration(g: Float): Unit = {
        val ga = new Vector2(g, 0)
        ga.setAngleDeg(270)
        accumulatedAcceleration.add(ga)
    }
}

class Collider(ge: GameEntity) {
    ge.addComponent(this)

    def boundaryRectangle = {
        Rectangle(ge.getX, ge.getY, ge.getWidth, ge.getHeight)
    }
    def collidesWith(other: Collider): Boolean = {
        if (boundaryRectangle.overlaps(other.boundaryRectangle)) true else false
    }
}

class PositioningCapability(ge: GameEntity) {
    def centerAtPosition(x: Float, y: Float): Unit = {
        ge.setPosition(x - ge.getWidth / 2, y - ge.getHeight / 2)
    }

    def centerAtActor(other: GameEntity): Unit = {
        centerAtPosition(other.getX + other.getWidth / 2, other.getY + other.getHeight / 2)
    }
}

object WorldBounds {
    var boundsRect: Rectangle = _
    def set(width: Float, height: Float): Unit = {
        boundsRect = new Rectangle(0, 0, width, height)
    }

    def set(width: Int, height: Int): Unit = {
        boundsRect = new Rectangle(0, 0, width.toFloat, height.toFloat)
    }

    def set(ge: GameEntity): Unit = {
        set(ge.getWidth, ge.getHeight)
    }

    def width: Float = boundsRect.width
    def height: Float = boundsRect.height
}

class WorldBoundsCapability(ge: GameEntity) {
    ge.addComponent(this)
    val pc = ge.getComponent(classOf[PhysicsBehavior])
    import WorldBounds.boundsRect

    def wrapAround(): Unit = {
        if (ge.getX + ge.getWidth < 0) {
            ge.setX(boundsRect.width)
        }
        if (ge.getX > boundsRect.width) {
            ge.setX(-ge.getWidth)
        }
        if (ge.getY + ge.getHeight < 0) {
            ge.setY(boundsRect.height)
        }
        if (ge.getY > boundsRect.height) {
            ge.setY(-ge.getHeight)
        }
    }

    def keepWithin(): Unit = {
        import ge._
        if (getX < 0) setX(0)
        if (getX + getWidth > boundsRect.width) setX(boundsRect.width - getWidth)
        if (getY < 0) setY(0)
        if (getY + getHeight > boundsRect.height) setY(boundsRect.height - getHeight)
    }

    def bounceOff(): Unit = {
        import ge._

        def flipXVel(): Unit = {
            val vel = pc.velocity
            pc.setVelocity(-vel.x, vel.y)
        }

        def flipYVel(): Unit = {
            val vel = pc.velocity
            pc.setVelocity(vel.x, -vel.y)
        }

        if (getX < 0) {
            setX(0)
            flipXVel()
        }

        if (getX + getWidth > boundsRect.width) {
            setX(boundsRect.width - getWidth)
            flipXVel()
        }
        if (getY < 0) {
            setY(0)
            flipYVel()
        }
        if (getY + getHeight > boundsRect.height) {
            setY(boundsRect.height - getHeight)
            flipYVel()
        }
    }

    def isOutside: Boolean = {
        import ge._
        (getX + getWidth < 0) ||
            (getX > boundsRect.width) ||
            (getY + getHeight < 0) ||
            (getY > boundsRect.height)
    }
}
