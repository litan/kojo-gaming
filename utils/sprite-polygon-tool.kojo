cleari()
//disablePanAndZoom()

// double or float array to be printed
val doubleArray = true

val assetsDir = "/home/lalit/Downloads/hunted3-assets"
val sprite = Picture.image(s"$assetsDir/player_run1.png")

draw(sprite)

var points = ArrayBuffer.empty[Point]
var pointsPic: Picture = Picture.rectangle(0, 0)
pointsPic.draw()

def updateBoundary() {
    val pointsPic2 = Picture.fromVertexShape { s =>
        s.beginShape()
        points.foreach { pt =>
            s.vertex(pt.x, pt.y)
        }
        s.endShape()
    }.withPenColor(blue).withPenThickness(4)
    pointsPic2.draw()
    pointsPic.erase()
    pointsPic = pointsPic2
}

onMouseClick { (x, y) =>
    points.append(Point(x, y))
    updateBoundary()

    val len = points.length
    if (points(len - 2) == points(len - 1)) {
        points.remove(len - 1)
        val pts = points
            .flatMap(pt => Array(pt.x, pt.y))
            .map(n => if (doubleArray) s"${n}" else s"${n}f")

        println(pts.mkString("Array(", ", ", ")"))
        points.append(points.head)
        updateBoundary()
        points = ArrayBuffer.empty[Point]
    }
}

activateCanvas()
