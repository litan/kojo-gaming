package net.kogics.kojo.gaming

import com.badlogic.gdx.Gdx

object KojoUtils {
  lazy val cwidth = WorldBounds.width
  lazy val cheight = WorldBounds.height

  def doublesEqual(d1: Double, d2: Double, tol: Double): Boolean = {
    if (d1 == d2) return true
    else if (math.abs(d1 - d2) < tol) return true
    else return false
  }

  val Random = new java.util.Random

  def setRandomSeed(seed: Long): Unit = {
    Random.setSeed(seed)
  }

  def random(upperBound: Int) = Random.nextInt(upperBound)

  def random(lowerBound: Int, upperBound: Int): Int = {
    if (lowerBound >= upperBound) lowerBound
    else
      lowerBound + random(upperBound - lowerBound)
  }

  def randomDouble(upperBound: Double): Double = {
    if ((upperBound == 0) || (upperBound != upperBound)) 0
    else
      Random.nextDouble * upperBound
  }

  def randomDouble(lowerBound: Double, upperBound: Double): Double = {
    if (lowerBound >= upperBound) lowerBound
    else
      lowerBound + randomDouble(upperBound - lowerBound)
  }

  def randomNormalDouble: Double = Random.nextGaussian()

  def randomNormalDouble(mean: Double, stdDev: Double): Double = randomNormalDouble * stdDev + mean

  def randomBoolean = Random.nextBoolean

  def randomInt = Random.nextInt

  def randomLong = Random.nextLong

  def randomFrom[T](seq: collection.Seq[T]) = seq(random(seq.length))

  def initRandomGenerator(): Unit = {
    initRandomGenerator(System.currentTimeMillis())
  }

  def initRandomGenerator(seed: Long): Unit = {
    println(s"Random seed set to: ${seed}L")
    setRandomSeed(seed)
  }

  def isKeyPressed(key: Int): Boolean = {
    Gdx.input.isKeyPressed(key)
  }

  def repeat(n: Int)(fn: => Unit): Unit = {
    var i = 0
    while (i < n) {
      fn
      i += 1
    }
  }

  def repeatFor[T](seq: Iterable[T])(fn: T => Unit): Unit = {
    val iter = seq.iterator
    while (iter.hasNext) {
      fn(iter.next())
    }
  }
}
