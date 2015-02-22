package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{ Raster, Color }
import com.sksamuel.scrimage.filter.util._
import com.sksamuel.scrimage.geom.Rectangle
import thirdparty.jhlabs.math._
import CellularFilter._

import thirdparty.jhlabs.image.{ Colormap, Gradient, PixelUtils, ImageMath }

object CellularFilter {

  protected val probabilities = Array.ofDim[Byte](8192)
  var factorial = 1
  var total = 0f
  val mean = 2.5f
  for (i <- 0 until 10) {
    if (i > 1) factorial *= i
    val probability = Math.pow(mean, i).toFloat * Math.exp(-mean).toFloat / factorial
    val start = (total * 8192).toInt
    total += probability
    val end = (total * 8192).toInt
    for (j <- start until end) probabilities(j) = i.toByte
  }

  val RANDOM = 0

  val SQUARE = 1

  val HEXAGONAL = 2

  val OCTAGONAL = 3

  val TRIANGULAR = 4

  class Point {
    var index: Int = _
    var x: Float = _
    var y: Float = _
    var dx: Float = _
    var dy: Float = _
    var cubeX: Float = _
    var cubeY: Float = _
    var distance: Float = Float.PositiveInfinity
  }
}

case class CellularFilter(
    scale: Float = 32f,
    stretch: Float = 1f,
    angle: Float = 0f,
    amount: Float = 1f,
    turbulence: Float = 1f,
    gain: Float = 0.5f,
    bias: Float = 0.5f,
    distancePower: Float = 2f,
    useColor: Boolean = false,
    colormap: Colormap = new Gradient(),
    coefficients: Array[Float] = Array(1, 0, 0, 0),
    angleCoefficient: Float = 0f,
    gradientCoefficient: Float = 0f,
    randomness: Float = 0f,
    gridType: Int = HEXAGONAL) extends PixelByPixelFilter with ExhaustiveFilter {

  protected var cos = Math.cos(angle).toFloat
  protected var sin = Math.sin(angle).toFloat

  def setCoefficient(i: Int, v: Float) {
    coefficients(i) = v
  }

  def getCoefficient(i: Int): Float = coefficients(i)

  def setF1(v: Float) {
    coefficients(0) = v
  }

  def getF1(): Float = coefficients(0)

  def setF2(v: Float) {
    coefficients(1) = v
  }

  def getF2(): Float = coefficients(1)

  def setF3(v: Float) {
    coefficients(2) = v
  }

  def getF3(): Float = coefficients(2)

  def setF4(v: Float) {
    coefficients(3) = v
  }

  def getF4(): Float = coefficients(3)

  private[this] def checkCube(x: Float,
                              y: Float,
                              cubeX: Int,
                              cubeY: Int,
                              results: Array[Point]): Float = {

    val random = new java.util.Random(571 * cubeX + 23 * cubeY)
    // random.setSeed(571 * cubeX + 23 * cubeY)

    val numPoints = gridType match {
      case SQUARE => 1
      case HEXAGONAL => 1
      case OCTAGONAL => 2
      case TRIANGULAR => 2
      case _ => probabilities(random.nextInt() & 0x1fff)
    }

    for (i <- 0 until numPoints) {
      var px = 0f
      var py = 0f
      var weight = 1.0f
      gridType match {
        case RANDOM =>
          px = random.nextFloat()
          py = random.nextFloat()

        case SQUARE =>
          px = 0.5f
          py = 0.5f
          if (randomness != 0) {
            px += randomness * (random.nextFloat() - 0.5f)
            py += randomness * (random.nextFloat() - 0.5f)
          }

        case HEXAGONAL =>
          if ((cubeX & 1) == 0) {
            px = 0.75f
            py = 0
          } else {
            px = 0.75f
            py = 0.5f
          }
          if (randomness != 0) {
            px += randomness *
              Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py))
            py += randomness *
              Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137)
          }

        case OCTAGONAL =>
          i match {
            case 0 =>
              px = 0.207f
              py = 0.207f

            case 1 =>
              px = 0.707f
              py = 0.707f
              weight = 1.6f

          }
          if (randomness != 0) {
            px += randomness *
              Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py))
            py += randomness *
              Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137)
          }

        case TRIANGULAR =>
          if ((cubeY & 1) == 0) {
            if (i == 0) {
              px = 0.25f
              py = 0.35f
            } else {
              px = 0.75f
              py = 0.65f
            }
          } else {
            if (i == 0) {
              px = 0.75f
              py = 0.35f
            } else {
              px = 0.25f
              py = 0.65f
            }
          }
          if (randomness != 0) {
            px += randomness *
              Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py))
            py += randomness *
              Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137)
          }
      }
      val dx = Math.abs(x - px).toFloat * weight
      val dy = Math.abs(y - py).toFloat * weight

      val d = {
        if (distancePower == 1.0f) dx + dy
        else if (distancePower == 2.0f) Math.sqrt(dx * dx + dy * dy).toFloat
        else Math.pow(
          Math.pow(dx, distancePower).toFloat + Math.pow(dy, distancePower).toFloat,
          1 / distancePower).toFloat
      }

      if (d < results(0).distance) {
        val p = results(2)
        results(2) = results(1)
        results(1) = results(0)
        results(0) = p
        p.distance = d
        p.dx = dx
        p.dy = dy
        p.x = cubeX + px
        p.y = cubeY + py
      } else if (d < results(1).distance) {
        val p = results(2)
        results(2) = results(1)
        results(1) = p
        p.distance = d
        p.dx = dx
        p.dy = dy
        p.x = cubeX + px
        p.y = cubeY + py
      } else if (d < results(2).distance) {
        val p = results(2)
        p.distance = d
        p.dx = dx
        p.dy = dy
        p.x = cubeX + px
        p.y = cubeY + py
      }
    }
    results(2).distance
  }

  def findPoints(x: Float, y: Float, results: Array[Point]) = {
    val ix = x.toInt
    val iy = y.toInt
    val fx = x - ix
    val fy = y - iy
    var d = checkCube(fx, fy, ix, iy, results)
    if (d > fy) d = checkCube(fx, fy + 1, ix, iy - 1, results)
    if (d > 1 - fy) d = checkCube(fx, fy - 1, ix, iy + 1, results)
    if (d > fx) {
      checkCube(fx + 1, fy, ix - 1, iy, results)
      if (d > fy) d = checkCube(fx + 1, fy + 1, ix - 1, iy - 1, results)
      if (d > 1 - fy) d = checkCube(fx + 1, fy - 1, ix - 1, iy + 1, results)
    }
    if (d > 1 - fx) {
      d = checkCube(fx - 1, fy, ix + 1, iy, results)
      if (d > fy) d = checkCube(fx - 1, fy + 1, ix + 1, iy - 1, results)
      if (d > 1 - fy) d = checkCube(fx - 1, fy - 1, ix + 1, iy + 1, results)
    }
    var t = 0f
    for (i <- 0 until 3) t += coefficients(i) * results(i).distance
    if (angleCoefficient != 0) {
      var angle = Math.atan2(y - results(0).y, x - results(0).x).toFloat
      if (angle < 0) angle += 2 * Math.PI.toFloat
      angle /= 4 * Math.PI.toFloat
      t += angleCoefficient * angle
    }
    if (gradientCoefficient != 0) {
      val a = 1 / (results(0).dy + results(0).dx)
      t += gradientCoefficient * a
    }
    t
  }

  def turbulence2(x: Float, y: Float, freq: Float, results: Array[Point]): Float = {
    var t = 0.0f
    var f = 1.0f
    while (f <= freq) {
      t += findPoints(f * x, f * y, results) / f
      f *= 2
    }
    t
  }

  def readPointColor(p: Point, src: Raster) = {
    val srcx = clamp(((p.x - 1000) * scale).toInt, src.width)
    val srcy = clamp(((p.y - 1000) * scale).toInt, src.height)
    src.read(srcx, srcy)
  }

  def apply(x: Int, y: Int, src: Raster): Color = {
    val nx: Float = (cos * x + sin * y) / scale + 1000
    val ny: Float = (-sin * x + cos * y) / (scale * stretch) + 1000

    val results = Array.fill(3) { new Point }
    var f =
      if (turbulence == 1.0f) findPoints(nx, ny, results)
      else turbulence2(nx, ny, turbulence, results)
    f *= 2
    f *= amount

    if (useColor) {
      f = (results(1).distance - results(0).distance) / (results(1).distance + results(0).distance)
      f = smoothStep(coefficients(1), coefficients(0), f)
      mixColors(f, 0xff000000, readPointColor(results(0), src))
    } else if (colormap != null) {
      Color(colormap.getColor(f))
    } else {
      val v = clamp((f * 255).toInt)
      Color(v, v, v)
    }
  }
}
