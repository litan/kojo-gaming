// #include ../engine/canvas-gaming.kojo

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.{ Vector2 => GdxVector2 }

class GdxCollider(val ge: GameEntity) {
    ge.addComponent(this)

    private var bPoly: Polygon = _
    setBoundaryRectangle()

    def setBoundaryRectangle(): Unit = {
        val w = ge.getWidth
        val h = ge.getHeight
        val vertices = Array(0, 0, w, 0, w, h, 0, h)
        setBoundaryPolygon(vertices)
    }

    def setBoundaryPolygon(numSides: Int): Unit = {
        val w = ge.getWidth
        val h = ge.getHeight
        val vertices = ArrayBuffer.empty[Float]
        val twoPi = 2 * 3.14f
        for (i <- 0 until numSides) {
            val angle = i * twoPi / numSides
            vertices.append(w / 2 * MathUtils.cos(angle) + w / 2)
            vertices.append(h / 2 * MathUtils.sin(angle) + h / 2)
        }
        setBoundaryPolygon(vertices)
    }

    def setBoundaryPolygon(vertices: collection.Seq[Float]): Unit = {
        bPoly = new Polygon(vertices.toArray)
    }

    def boundaryPolygon: Polygon = {
        import ge._
        bPoly.setPosition(getX, getY)
        bPoly.setOrigin(getX, getY)
        bPoly.setRotation(getRotation)
        bPoly
    }

    def collidesWith(other: GdxCollider): Boolean = {
        val poly1 = boundaryPolygon
        val poly2 = other.boundaryPolygon
        if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
            Intersector.overlapConvexPolygons(poly1, poly2)
        }
        else {
            false
        }
    }

    def avoidOverlap(other: GdxCollider): Option[GdxVector2] = {
        val poly1 = this.boundaryPolygon
        val poly2 = other.boundaryPolygon
        if (poly1.getBoundingRectangle.overlaps(poly2.getBoundingRectangle)) {
            val mtv = new Intersector.MinimumTranslationVector
            val polygonOverlap = Intersector.overlapConvexPolygons(poly1, poly2, mtv)
            if (polygonOverlap) {
                ge.moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth)
                Some(mtv.normal)
            }
            else {
                None
            }
        }
        else {
            None
        }
    }

    val moveVec = new Vector2(0, 0)
    val incident = new GdxVector2()
    val normal = new GdxVector2()

    def doBounce(ge: GameEntity, mtv: GdxVector2, other: Boolean) {
        if (other) {
            ge.moveBy(moveVec.set(-mtv.x, -mtv.y))
        }
        else {
            ge.moveBy(moveVec.set(mtv.x, mtv.y))
        }
        val p = ge.getComponent(classOf[PhysicsBehavior])
        incident.set(p.velocity.x, p.velocity.y)
        val n = mtv.nor()
        if (other) {
            normal.set(-n.x, -n.y)
        }
        else {
            normal.set(n)
        }
        val dot = incident.dot(normal)
        val reflection = incident.sub(normal.scl(2 * dot))
        p.setVelocity(reflection.x, reflection.y)
    }

    def bounceOff(other: GdxCollider): Unit = {
        avoidOverlap(other).foreach { mtv =>
            doBounce(ge, mtv, false)
        }
    }

    def bounceBothOff(other: GdxCollider): Unit = {
        avoidOverlap(other).foreach { mtv =>
            doBounce(ge, mtv, false)
            doBounce(other.ge, mtv, true)
        }
    }
}
