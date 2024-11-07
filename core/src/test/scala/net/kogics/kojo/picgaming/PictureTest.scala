package net.kogics.kojo.picgaming

import net.kogics.kojo.core.Point

class PictureTests extends munit.FunSuite {

  test("Picture translation") {
    val pic = Picture.rectangle(100, 100)
    val pos0 = pic.position
    pic.translate(10, 20)
    val pos1 = pic.position
    assertEquals(pos0, Point(0, 0))
    assertEquals(pos1, Point(10, 20))
  }

  test("Picture boundary polygons") {
    val pic = Picture.rectangle(100, 100)
    val b1 = pic.boundaryPolygon.getTransformedVertices.toList
    pic.translate(10, 10)
    val b2 = pic.boundaryPolygon.getTransformedVertices.toList
    pic.scale(2)
    val b3 = pic.boundaryPolygon.getTransformedVertices.toList
    assertEquals(b1, List(0f, 0, 100, 0, 100, 100, 0, 100))
    assertEquals(b2, List(10f, 10, 110, 10, 110, 110, 10, 110))
    assertEquals(b3, List(10f, 10, 210, 10, 210, 210, 10, 210))
  }

  test("Picture batch boundary polygons".ignore) {
    val pic1 = Picture.image("../assets/green-sq.png")
    val pic2 = Picture.image("../assets/green-sq.png")
    val pic = Builtins.picBatch(pic1, pic2)
    val b1 = pic.boundaryPolygon.getTransformedVertices.toList
    println(b1)
    assert(true)
  }
}
