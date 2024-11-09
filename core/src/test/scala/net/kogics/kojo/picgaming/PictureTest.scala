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
    // todo with stub raster pics later
    val pic1 = Picture.image("../assets/green-sq.png")
    val pic2 = Picture.image("../assets/green-sq.png")
    val pic = Builtins.picBatch(pic1, pic2)
    val b1 = pic.boundaryPolygon.getTransformedVertices.toList
    println(b1)
    assert(true)
  }

  test("Rectangle picture closeness") {
    val pic1 = Picture.rectangle(50, 50)
    val pic2 = Picture.rectangle(50, 50)
    pic2.setPosition(100, 0)
    // gap between them is now 50
    assert(!pic1.isCloser(pic2, 50))
    assert(pic1.isCloser(pic2, 50.1))
  }

  test("Ellipse picture closeness") {
    val pic1 = Picture.ellipse(50, 50)
    val pic2 = Picture.ellipse(50, 50)
    pic2.setPosition(150, 0)
    // gap between them is now 50
    assert(!pic1.isCloser(pic2, 50))
    assert(pic1.isCloser(pic2, 50.1))
  }

  test("Rectangle picture closeness with scaling") {
    val pic1 = Picture.rectangle(50, 50)
    pic1.scale(1.1)
    val pic2 = Picture.rectangle(50, 50)
    pic2.setPosition(100, 0)
    // gap between them is now less than 50
    assert(pic1.isCloser(pic2, 50))
  }

  test("Ellipse picture closeness with scaling") {
    val pic1 = Picture.ellipse(50, 50)
    pic1.scale(1.1)
    val pic2 = Picture.ellipse(50, 50)
    pic2.setPosition(150, 0)
    // gap between them is now less than 50
    assert(pic1.isCloser(pic2, 50))
  }

  test("Rectangle picture closeness with rotation") {
    val pic1 = Picture.rectangle(50, 50)
    pic1.rotate(5)
    val pic2 = Picture.rectangle(50, 50)
    pic2.setPosition(100, 0)
    // gap between them is now greater than 50
    assert(!pic1.isCloser(pic2, 50.1))
  }

  test("Ellipse picture closeness with rotation") {
    val pic1 = Picture.ellipse(50, 50)
    pic1.rotate(5)
    val pic2 = Picture.ellipse(50, 50)
    pic2.setPosition(150, 0)
    // gap between them is now still 50
    assert(!pic1.isCloser(pic2, 50))
    assert(pic1.isCloser(pic2, 50.1))
  }
}
